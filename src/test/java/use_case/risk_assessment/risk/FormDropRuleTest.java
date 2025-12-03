package use_case.risk_assessment.risk;

import entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormDropRuleTest {

    private Player makePlayer(double last3Form, double seasonAvgForm) {
        return new Player(
                1, "P", 3, "a", 5.0, 3, "X",
                Map.of(), // season total stats (ignored)
                Map.of("season_avg_form", seasonAvgForm), // REQUIRED
                Map.of("form_last3", last3Form),          // REQUIRED
                Map.of()                                  // last5 stats (ignored)
        );
    }

    @Test
    void testTriggeredWhenFormDrops() {
        // last3Form = 3.0, avg = 10.0 → 3 < 6 → triggered
        FormDropRule rule = new FormDropRule();
        Player p = makePlayer(3.0, 10.0);
        assertTrue(rule.isTriggered(p));
    }

    @Test
    void testNotTriggeredWhenFormImproves() {
        // last3Form = 8.0, avg = 10.0 → 8 < 6 → false
        FormDropRule rule = new FormDropRule();
        Player p = makePlayer(8.0, 10.0);
        assertFalse(rule.isTriggered(p));
    }

    @Test
    void testName() {
        FormDropRule rule = new FormDropRule();
        assertEquals("Form Drop", rule.getName());
    }
}
