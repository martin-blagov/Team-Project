package use_case.transfer_suggestions;

import entity.Player;
import entity.Team;
import use_case.PlayerDataAccessInterface;
import use_case.team_entry.TeamDataAccessInterface;

import java.util.*;

/**
 * Interactor for the Transfer Suggestions use case.
 * Analyzes the user's team and suggests optimal transfers.
 */
public class TransferSuggestionsInteractor implements TransferSuggestionsInputBoundary {

    private final TeamDataAccessInterface teamDataAccess;
    private final PlayerDataAccessInterface playerDataAccess;
    private final TransferSuggestionsOutputBoundary presenter;

    private static final int MAX_K = 10;
    private static final int MAX_PLAYERS_PER_CLUB = 3;
    private static final double PRICE_PENALTY_WEIGHT = 0.3;

    public TransferSuggestionsInteractor(TeamDataAccessInterface teamDataAccess,
                                         PlayerDataAccessInterface playerDataAccess,
                                         TransferSuggestionsOutputBoundary presenter) {
        this.teamDataAccess = teamDataAccess;
        this.playerDataAccess = playerDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(TransferSuggestionsInputData inputData) {
        try {
            // Get current team
            Team currentTeam = teamDataAccess.getTeam();

            if (currentTeam == null) {
                presenter.presentFailure("No team found. Please create a team first.");
                return;
            }

            int numberOfTransfers = inputData.getNumberOfTransfers();

            // Validate input
            // Validate input
            if (numberOfTransfers < 0 || numberOfTransfers > 15) {
                presenter.presentFailure("Number of transfers must be between 0 and 15.");
                return;
            }

            if (numberOfTransfers > currentTeam.getPlayers().size()) {
                presenter.presentFailure("Cannot transfer more players than you have in your team.");
                return;
            }

            // Handle 0 transfers - just return original team
            if (numberOfTransfers == 0) {
                TransferSuggestionsOutputData outputData = new TransferSuggestionsOutputData(
                        currentTeam,
                        currentTeam,  // Suggested team is same as original
                        new ArrayList<>(),  // Empty swaps list
                        0.0  // No improvement
                );
                presenter.presentSuccess(outputData);
                return;
            }

            // 1. Calculate percentiles for all players
            Map<Integer, Double> performancePercentiles = calculatePerformancePercentiles();
            Map<Integer, Double> pricePercentiles = calculatePricePercentiles();

            // 2. Find worst N players to replace
            List<Player> playersToReplace = findWorstPlayers(
                    currentTeam,
                    numberOfTransfers,
                    performancePercentiles,
                    pricePercentiles
            );

            // 3. Find best replacement combinations
            List<TransferSuggestionsOutputData.PlayerSwap> bestSwaps = findBestReplacements(playersToReplace, currentTeam);

            // Check if we found any valid swaps
            if (bestSwaps.isEmpty()) {
                presenter.presentFailure("No valid transfers found within budget and club constraints.");
                return;
            }

            // 4. Create new team with transfers applied
            Team suggestedTeam = applySwapsToTeam(currentTeam, bestSwaps);

            // 5. Calculate total points improvement
            double totalPointsImprovement = 0.0;
            for (TransferSuggestionsOutputData.PlayerSwap swap : bestSwaps) {
                totalPointsImprovement += swap.getPointsImprovement();
            }

            // 6. Create output data and present success
            TransferSuggestionsOutputData outputData = new TransferSuggestionsOutputData(
                    currentTeam,
                    suggestedTeam,
                    bestSwaps,
                    totalPointsImprovement
            );

            presenter.presentSuccess(outputData);

        } catch (Exception e) {
            presenter.presentFailure("Error generating transfer suggestions: " + e.getMessage());
        }
    }

    /**
     * Apply swaps to a team to create a new team.
     * Returns a new Team object with the swaps applied.
     */
    private Team applySwapsToTeam(Team originalTeam, List<TransferSuggestionsOutputData.PlayerSwap> swaps) {
        // Get current players
        List<Player> newPlayers = new ArrayList<>(originalTeam.getPlayers());

        // Apply each swap
        for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
            // Remove player out
            newPlayers.remove(swap.getPlayerOut());
            // Add player in
            newPlayers.add(swap.getPlayerIn());
        }

        // Calculate new budget
        float newBudget = originalTeam.getBudget();
        for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
            newBudget += swap.getPlayerOut().getNowCost();
            newBudget -= swap.getPlayerIn().getNowCost();
        }

