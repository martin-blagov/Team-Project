package use_case.risk_assessment.risk;

import entity.Player;

import java.util.Map;

public class PredictedPointsDeclineRule implements RiskRule {

    public static final double THRESHOLD_RATIO = 0.6;
    @Override
    public boolean isTriggered(Player player) {

        if (player == null) return false;

        Double predicted = player.getPredictedPoints();
        if (predicted == null|| predicted <= 0) {
            return false;
        }

        Map<String, Double> last5Map = player.getAllLast5Stats();
        if (last5Map == null) return false;

        Double last5 = last5Map.get("total_points_last5");
        if (last5 == null || last5 <= 0) return false;
        // predicted points drop significantly from last form
        return predicted < last5 * THRESHOLD_RATIO; // 40% decline
    }

    @Override
    public String getName() {
        return "Predicted Points Decline";
    }
}
