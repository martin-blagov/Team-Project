package use_case.underperforming;

import entity.Team;

public class UnderperformingInputData {
    private final Team currentTeam;

    public UnderperformingInputData(Team currentTeam) {
        this.currentTeam = currentTeam;
    }

    public Team getCurrentTeam() {
        return currentTeam;
    }
}
