
package entity;

import java.util.HashMap;
import java.util.Map;

/**
 * FPL Player entity with prediction capabilities.
 * Position-specific behavior is handled by passing different coefficients,
 * not by inheritance.
 */
public class Player {

    private static final int GOALKEEPER_POSITION = 1;
    private static final int DEFENDER_POSITION = 2;
    private static final int MIDFIELDER_POSITION = 3;
    private static final int FORWARD_POSITION = 4;

    // Basic player info
    private final int id;
    private final String webName;
    // 1=GK, 2=DEF, 3=MID, 4=FWD
    private final int elementType;
    private final String status;
    private final double nowCost;
    private final int position;
    private final String team;

    // Feature maps
    // cumulative totals
    private final Map<String, Double> seasonTotalStats;
    private final Map<String, Double> seasonAvgStats;
    private final Map<String, Double> last3Stats;
    private final Map<String, Double> last5Stats;

    // Prediction result
    private Double predictedPoints;

    public Player(int id, String webName, int elementType, String status,
                  double nowCost, int position, String team, Map<String, Double> seasonTotalStats, Map<String, Double>
                          seasonAvgStats,
                  Map<String, Double> last3Stats, Map<String, Double> last5Stats) {
        this.id = id;
        this.webName = webName;
        this.elementType = elementType;
        this.status = status;
        this.nowCost = nowCost;
        this.position = position;
        this.team = team;
        this.seasonTotalStats = seasonTotalStats;
        this.seasonAvgStats = seasonAvgStats;
        this.last3Stats = last3Stats;
        this.last5Stats = last5Stats;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getWebName() {
        return webName;
    }

    public int getElementType() {
        return elementType;
    }

    public String getStatus() {
        return status;
    }

    public double getNowCost() {
        return nowCost;
    }

    public String getTeamName() {
        return team;
    }

    public Double getPredictedPoints() {
        return predictedPoints;
    }

    /**
     * Get position string based on the integer value.
     * @return position String corresponding to position number
     */
    public String getPosition() {
        String positionString = "unknown";

        if (position == GOALKEEPER_POSITION) {
            positionString = "goalkeeper";
        }
        else if (position == DEFENDER_POSITION) {
            positionString = "defender";
        }
        else if (position == MIDFIELDER_POSITION) {
            positionString = "midfielder";
        }
        else if (position == FORWARD_POSITION) {
            positionString = "forward";
        }

        return positionString;
    }

    // ========== NEW: Stat Retrieval Methods ==========

    /**
     * Get a specific season total stat (e.g., total goals, total assists).
     *
     * @param statName The stat name (e.g., "goals_scored", "assists", "total_points")
     * @return The season total value, or 0.0 if stat doesn't exist
     */
    public double getSeasonTotalStat(String statName) {
        return seasonTotalStats.get(statName);
    }

    /**
     * Get a specific season average stat.
     *
     * @param statName The stat name without "season_avg_" prefix (e.g., "goals_scored")
     * @return The season average value, or 0.0 if stat doesn't exist
     */
    public double getSeasonAvgStat(String statName) {
        return seasonAvgStats.get("season_avg_" + statName);
    }

    /**
     * Get a specific last 3 gameweeks stat.
     *
     * @param statName The stat name without "_last3" suffix (e.g., "goals_scored")
     * @return The last 3 average value, or 0.0 if stat doesn't exist
     */
    public double getLast3Stat(String statName) {
        return last3Stats.get(statName + "_last3");
    }

    /**
     * Get a specific last 5 gameweeks stat.
     *
     * @param statName The stat name without "_last5" suffix (e.g., "goals_scored")
     * @return The last 5 average value, or 0.0 if stat doesn't exist
     */
    public double getLast5Stat(String statName) {
        return last5Stats.get(statName + "_last5");
    }

    /**
     * Get all season total stats.
     *
     * @return Unmodifiable map of all season totals
     */
    public Map<String, Double> getAllSeasonTotalStats() {
        return new HashMap<>(seasonTotalStats);
    }

    /**
     * Get all season average stats.
     *
     * @return Unmodifiable map of all season averages
     */
    public Map<String, Double> getAllSeasonAvgStats() {
        return new HashMap<>(seasonAvgStats);
    }

    /**
     * Get all last 3 gameweeks stats.
     *
     * @return Unmodifiable map of all last 3 stats
     */
    public Map<String, Double> getAllLast3Stats() {
        return new HashMap<>(last3Stats);
    }

    /**
     * Get all last 5 gameweeks stats.
     *
     * @return Unmodifiable map of all last 5 stats
     */
    public Map<String, Double> getAllLast5Stats() {
        return new HashMap<>(last5Stats);
    }

    /**
     * Calculate predicted points using Ridge Regression coefficients.
     * Works for all positions - just pass the appropriate coefficients.
     *
     * @param coefficients json string picked up from coefficients file
     *
     */
    public void calculatePredictedPoints(Map<String, Double> coefficients) {

        final String interceptstring = "intercept";

        if (coefficients == null || !coefficients.containsKey(interceptstring) || !"a".equals(status)) {
            this.predictedPoints = 0.0;
        }
        else {

            double prediction = coefficients.get(interceptstring);

            // Loop through each coefficient and apply it
            for (Map.Entry<String, Double> entry : coefficients.entrySet()) {
                final String featureName = entry.getKey();
                final double coefficient = entry.getValue();

                if (interceptstring.equals(featureName)) {
                    continue;
                }

                final double featureValue = getFeatureValue(featureName);
                prediction += featureValue * coefficient;
            }
            this.predictedPoints = prediction;
        }

    }

    /**
     * Extract feature value based on coefficient name.
     *
     * @param featureName name of feature
     * @return FeatureValue corresponding to the name
     */

    public double getFeatureValue(String featureName) {
        double result = 0.0;

        if ("now_cost".equals(featureName)) {
            result = nowCost;
        }
        else if (featureName.startsWith("season_avg_")) {
            final Double value = seasonAvgStats.get(featureName);
            if (value != null) {
                result = value;
            }
        }
        else if (featureName.endsWith("_last3")) {
            final Double value = last3Stats.get(featureName);
            if (value != null) {
                result = value;
            }
        }
        else if (featureName.endsWith("_last5")) {
            final Double value = last5Stats.get(featureName);
            if (value != null) {
                result = value;
            }
        }

        return result;
    }
}
