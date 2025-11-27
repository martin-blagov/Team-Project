package use_case.risk_assessment.risk;
import entity.Player;

// Strategy Pattern: RiskRule interface for different risk assessment rules
public interface RiskRule {
    /**
     * An interface representing a risk assessment rule for evaluating players.
     *
     * @param player The player to be evaluated.
     * @return true if the player meets the risk criteria defined by the rule, false otherwise.
     *
     */

    boolean isTriggered(Player player);
    String getName();

}

