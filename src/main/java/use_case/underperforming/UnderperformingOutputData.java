package use_case.underperforming;

import java.util.List;

public class UnderperformingOutputData {
    private final List<String> underperformingPlayers;

    public UnderperformingOutputData(List<String> underperformingPlayers) {
        this.underperformingPlayers = underperformingPlayers;
    }

    public List<String> getUnderperformingPlayers() {
        return underperformingPlayers;
    }
}
