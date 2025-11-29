package use_case.risk_assessment.risk;

import entity.Player;

public class FormDropRule implements RiskRule {

    private static final double THRESHOLD_RATIO = 0.6;
    // last3 < 60% of seasonAvg = poor form

    @Override
    public String getName() {
        return "Form Drop";
    }

    @Override
    public boolean isTriggered(Player p) {
        double last3 = p.getLast3Stat("total_points");
        double avg = p.getSeasonAvgStat("total_points");

        return last3 < avg * THRESHOLD_RATIO;
    }
}
