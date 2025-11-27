package use_case.risk_assessment.risk;

import entity.Player;

public class ValueRiskRule implements RiskRule {

    // Example threshold: predicted points must be at least 0.5 per cost unit
    private static final double MIN_POINTS_PER_COST = 0.5;

    @Override
    public String getName() {
        return "Value Risk";
    }

    @Override
    public boolean isTriggered(Player player) {
        Double predicted = player.getPredictedPoints();
        double cost = player.getNowCost();

        if (predicted == null) return false;

        return cost/predicted <= MIN_POINTS_PER_COST;
    }
}
