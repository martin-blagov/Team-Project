package use_case.risk_assessment.rules;

import entity.Player;
import java.util.Optional;

public class FormDropRule implements RiskRule {

    private static final double THRESHOLD_RATIO = 0.6;
    // last3 < 60% of seasonAvg = poor form

    @Override
    public Optional<String> evaluate(Player p) {
        double last3 = p.getLast3Stat("total_points");
        double avg = p.getSeasonAvgStat("total_points");

        if (avg <= 0) {
            return Optional.empty();
        }

        if (last3 < avg * THRESHOLD_RATIO) {
            return Optional.of("Recent form is below season average");
        }

        return Optional.empty();
    }
}
