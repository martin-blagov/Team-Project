package use_case.replacement;

import entity.Player;
import java.util.List;

public class ReplacementOutputData {
    private final List<Player> suggestedPlayers;

    public ReplacementOutputData(List<Player> suggestedPlayers) {
        this.suggestedPlayers = suggestedPlayers;
    }

    public List<Player> getSuggestedPlayers() {
        return suggestedPlayers;
    }
}
