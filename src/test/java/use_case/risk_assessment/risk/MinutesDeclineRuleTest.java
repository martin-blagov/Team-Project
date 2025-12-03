package use_case.risk_assessment.risk;

import entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MinutesDeclineRuleTest {

    private Player makePlayer(double last3Minutes, double seasonAvgMinutes) {
        return new Player(
                1, "P", 3, "a", 5.0, 3, "X",
                Map.of(), // season totals (unused)
                Map.of("season_avg_minutes", seasonAvgMinutes), // REQUIRED
                Map.of("minutes_last3", last3Minutes),          // REQUIRED
                Map.of()  // last5 stats unused
        );
    }

    @Test
    void testTriggeredWhenMinutesDrop() {
        MinutesDeclineRule rule = new MinutesDeclineRule();
        // last3 = 30, avg = 100 → 30 < 50 → triggered
        Player p = makePlayer(30, 100);
        assertTrue(rule.isTriggered(p));
    }

    @Test
    void testNotTriggeredWhenMinutesStableOrHigher() {
        MinutesDeclineRule rule = new MinutesDeclineRule();
        // last3 = 60, avg = 100 → 60 < 50? No → not triggered
        Player p = makePlayer(60, 100);
        assertFalse(rule.isTriggered(p));
    }

    @Test
    void testName() {
        MinutesDeclineRule rule = new MinutesDeclineRule();
        assertEquals("Minutes Decline", rule.getName());
    }
}