        // Create new team
        return new Team(newPlayers, newBudget, true);
    }

    // ========== Helper Methods (to be implemented) ==========

    /**
     * Calculate performance percentile for each player in their position.
     * Returns a map of playerId -> percentile (0-100) for ONLY the players in the current team.
     * Lower percentile = worse performance, higher percentile = better performance.
     *
     * Filters out players with 0 predicted points to avoid skewing, but includes
     * any 0-point players that are in the user's current team.
     */
    private Map<Integer, Double> calculatePerformancePercentiles() {
        Map<Integer, Double> percentiles = new HashMap<>();
        Team currentTeam = teamDataAccess.getTeam();

        // Get set of player IDs in current team
        Set<Integer> teamPlayerIds = new HashSet<>();
        if (currentTeam != null) {
            for (Player player : currentTeam.getPlayers()) {
                teamPlayerIds.add(player.getId());
            }
        }

        // For each position (1=GK, 2=DEF, 3=MID, 4=FWD)
        for (int position = 1; position <= 4; position++) {
            List<Player> playersInPosition = playerDataAccess.getPlayersByPosition(position);

            // Build list of eligible players: non-zero points OR in current team
            List<Player> eligiblePlayers = new ArrayList<>();
            for (Player player : playersInPosition) {
                if (player.getPredictedPoints() == null) {
                    continue; // Skip players with null predictions
                }

                // Include if: has non-zero points OR is in the team
                if (player.getPredictedPoints() > 0.0 || teamPlayerIds.contains(player.getId())) {
                    eligiblePlayers.add(player);
                }
            }

            // Skip if no eligible players in this position
            if (eligiblePlayers.isEmpty()) {
                continue;
            }

            // Sort by predicted points (ascending: worst to best)
            eligiblePlayers.sort((a, b) ->
                    Double.compare(a.getPredictedPoints(), b.getPredictedPoints()));

            // Assign percentiles ONLY for players in the current team
            for (int i = 0; i < eligiblePlayers.size(); i++) {
                Player player = eligiblePlayers.get(i);

                // Only store percentile if this player is in our team
                if (teamPlayerIds.contains(player.getId())) {
                    double percentile;
                    if (eligiblePlayers.size() == 1) {
                        percentile = 50.0; // Single player gets middle percentile
                    } else {
                        percentile = (i / (double)(eligiblePlayers.size() - 1)) * 100.0;
                    }
                    percentiles.put(player.getId(), percentile);
                }
            }
        }

        return percentiles;
    }

    /**
     * Calculate price percentile for each player in their position.
     * Returns a map of playerId -> percentile (0-100) for ONLY the players in the current team.
     * Lower percentile = cheaper, higher percentile = more expensive.
     *
     * Does NOT filter out any players - all valid prices are included in the distribution.
     */
    private Map<Integer, Double> calculatePricePercentiles() {
        Map<Integer, Double> percentiles = new HashMap<>();
        Team currentTeam = teamDataAccess.getTeam();

        // Get set of player IDs in current team
        Set<Integer> teamPlayerIds = new HashSet<>();
        if (currentTeam != null) {
            for (Player player : currentTeam.getPlayers()) {
                teamPlayerIds.add(player.getId());
            }
        }

        // For each position (1=GK, 2=DEF, 3=MID, 4=FWD)
        for (int position = 1; position <= 4; position++) {
            List<Player> playersInPosition = playerDataAccess.getPlayersByPosition(position);

            // Use all players in position (no filtering for price)
            List<Player> validPlayers = new ArrayList<>();
            for (Player player : playersInPosition) {
                // Only skip if price data is missing (shouldn't happen)
                if (player.getNowCost() > 0) {
                    validPlayers.add(player);
                }
            }

            // Skip if no valid players in this position
            if (validPlayers.isEmpty()) {
                continue;
            }

            // Sort by price (ascending: cheapest to most expensive)
            validPlayers.sort((a, b) ->
                    Double.compare(a.getNowCost(), b.getNowCost()));

            // Assign percentiles ONLY for players in the current team
            for (int i = 0; i < validPlayers.size(); i++) {
                Player player = validPlayers.get(i);

                // Only store percentile if this player is in our team
                if (teamPlayerIds.contains(player.getId())) {
                    double percentile;
                    if (validPlayers.size() == 1) {
                        percentile = 50.0; // Single player gets middle percentile
                    } else {
                        percentile = (i / (double)(validPlayers.size() - 1)) * 100.0;
                    }
                    percentiles.put(player.getId(), percentile);
                }
            }
        }

        return percentiles;
    }

    /**
     * Find the N worst players to replace using percentile ranking with price penalty.
     *
     * Replacement priority formula:
     * priority = performance_percentile - (price_percentile - performance_percentile) * weight
     *
     * Lower priority = worse player (should be replaced first)
     *
     * Examples:
     * - Expensive underperformer: perf=50, price=80 → priority = 50 - (80-50)*0.3 = 41
     * - Cheap underperformer: perf=49, price=50 → priority = 49 - (50-49)*0.3 = 48.7
     * - Haaland (expensive star): perf=90, price=95 → priority = 90 - (95-90)*0.3 = 88.5
     */
    private List<Player> findWorstPlayers(Team team,
                                          int numberOfTransfers,
                                          Map<Integer, Double> performancePercentiles,
                                          Map<Integer, Double> pricePercentiles) {

        List<Player> teamPlayers = team.getPlayers();

        // Calculate replacement priority for each player
        List<PlayerWithPriority> playersWithPriorities = new ArrayList<>();

        for (Player player : teamPlayers) {
            double performancePercentile = performancePercentiles.get(player.getId());
            double pricePercentile = pricePercentiles.get(player.getId());

            // Calculate replacement priority (lower = worse player)
            double pricePenalty = (pricePercentile - performancePercentile) * PRICE_PENALTY_WEIGHT;
            double replacementPriority = performancePercentile - pricePenalty;

            playersWithPriorities.add(new PlayerWithPriority(player, replacementPriority));
        }

        // Sort by priority (ascending: worst first)
        playersWithPriorities.sort((a, b) -> Double.compare(a.priority, b.priority));

        // Return the N worst players
        List<Player> worstPlayers = new ArrayList<>();
        for (int i = 0; i < numberOfTransfers && i < playersWithPriorities.size(); i++) {
            worstPlayers.add(playersWithPriorities.get(i).player);
        }

        return worstPlayers;
    }

    /**
     * Helper class to pair a player with their replacement priority score.
     */
    private static class PlayerWithPriority {
        final Player player;
        final double priority;

        PlayerWithPriority(Player player, double priority) {
            this.player = player;
            this.priority = priority;
        }
    }

    /**
     * Find the best replacement combinations using iterative k-search.
     *
     * Algorithm:
     * 1. Start with k = max(3, numberOfTransfers)
     * 2. For each position that needs a replacement, get top k players
     * 3. Generate all combinations of replacements
     * 4. Check each combination for budget and club constraints
     * 5. Keep the best valid combination
     * 6. Increment k and repeat until MAX_K or until we find a solution
     */
    private List<TransferSuggestionsOutputData.PlayerSwap> findBestReplacements(
            List<Player> playersToReplace,
            Team currentTeam) {

        int numberOfTransfers = playersToReplace.size();
        int startK = Math.max(3, numberOfTransfers);

        List<TransferSuggestionsOutputData.PlayerSwap> bestSwaps = null;
        double bestTotalPointsImprovement = Double.NEGATIVE_INFINITY;

        // Iterate k from startK to MAX_K
        for (int k = startK; k <= MAX_K; k++) {
            // For each player to replace, get top k candidates in their position
            List<List<Player>> candidatesPerPlayer = new ArrayList<>();

            for (Player playerOut : playersToReplace) {
                List<Player> candidates = getTopKCandidatesForPosition(
                        playerOut.getElementType(),
                        k,
                        currentTeam,
                        playersToReplace  // Pass players being replaced
                );
                candidatesPerPlayer.add(candidates);
            }

            // Generate all combinations (cartesian product)
            List<List<Player>> allCombinations = generateCombinations(candidatesPerPlayer);

            // Check each combination
            for (List<Player> playersIn : allCombinations) {
                // Create swaps by pairing playersToReplace with playersIn
                List<TransferSuggestionsOutputData.PlayerSwap> swaps = new ArrayList<>();
                for (int i = 0; i < playersToReplace.size(); i++) {
                    swaps.add(new TransferSuggestionsOutputData.PlayerSwap(
                            playersToReplace.get(i),
                            playersIn.get(i)
                    ));
                }

                // Check budget constraint
                if (!fitsWithinBudget(swaps, currentTeam.getBudget())) {
                    continue;
                }

                // Check club constraint
                if (hasClubViolation(swaps, currentTeam)) {
                    continue;
                }

                // Calculate total points improvement
                double totalPointsImprovement = 0.0;
                for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
                    totalPointsImprovement += swap.getPointsImprovement();
                }

                // Keep track of best combination
                if (totalPointsImprovement > bestTotalPointsImprovement) {
                    bestTotalPointsImprovement = totalPointsImprovement;
                    bestSwaps = swaps;
                }
            }
        }

        // Return best swaps found (or empty list if none found)
        return bestSwaps != null ? bestSwaps : new ArrayList<>();
    }

    /**
     * Get top k players for a specific position, excluding players already in the team
     * EXCEPT those being replaced (they can be brought back in).
     * Sorted by predicted points (descending).
     */
    private List<Player> getTopKCandidatesForPosition(int position, int k, Team currentTeam,
                                                      List<Player> playersToReplace) {
        // Get all players in this position sorted by predicted points
        List<Player> topPlayers = playerDataAccess.getTopPlayersByPosition(position, -1);

        // Get set of player IDs already in team
        Set<Integer> teamPlayerIds = new HashSet<>();
        for (Player player : currentTeam.getPlayers()) {
            teamPlayerIds.add(player.getId());
        }

        // Get set of player IDs being replaced (these CAN be brought back in)
        Set<Integer> replacementPlayerIds = new HashSet<>();
        for (Player player : playersToReplace) {
            replacementPlayerIds.add(player.getId());
        }

        // Filter: exclude team players EXCEPT those being replaced
        List<Player> candidates = new ArrayList<>();
        for (Player player : topPlayers) {
            boolean isInTeam = teamPlayerIds.contains(player.getId());
            boolean isBeingReplaced = replacementPlayerIds.contains(player.getId());

            // Include if: NOT in team OR is being replaced
            if (!isInTeam || isBeingReplaced) {
                candidates.add(player);
                if (candidates.size() == k) {
                    break;
                }
            }
        }

        return candidates;
    }

    /**
     * Generate all combinations (cartesian product) of players.
     *
     * Example: If replacing 2 players, and we have:
     * - Position 1: [A, B, C]
     * - Position 2: [X, Y]
     *
     * Returns: [[A,X], [A,Y], [B,X], [B,Y], [C,X], [C,Y]]
     */
    private List<List<Player>> generateCombinations(List<List<Player>> candidatesPerPlayer) {
        List<List<Player>> result = new ArrayList<>();
        generateCombinationsHelper(candidatesPerPlayer, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Recursive helper for generating combinations.
     */
    private void generateCombinationsHelper(List<List<Player>> candidatesPerPlayer,
                                            int index,
                                            List<Player> current,
                                            List<List<Player>> result) {
        if (index == candidatesPerPlayer.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (Player player : candidatesPerPlayer.get(index)) {
            current.add(player);
            generateCombinationsHelper(candidatesPerPlayer, index + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Check if a combination of replacements fits within budget.
     *
     * Budget calculation: For each swap, we gain back the playerOut's cost
     * and spend the playerIn's cost.
     */
    private boolean fitsWithinBudget(List<TransferSuggestionsOutputData.PlayerSwap> swaps, float currentBudget) {
        float budgetAfterSwaps = currentBudget;

        for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
            // Selling playerOut adds to budget, buying playerIn subtracts
            budgetAfterSwaps += swap.getPlayerOut().getNowCost();
            budgetAfterSwaps -= swap.getPlayerIn().getNowCost();
        }

        return budgetAfterSwaps >= 0;
    }

    /**
     * Check if adding players would violate the club constraint (max 3 per club).
     *
     * Algorithm:
     * 1. Count current club distribution
     * 2. Remove players being transferred out
     * 3. Add players being transferred in
     * 4. Check if any club exceeds 3 players
     */
    private boolean hasClubViolation(List<TransferSuggestionsOutputData.PlayerSwap> swaps, Team currentTeam) {
        // Count players per club in current team
        Map<String, Integer> clubCounts = new HashMap<>();
        for (Player player : currentTeam.getPlayers()) {
            String club = player.getTeamName();
            clubCounts.put(club, clubCounts.getOrDefault(club, 0) + 1);
        }

        // Remove players being transferred out
        for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
            String club = swap.getPlayerOut().getTeamName();
            clubCounts.put(club, clubCounts.get(club) - 1);
            if (clubCounts.get(club) == 0) {
                clubCounts.remove(club);
            }
        }

        // Add players being transferred in
        for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
            String club = swap.getPlayerIn().getTeamName();
            clubCounts.put(club, clubCounts.getOrDefault(club, 0) + 1);
        }

        // Check if any club exceeds limit
        for (int count : clubCounts.values()) {
            if (count > MAX_PLAYERS_PER_CLUB) {
                return true; // Violation!
            }
        }

        return false; // No violation
    }

    @Override
    public void switchToHomePage() {
        presenter.switchToHomePage();
    }
}