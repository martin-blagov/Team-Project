package use_case.transfer_suggestions;

import entity.Player;
import entity.Team;
import use_case.PlayerDataAccessInterface;
import use_case.transfer_suggestions.TransferSuggestionsTeamDataAccessInterface;

import java.util.*;

/**
 * Interactor for the Transfer Suggestions use case.
 * Analyzes the user's team and suggests optimal transfers.
 */
public class TransferSuggestionsInteractor implements TransferSuggestionsInputBoundary {

    private final TransferSuggestionsTeamDataAccessInterface teamDataAccess;
    private final PlayerDataAccessInterface playerDataAccess;
    private final TransferSuggestionsOutputBoundary presenter;

    private static final int MAX_K = 10;
    private static final int MAX_OFFSET = 5;
    private static final int MAX_PLAYERS_PER_CLUB = 3;
    private static final double PRICE_PENALTY_WEIGHT = 0.3;

    public TransferSuggestionsInteractor(TransferSuggestionsTeamDataAccessInterface teamDataAccess,
                                         PlayerDataAccessInterface playerDataAccess,
                                         TransferSuggestionsOutputBoundary presenter) {
        this. teamDataAccess = teamDataAccess;
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

            int numberOfTransfers = inputData. getNumberOfTransfers();

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

            // 1.  Calculate percentiles for all players
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
            List<TransferSuggestionsOutputData. PlayerSwap> bestSwaps = findBestReplacements(playersToReplace, currentTeam);

            // Check if we found any valid swaps
            if (bestSwaps.isEmpty()) {
                presenter.presentFailure("No valid transfers found within budget and club constraints.");
                return;
            }

            // 4.  Create new team with transfers applied
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

            presenter. presentSuccess(outputData);

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
            newPlayers. remove(swap.getPlayerOut());
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

    // ========== Helper Methods ==========

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
                    Double. compare(a.getPredictedPoints(), b.getPredictedPoints()));

