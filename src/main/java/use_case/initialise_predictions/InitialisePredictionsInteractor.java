package use_case.initialise_predictions;
import entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;
import use_case.PlayerDataAccessInterface;

import java.io.IOException;
import java.util.*;

public class InitialisePredictionsInteractor implements InitialisePredictionsInputBoundary {
    private final BootstrapDataAccessInterface bootstrapDataAccess;
    private final GameWeekDataAccessInterface gameWeekDataAccess;
    private final ModelCoefficientDataAccessInterface modelCoefficientAccess;
    private final InitialisePredictionsOutputBoundary presenter;
    private final PlayerDataAccessInterface playerDataAccess;

    public InitialisePredictionsInteractor(
            BootstrapDataAccessInterface bootstrapDataAccess,
            GameWeekDataAccessInterface gameWeekDataAccess,
            ModelCoefficientDataAccessInterface modelCoefficientAccess,
            InitialisePredictionsOutputBoundary presenter, PlayerDataAccessInterface playerDataAccess) {
        this.bootstrapDataAccess = bootstrapDataAccess;
        this.gameWeekDataAccess = gameWeekDataAccess;
        this.modelCoefficientAccess = modelCoefficientAccess;
        this.presenter = presenter;
        this.playerDataAccess = playerDataAccess;
    }

    @Override
    public void execute(InitialisePredictionsInputData inputData) {
        try {
            String bootstrapJSON = bootstrapDataAccess.getBootstrapData();
            JSONObject bootstrapJSONObject = new JSONObject(bootstrapJSON);

            List<Integer> finishedGameweeks = getFinishedGameweeks(bootstrapJSONObject);

            Map<Integer, List<Map<String, Double>>> playerGameweekStats =
                    fetchAndOrganizeGameweekData(finishedGameweeks);

            Map<Integer, Map<String, Double>> coefficients = modelCoefficientAccess.getAllCoefficients();

            List<Player> players = createAllPlayers(bootstrapJSONObject, playerGameweekStats, coefficients);

            InitialisePredictionsOutputData outputData = new InitialisePredictionsOutputData(
                    players.size(), finishedGameweeks.size()
            );
            presenter.presentSuccess(outputData);
        } catch (IOException e) {
            presenter.presentFailure("Failed to fetch data: " + e.getMessage());
        } catch (Exception e) {
            presenter.presentFailure("Error: " + e.getMessage());
        }


    }

    private List<Integer> getFinishedGameweeks(JSONObject bootstrapData) {
        List<Integer> finishedGameweeks = new ArrayList<>();
        JSONArray events = bootstrapData.getJSONArray("events");

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            if (event.getBoolean("finished")) {
                finishedGameweeks.add(event.getInt("id"));
                }
            }

