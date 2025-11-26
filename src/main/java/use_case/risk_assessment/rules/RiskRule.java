package use_case.risk_assessment.rules;
import entity.Player;
import java.util.Optional;


public interface RiskRule {
    /**
     * An interface representing a risk assessment rule for evaluating players.
     * @param player The player to be evaluated.
     * @return An Optional containing a String message if the player meets the risk criteria, or an
     */
    Optional<String> evaluate(Player player);
}
