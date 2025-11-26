package use_case.risk_assessment.rules;

import entity.Player;
import java.util.Optional;

public class MinutesDeclineRule implements RiskRule {

    private static final double THRESHOLD_RATIO = 0.5;
    // last3 < 50% of seasonAvg = risk

    @Override
    public Optional<String> evaluate(Player p) {
        double last3 = p.getLast3Stat("minutes");
        double avg = p.getSeasonAvgStat("minutes");

        // If data missing or zero, skip
        if (avg <= 0) {
            return Optional.empty();
        }

        if (last3 < avg * THRESHOLD_RATIO) {
            return Optional.of("Significant drop in playing minutes");
        }

        return Optional.empty();
    }
}
