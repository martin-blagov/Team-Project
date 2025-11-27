package use_case.risk_assessment.risk;

import entity.Player;
import java.util.ArrayList;
import java.util.List;

public class PlayerRisk {

    private final Player player;
    private final List<RiskRule> rules;
    private final List<RiskRule> triggeredRules;

    public PlayerRisk(Player player, List<RiskRule> rules) {
        this.player = player;
        this.rules = rules;
        this.triggeredRules = new ArrayList<>();
        evaluate();
    }

    private void evaluate() {
        for (RiskRule rule : rules) {
            if (rule.isTriggered(player)) {
                triggeredRules.add(rule);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public int getRiskCount() {
        return triggeredRules.size();
    }

    public List<RiskRule> getTriggeredRules() {
        return triggeredRules;
    }

    /**
     * Builds a readable comma-separated string of the triggered risk names.
     * Used by the Presenter and View.
     */
    public String getTriggeredRiskNames() {
        if (triggeredRules.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < triggeredRules.size(); i++) {
            builder.append(triggeredRules.get(i).getName());
            if (i < triggeredRules.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
