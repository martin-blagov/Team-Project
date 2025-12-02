package use_case.risk_assessment.risk;

import entity.Player;

import java.util.Map;

public class FormDropRule implements RiskRule {

    private static final double THRESHOLD_RATIO = 0.6;
    // last3 < 60% of seasonAvg = poor form

    @Override
    public String getName() {
        return "Form Drop";
    }

    @Override
    public boolean isTriggered(Player player) {
        if (player == null) return false;

        // Last 3 form
        Map<String, Double> last3 = player.getAllLast3Stats();
        if (last3 == null) return false;

        Double last3Form = last3.get("form_last3");
        if (last3Form == null || last3Form <= 0) return false;

        // Season avg form
        Map<String, Double> avg = player.getAllSeasonAvgStats();
        if (avg == null) return false;

        Double seasonAvgForm = avg.get("season_avg_form");
        if (seasonAvgForm == null || seasonAvgForm <= 0) return false;

        return last3Form < seasonAvgForm * THRESHOLD_RATIO;
    }
}
