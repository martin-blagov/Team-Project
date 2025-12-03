package use_case.risk_assessment.risk;

import entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRiskTest {

    private Player makePlayer() {
        return new Player(
                1, "P", 3, "a", 5.0, 3, "X",
                Map.of(), // season totals unused

                // season_avg stats
                Map.of(
                        "season_avg_minutes", 100.0,
                        "season_avg_form", 10.0,
                        "season_avg_value", 6.5,
                        "season_avg_predicted_points", 5.0
                ),

                // last3 stats
                Map.of(
                        "minutes_last3", 30.0,       // triggers MinutesDeclineRule (30 < 100 * 0.5 = 50)
                        "form_last3", 4.0,           // triggers FormDropRule (4 < 10 * 0.6 = 6)
                        "predicted_points_last3", 1.0
                ),

                // last5 stats
                Map.of(
                        "value_last5", 5.5           // triggers ValueRiskRule if rule logic expects this
                )
        );
    }


    @Test
    void testRiskCountsCorrectly() {
        Player p = makePlayer();

        List<RiskRule> rules = List.of(
                new MinutesDeclineRule(),
                new FormDropRule(),
                new ValueRiskRule(),
                new PredictedPointsDeclineRule()
        );

        PlayerRisk risk = new PlayerRisk(p, rules);

        assertTrue(risk.getRiskCount() >= 2); // now guaranteed ≥ 2
        assertFalse(risk.getTriggeredRules().isEmpty());
        assertEquals(p, risk.getPlayer());
    }


    @Test
    void testTriggeredRiskNames() {
        Player p = makePlayer();
        List<RiskRule> rules = List.of(new MinutesDeclineRule());

        PlayerRisk risk = new PlayerRisk(p, rules);

        assertEquals("Minutes Decline", risk.getTriggeredRiskNames());
    }

    @Test
    void testTriggeredRiskNamesNone() {
        Player p = makePlayer();
        List<RiskRule> rules = List.of(); // no rules

        PlayerRisk risk = new PlayerRisk(p, rules);

        assertEquals("None", risk.getTriggeredRiskNames());
    }
}