package use_case.risk_assessment.rules;

import entity.Player;
import java.util.Optional;

public class ValueRiskRule implements RiskRule {

    // Example threshold: predicted points must be at least 0.5 per cost unit
    private static final double MIN_POINTS_PER_COST = 0.5;

    @Override
    public Optional<String> evaluate(Player p) {
        Double predicted = p.getPredictedPoints();
        double cost = p.getNowCost();

        if (predicted == null || cost <= 0) {
            return Optional.empty();
        }

        double valueScore = predicted / cost;

        if (valueScore < MIN_POINTS_PER_COST) {
            return Optional.of("Low value for cost");
        }

        return Optional.empty();
    }
}
