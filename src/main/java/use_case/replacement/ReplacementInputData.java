package use_case.replacement;

import entity.Team;

public class ReplacementInputData {
    private final Team currentTeam;

    public ReplacementInputData(Team currentTeam) {
        this.currentTeam = currentTeam;
    }

    public Team getCurrentTeam() {
        return currentTeam;
    }
}
