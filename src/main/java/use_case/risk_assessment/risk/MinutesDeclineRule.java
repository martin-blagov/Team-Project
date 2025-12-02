package use_case.risk_assessment.risk;

import entity.Player;

import java.util.Map;

public class MinutesDeclineRule implements RiskRule {

    private static final double THRESHOLD_RATIO = 0.5;
    // last3 < 50% of seasonAvg = risk

    @Override
    public String getName() {
        return "Minutes Decline";
    }


    @Override
    public boolean isTriggered(Player player) {

        if (player == null) return false;

        // Last 3 minutes
        Map<String, Double> last3 = player.getAllLast3Stats();
        if (last3 == null) return false;

        Double last3Minutes = last3.get("minutes_last3");
        if (last3Minutes == null || last3Minutes <= 0) return false;

        // Season average minutes
        Map<String, Double> avg = player.getAllSeasonAvgStats();
        if (avg == null) return false;

        Double seasonAvgMinutes = avg.get("season_avg_minutes");
        if (seasonAvgMinutes == null || seasonAvgMinutes <= 0) return false;

        return last3Minutes < seasonAvgMinutes * THRESHOLD_RATIO;
    }
}
