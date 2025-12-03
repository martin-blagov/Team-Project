package use_case.risk_assessment.risk;

import entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValueRiskRuleTest {

    private Player makePlayer(double cost, double avgPoints) {
        return new Player(
                1, "P", 3, "a", cost, 3, "X",
                Map.of("total_points", avgPoints * 10),
                Map.of(),
                Map.of(),
                Map.of()
        );
    }

    @Test
    void testHighCostLowReturnTriggers() {
        ValueRiskRule rule = new ValueRiskRule();
        Player p = makePlayer(10.0, 0.1);
        assertTrue(rule.isTriggered(p));
    }

    @Test
    void testFairValueDoesNotTrigger() {
        ValueRiskRule rule = new ValueRiskRule();
        Player p = makePlayer(5.0, 1.5); // total=15, value=3 → not a risk
        assertFalse(rule.isTriggered(p));
    }

    @Test
    void testName() {
        ValueRiskRule rule = new ValueRiskRule();
        assertEquals("Value Risk", rule.getName());
    }
}
