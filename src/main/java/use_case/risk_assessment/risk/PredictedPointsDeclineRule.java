package use_case.risk_assessment.risk;

import entity.Player;

public class PredictedPointsDeclineRule implements RiskRule {

    @Override
    public String getRuleName() {
        return "Low Predicted Points";
    }

    @Override
    public boolean evaluate(Player player) {
        Double pred = player.getPredictedPoints();
        if (pred == null) return false;

        return pred < 3.5;  // tweakable
    }
}
