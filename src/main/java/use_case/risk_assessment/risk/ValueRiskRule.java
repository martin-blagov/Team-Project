package use_case.risk_assessment.risk;

import entity.Player;

import java.util.Map;

public class ValueRiskRule implements RiskRule {

    // Example threshold: predicted points must be at least 0.5 per cost unit
    private static final double MIN_VALUE_THRESHOLD = 10.0;

    @Override
    public String getName() {
        return "Value Risk";
    }

    @Override
    public boolean isTriggered(Player player) {
        if (player == null) return false;
        double cost = player.getNowCost();
        if (cost <= 0) return false;

        // Season total points
        Map<String, Double> totals = player.getAllSeasonTotalStats();
        if (totals == null) return false;

        Double totalPoints = totals.get("total_points");
        if (totalPoints == null || totalPoints < 0) return false;

        // Value = points / cost
        double value = totalPoints / cost;

        return value < MIN_VALUE_THRESHOLD;
    }
}
