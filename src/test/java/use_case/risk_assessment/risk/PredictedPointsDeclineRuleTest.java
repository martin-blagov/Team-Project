package use_case.risk_assessment.risk;

import entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PredictedPointsDeclineRuleTest {

    private Player makePlayer(double last5, double predictedPoints) {
        Player p = new Player(
                1, "P", 3, "a", 5.0, 3, "X",
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of("total_points_last5", last5)
        );

        // Set predictedPoints via the calculation method
        p.calculatePredictedPoints(Map.of(
                "intercept", predictedPoints   // simplest possible model
        ));

        return p;
    }

    @Test
    void testTriggeredWhenPredictedDrop() {
        PredictedPointsDeclineRule rule = new PredictedPointsDeclineRule();

        // predicted = 0.3, last5 = 1.0 -> 0.3 < 1.0 * 0.6 => true
        Player p = makePlayer(1.0, 0.3);

        assertTrue(rule.isTriggered(p));
    }

    @Test
    void testNotTriggeredWhenStable() {
        PredictedPointsDeclineRule rule = new PredictedPointsDeclineRule();

        // predicted = 1.0, last5 = 1.0 -> not less than 0.6
        Player p = makePlayer(1.0, 1.0);

        assertFalse(rule.isTriggered(p));
    }

    @Test
    void testName() {
        PredictedPointsDeclineRule rule = new PredictedPointsDeclineRule();
        assertEquals("Predicted Points Decline", rule.getName());
    }

}
