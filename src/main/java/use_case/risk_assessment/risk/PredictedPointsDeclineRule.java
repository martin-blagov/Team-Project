package use_case.risk_assessment.risk;

import entity.Player;

public class PredictedPointsDeclineRule implements RiskRule {

    public static final double THRESHOLD_RATIO = 0.6;
    @Override
    public boolean isTriggered(Player player) {

        Double predicted = player.getPredictedPoints();
        if (predicted == null) {
            return false;
        }

        double last5 = player.getLast5Stat("total_points");
        if (last5 <= 0) {
            return false;
        }

        // predicted points drop significantly from last form
        return predicted < last5 * THRESHOLD_RATIO; // 40% decline
    }

    @Override
    public String getName() {
        return "Predicted Points Decline";
    }
}