            // Assign percentiles ONLY for players in the current team
            for (int i = 0; i < eligiblePlayers.size(); i++) {
                Player player = eligiblePlayers.get(i);

                // Only store percentile if this player is in our team
                if (teamPlayerIds.contains(player.getId())) {
                    double percentile;
                    if (eligiblePlayers.size() == 1) {
                        percentile = 50.0; // Single player gets middle percentile
                    } else {
                        percentile = (i / (double)(eligiblePlayers. size() - 1)) * 100.0;
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
            for (Player player : currentTeam. getPlayers()) {
                teamPlayerIds.add(player. getId());
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
            if (validPlayers. isEmpty()) {
                continue;
            }

            // Sort by price (ascending: cheapest to most expensive)
            validPlayers.sort((a, b) ->
                    Double.compare(a.getNowCost(), b.getNowCost()));

            // Assign percentiles ONLY for players in the current team
            for (int i = 0; i < validPlayers. size(); i++) {
                Player player = validPlayers.get(i);

                // Only store percentile if this player is in our team
                if (teamPlayerIds. contains(player.getId())) {
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
     * - Cheap underperformer: perf=49, price=50 → priority = 49 - (50-49)*0. 3 = 48. 7
     * - Haaland (expensive star): perf=90, price=95 → priority = 90 - (95-90)*0. 3 = 88.5
     */
    private List<Player> findWorstPlayers(Team team,
                                          int numberOfTransfers,
                                          Map<Integer, Double> performancePercentiles,
                                          Map<Integer, Double> pricePercentiles) {

        List<Player> teamPlayers = team. getPlayers();

        // Calculate replacement priority for each player
        List<PlayerWithPriority> playersWithPriorities = new ArrayList<>();

        for (Player player : teamPlayers) {
            double performancePercentile = performancePercentiles.get(player. getId());
            double pricePercentile = pricePercentiles.get(player.getId());

            // Calculate replacement priority (lower = worse player)
            double pricePenalty = (pricePercentile - performancePercentile) * PRICE_PENALTY_WEIGHT;
            double replacementPriority = performancePercentile - pricePenalty;

            playersWithPriorities.add(new PlayerWithPriority(player, replacementPriority));
        }

        // Sort by priority (ascending: worst first)
        playersWithPriorities. sort((a, b) -> Double.compare(a.priority, b.priority));

        // Return the N worst players
        List<Player> worstPlayers = new ArrayList<>();
        for (int i = 0; i < numberOfTransfers && i < playersWithPriorities.size(); i++) {
            worstPlayers.add(playersWithPriorities. get(i).player);
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
     * Group players by their position.
     *
     * Example input: [Gyökeres(FWD), Dorgu(DEF), Milenković(DEF), Füllkrug(FWD), Palmer(MID)]
     * Example output: {
     *   4: [Gyökeres, Füllkrug],
     *   2: [Dorgu, Milenković],
     *   3: [Palmer]
     * }
     *
     * @param players List of players to group
     * @return Map of position -> list of players in that position
     */
    private Map<Integer, List<Player>> groupPlayersByPosition(List<Player> players) {
        Map<Integer, List<Player>> grouped = new HashMap<>();

        for (Player player : players) {
            int position = player.getElementType();
            if (! grouped.containsKey(position)) {
                grouped.put(position, new ArrayList<>());
            }
            grouped.get(position).add(player);
        }

        return grouped;
    }

    /**
     * Find the best replacement combinations using per-position k values with global offset.
     *
     * Algorithm:
     * 1. Group players to replace by position
     * 2. Calculate base k for each position (replacingCount + 2, minimum 3)
     * 3.  Iterate with increasing global offset (0, 1, 2, ...  up to MAX_OFFSET)
     * 4.  For each offset, get candidates per position and generate combinations
     * 5. Find the best valid combination at this offset level
     * 6. If a valid combination is found, return it immediately (early exit)
     * 7. If no valid combination, continue to next offset
     */
    private List<TransferSuggestionsOutputData.PlayerSwap> findBestReplacements(
            List<Player> playersToReplace,
            Team currentTeam) {

        // Step 1: Group players by position
        Map<Integer, List<Player>> playersByPosition = groupPlayersByPosition(playersToReplace);

        // Step 2: Calculate base k for each position
        Map<Integer, Integer> baseKByPosition = new HashMap<>();
        for (Map.Entry<Integer, List<Player>> entry : playersByPosition. entrySet()) {
            int position = entry.getKey();
            int replacingCount = entry.getValue().size();
            int baseK = Math.max(3, replacingCount + 2);
            baseKByPosition.put(position, baseK);
        }

        // Step 3: Iterate with increasing global offset
        for (int offset = 0; offset <= MAX_OFFSET; offset++) {

            // Step 4: Get candidates for each position with position-specific k + offset
            Map<Integer, List<Player>> candidatesByPosition = new HashMap<>();
            for (Integer position : playersByPosition.keySet()) {
                int k = Math.min(baseKByPosition.get(position) + offset, MAX_K);
                List<Player> candidates = getTopKCandidatesForPosition(
                        position, k, currentTeam, playersToReplace
                );
                candidatesByPosition.put(position, candidates);
            }

            // Step 5: Generate all valid combinations for this offset level
            List<List<Player>> allCombinations = generateCombinationsByPosition(
                    playersByPosition,
                    candidatesByPosition
            );

            // Step 6: Find the best valid combination at THIS offset level
            List<TransferSuggestionsOutputData. PlayerSwap> bestSwapsAtThisOffset = null;
            double bestPointsAtThisOffset = Double.NEGATIVE_INFINITY;

            for (List<Player> combination : allCombinations) {

                // Map this combination to actual swaps
                List<TransferSuggestionsOutputData.PlayerSwap> swaps = mapCombinationToSwaps(
                        combination,
                        playersToReplace
                );

                // Check budget constraint
                if (! fitsWithinBudget(swaps, currentTeam. getBudget())) {
                    continue;
                }

                // Check club constraint (max 3 per club)
                if (hasClubViolation(swaps, currentTeam)) {
                    continue;
                }

                // Calculate total points improvement
                double totalPointsImprovement = 0.0;
                for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
                    totalPointsImprovement += swap.getPointsImprovement();
                }

                // Is this the best valid solution at this offset level?
                if (totalPointsImprovement > bestPointsAtThisOffset) {
                    bestPointsAtThisOffset = totalPointsImprovement;
                    bestSwapsAtThisOffset = swaps;
                }
            }

            // Step 7: If we found ANY valid solution at this offset, return it immediately
            // This is the early exit - we don't need to search deeper offsets
            if (bestSwapsAtThisOffset != null) {
                return bestSwapsAtThisOffset;
            }

            // No valid solution at this offset - continue to next offset to expand candidate pool
        }

        // No valid solution found at any offset level
        return new ArrayList<>();
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
            replacementPlayerIds.add(player. getId());
        }

        // Filter: exclude team players EXCEPT those being replaced
        List<Player> candidates = new ArrayList<>();
        for (Player player : topPlayers) {
            boolean isInTeam = teamPlayerIds.contains(player.getId());
            boolean isBeingReplaced = replacementPlayerIds.contains(player.getId());

            // Include if: NOT in team OR is being replaced
            if (! isInTeam || isBeingReplaced) {
                candidates.add(player);
                if (candidates.size() == k) {
                    break;
                }
            }
        }

        return candidates;
    }

    /**
     * Generate combinations by choosing N different players from each position's candidate list.
     *
     * Example:
     * - Need 2 FWDs from [João Pedro, Watkins, Haaland]
     * - Need 2 DEFs from [Gabriel, Saliba, Van Dijk]
     * - Need 1 MID from [Saka, Foden]
     *
     * Generates combinations like:
     * - [João Pedro, Watkins, Gabriel, Saliba, Saka]
     * - [João Pedro, Haaland, Gabriel, Van Dijk, Saka]
     * - etc.
     *
     * @param playersByPosition How many players needed per position
     * @param candidatesByPosition Available candidates per position
     * @return List of valid combinations (each combination is a list of players)
     */
    private List<List<Player>> generateCombinationsByPosition(
            Map<Integer, List<Player>> playersByPosition,
            Map<Integer, List<Player>> candidatesByPosition) {

        // For each position, generate all ways to choose N different players
        // Then combine across positions (cartesian product of per-position choices)

        List<List<List<Player>>> choicesPerPosition = new ArrayList<>();
        List<Integer> positionOrder = new ArrayList<>(playersByPosition. keySet());

        for (Integer position : positionOrder) {
            int needed = playersByPosition. get(position).size();
            List<Player> candidates = candidatesByPosition.get(position);

            // Generate all ways to choose 'needed' players from candidates
            List<List<Player>> choices = chooseDifferent(candidates, needed);
            choicesPerPosition.add(choices);
        }

        // Now combine across positions (cartesian product)
        List<List<Player>> result = new ArrayList<>();
        generateCrossPositionCombinations(choicesPerPosition, 0, new ArrayList<>(), result);

        return result;
    }

    /**
     * Generate all ways to choose 'count' different items from 'candidates'.
     *
     * Example: Choose 2 from [A, B, C]
     * Returns: [[A,B], [A,C], [B,C]]
     *
     * This is a standard "combinations" algorithm: C(n, k)
     *
     * @param candidates List of available items
     * @param count How many to choose
     * @return List of all possible combinations
     */
    private List<List<Player>> chooseDifferent(List<Player> candidates, int count) {
        List<List<Player>> result = new ArrayList<>();
        chooseDifferentHelper(candidates, count, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Recursive helper for choosing N different players.
     * Uses backtracking to generate C(n, k) combinations.
     */
    private void chooseDifferentHelper(List<Player> candidates, int count,
                                       int start, List<Player> current,
                                       List<List<Player>> result) {
        // Base case: we've chosen enough players
        if (current.size() == count) {
            result.add(new ArrayList<>(current));
            return;
        }

        // Not enough candidates left to complete the combination
        if (start >= candidates. size()) {
            return;
        }

        // Recursive case: try adding each remaining candidate
        for (int i = start; i < candidates.size(); i++) {
            current.add(candidates. get(i));
            chooseDifferentHelper(candidates, count, i + 1, current, result);
            current.remove(current.size() - 1);  // Backtrack
        }
    }

    /**
     * Generate cartesian product of per-position choices.
     *
     * Example:
     * choicesPerPosition = [
     *   [[A,B], [A,C], [B,C]],  // FWD choices
     *   [[X], [Y]]              // DEF choices
     * ]
     *
     * Result: [[A,B,X], [A,B,Y], [A,C,X], [A,C,Y], [B,C,X], [B,C,Y]]
     */
    private void generateCrossPositionCombinations(List<List<List<Player>>> choicesPerPosition,
                                                   int positionIndex,
                                                   List<Player> current,
                                                   List<List<Player>> result) {
        // Base case: we've made a choice for every position
        if (positionIndex == choicesPerPosition.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        // Recursive case: try each choice for this position
        List<List<Player>> choicesForThisPosition = choicesPerPosition. get(positionIndex);
        for (List<Player> choice : choicesForThisPosition) {
            // Add all players from this choice
            current.addAll(choice);
            generateCrossPositionCombinations(choicesPerPosition, positionIndex + 1, current, result);
            // Backtrack: remove the players we just added
            for (int i = 0; i < choice.size(); i++) {
                current.remove(current.size() - 1);
            }
        }
    }

    /**
     * Map a combination of incoming players to specific swaps.
     *
     * Example:
     * playersToReplace = [Gyökeres, Dorgu, Milenković, Füllkrug, Palmer]
     * combination = [João Pedro, Gabriel, Saliba, Watkins, Saka]
     *
     * Strategy: Match by position - pair outgoing and incoming players of the same position.
     *
     * @param combination List of incoming players
     * @param playersToReplace List of outgoing players
     * @return List of PlayerSwap objects
     */
    private List<TransferSuggestionsOutputData.PlayerSwap> mapCombinationToSwaps(
            List<Player> combination,
            List<Player> playersToReplace) {

        List<TransferSuggestionsOutputData.PlayerSwap> swaps = new ArrayList<>();

        // Group outgoing players by position
        Map<Integer, List<Player>> outgoingByPosition = groupPlayersByPosition(playersToReplace);

        // Group incoming players by position
        Map<Integer, List<Player>> incomingByPosition = groupPlayersByPosition(combination);

        // For each position, pair them up in order
        for (Integer position : outgoingByPosition.keySet()) {
            List<Player> outgoing = outgoingByPosition.get(position);
            List<Player> incoming = incomingByPosition. get(position);

            // Pair them 1:1 in order
            for (int i = 0; i < outgoing.size(); i++) {
                swaps.add(new TransferSuggestionsOutputData.PlayerSwap(
                        outgoing.get(i),
                        incoming. get(i)
                ));
            }
        }

        return swaps;
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
     * 3.  Add players being transferred in
     * 4.  Check if any club exceeds 3 players
     */
    private boolean hasClubViolation(List<TransferSuggestionsOutputData.PlayerSwap> swaps, Team currentTeam) {
        // Count players per club in current team
        Map<String, Integer> clubCounts = new HashMap<>();
        for (Player player : currentTeam.getPlayers()) {
            String club = player.getTeamName();
            clubCounts.put(club, clubCounts. getOrDefault(club, 0) + 1);
        }

        // Remove players being transferred out
        for (TransferSuggestionsOutputData. PlayerSwap swap : swaps) {
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
        for (int count : clubCounts. values()) {
            if (count > MAX_PLAYERS_PER_CLUB) {
                return true; // Violation!
            }
        }

        return false; // No violation
    }

    @Override
    public void openPage() {
        try {
            // Load the current team
            Team currentTeam = teamDataAccess.getTeam();

            if (currentTeam == null) {
                presenter.presentFailure("No team found.  Please create a team first.");
                return;
            }

            // Create output data with just the current team (no transfers yet)
            TransferSuggestionsOutputData outputData = new TransferSuggestionsOutputData(
                    currentTeam,
                    currentTeam,  // Suggested team starts same as current
                    new ArrayList<>(),  // No swaps yet
                    0.0  // No improvement yet
            );

            presenter.presentOpenPage(outputData);

        } catch (Exception e) {
            presenter.presentFailure("Failed to load team: " + e.getMessage());
        }
    }

    @Override
    public void switchToHomePage() {
        presenter.switchToHomePage();
    }
}