package use_case.risk_assessment.risk;

import entity.Player;

import java.util.List;

public class PlayerRisk {

    private final Player player;
    private final List<RiskRule> rules;

    public PlayerRisk(Player player, List<RiskRule> rules) {
        this.player = player;
        this.rules = rules;
    }

    public Player getPlayer() {
        return player;
    }

    public List<RiskRule> getRules() {
        return rules;
    }

    public int getRiskCount() {
        int count = 0;
        for (RiskRule rule : rules) {
            if (rule.evaluate(player)) {
                count++;
            }
        }
        return count;
    }
}
