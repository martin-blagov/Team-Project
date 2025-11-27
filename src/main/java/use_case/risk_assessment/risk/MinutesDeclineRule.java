package use_case.risk_assessment.risk;

import entity.Player;

public class MinutesDeclineRule implements RiskRule {

    private static final double THRESHOLD_RATIO = 0.5;
    // last3 < 50% of seasonAvg = risk

    @Override
    public String getRuleName() {
        return "Minutes Decline";
    }

    @Override
    public boolean evaluate(Player player) {
        double last3 = player.getLast3Stat("minutes");
        double avg = player.getSeasonAvgStat("minutes");

        return last3 < avg * THRESHOLD_RATIO;
    }
}