        return finishedGameweeks;
        }

    /**
     * Fetch all gameweek data and organize by player ID.
     * Returns: Map of playerId -> List of gameweek stats (in order)
     */
    private Map<Integer, List<Map<String, Double>>> fetchAndOrganizeGameweekData(
            List<Integer> finishedGameweeks) throws IOException {

        Map<Integer, List<Map<String, Double>>> playerGameweekStats = new HashMap<>();

        // Fetch each gameweek
        for (int gwId : finishedGameweeks) {
            String gwJson = gameWeekDataAccess.getGameWeekLiveData(gwId);
            JSONObject gwData = new JSONObject(gwJson);
            JSONArray elements = gwData.getJSONArray("elements");

            // Process each player's stats for this gameweek
            for (int i = 0; i < elements.length(); i++) {
                JSONObject playerGw = elements.getJSONObject(i);
                int playerId = playerGw.getInt("id");
                JSONObject stats = playerGw.getJSONObject("stats");

                // Convert stats to Map
                Map<String, Double> statMap = parseStatsToMap(stats);

                // Add to player's gameweek list
                // Check if this player exists in our map yet
                if (!playerGameweekStats.containsKey(playerId)) {
                    // First time seeing this player - create their list
                    playerGameweekStats.put(playerId, new ArrayList<>());
                }
                // Now add this gameweek's stats to the player's list
                playerGameweekStats.get(playerId).add(statMap);
            }
        }

        return playerGameweekStats;
    }

    private Map<String, Double> parseStatsToMap(JSONObject stats) {
        Map<String, Double> statMap = new HashMap<>();

        String[] statNames = {
                "total_points", "minutes", "goals_scored", "assists",
                "clean_sheets", "bonus", "bps", "saves", "penalties_saved",
                "goals_conceded", "influence", "creativity", "threat",
                "expected_goals", "expected_assists",
                "clearances_blocks_interceptions", "recoveries", "tackles", "defensive_contribution"
                };

        for (String statName : statNames) {
            double value = 0.0;
            if (stats.has(statName)) {
                Object obj = stats.get(statName);
                if (obj instanceof Number) {
                    value = ((Number) obj).doubleValue();
                    } else if (obj instanceof String) {
                    try {
                        value = Double.parseDouble((String) obj);
                        } catch (NumberFormatException e) {
                        value = 0.0;
                        }
                    }
                }
            statMap.put(statName, value);
            }

        return statMap;
        }

    /**
     * Create all Player entities from bootstrap data and gameweek stats.
     */
    private List<Player> createAllPlayers( JSONObject bootstrapData,
    Map<Integer, List<Map<String, Double>>> playerGameweekStats,
    Map<Integer, Map<String, Double>> coefficients) {

        List<Player> players = new ArrayList<>();
        JSONArray elements = bootstrapData.getJSONArray("elements");

        Map<Integer, String> teamMap = parseTeams(bootstrapData);

            // Process each player
        for (int i = 0; i < elements.length(); i++) {
            JSONObject playerJson = elements.getJSONObject(i);

            int id = playerJson.getInt("id");
            String webName = playerJson.getString("web_name");
            int elementType = playerJson.getInt("element_type");
            String status = playerJson.getString("status");
            double nowCost = playerJson.getDouble("now_cost") / 10.0;
            String team =  teamMap.get(playerJson.getInt("team"));

            // Get this player's gameweek stats
            List<Map<String, Double>> gameweeks = playerGameweekStats.getOrDefault(id, new ArrayList<>());

            // Calculate all features
            Map<String, Double> seasonTotals = calculateSeasonTotals(gameweeks);
            Map<String, Double> seasonAvgs = calculateRollingAverage(gameweeks, -1, "season_avg_");
            Map<String, Double> last3 = calculateRollingAverage(gameweeks, 3, "_last3");
            Map<String, Double> last5 = calculateRollingAverage(gameweeks, 5, "_last5");
            // Create Player entity
            Player player = new Player(
                    id, webName, elementType, status, nowCost, elementType, team,
                    seasonTotals, seasonAvgs, last3, last5
                    );

            // Calculate prediction
            Map<String, Double> positionCoefficients = coefficients.get(elementType);
            if (positionCoefficients != null) {
                player.calculatePredictedPoints(positionCoefficients);
                }

            players.add(player);
        }
        playerDataAccess.saveAll(players);
        return players;
    }

    /**
     * Parse team information from bootstrap data.
     * Returns: Map of teamId -> teamName
     */
    private Map<Integer, String> parseTeams(JSONObject bootstrapData) {
        Map<Integer, String> teamMap = new HashMap<>();
        JSONArray teams = bootstrapData.getJSONArray("teams");

        for (int i = 0; i < teams.length(); i++) {
            JSONObject team = teams.getJSONObject(i);
            int teamId = team.getInt("id");
            String teamName = team.getString("name");
            teamMap.put(teamId, teamName);
        }

        return teamMap;
    }

    /**
     * Calculate rolling average for a specified window.
     *
     * @param gameweeks List of all gameweek stats
     * @param window Number of gameweeks to average (use -1 for ALL previous gameweeks)
     * @param suffix Suffix to add to stat names (e.g., "_last3", "_last5", "season_avg_")
     * @return Map of calculated averages
     */
    private Map<String, Double> calculateRollingAverage(
            List<Map<String, Double>> gameweeks,
            int window,
            String suffix) {

        Map<String, Double> averages = new HashMap<>();

        if (gameweeks.isEmpty()) {
            return averages;
        }

        Set<String> statNames = gameweeks.get(0).keySet();
        int size = gameweeks.size();

        for (String stat : statNames) {
            double sum = 0.0;
            int count = 0;

            // Determine start and end indices
            int start;
            int end = size - 1;  // Always end before the latest gameweek

            if (window == -1) {
                // Season average: all previous gameweeks
                start = 0;
            } else {
                // Rolling window: last N gameweeks before latest
                start = Math.max(0, size - window - 1);
            }

            // Calculate sum
            for (int i = start; i < end; i++) {
                sum += gameweeks.get(i).getOrDefault(stat, 0.0);
                count++;
            }

            double avg = count > 0 ? sum / count : 0.0;

            // Add appropriate prefix/suffix
            String featureName = suffix.startsWith("season_avg_")
                    ? suffix + stat
                    : stat + suffix;

            averages.put(featureName, avg);
        }

        return averages;
    }

    private Map<String, Double> calculateSeasonTotals(List<Map<String, Double>> gameweeks) {
        Map<String, Double> totals = new HashMap<>();

        if (gameweeks.isEmpty()) {
            return totals;
            }

        // Get all stat names from first gameweek
        Set<String> statNames = gameweeks.get(0).keySet();

        for (String stat : statNames) {
            double sum = 0.0;
            for (Map<String, Double> gw : gameweeks) {
                sum += gw.getOrDefault(stat, 0.0);
                }
            totals.put(stat, sum);
            }

        return totals;
        }

}
