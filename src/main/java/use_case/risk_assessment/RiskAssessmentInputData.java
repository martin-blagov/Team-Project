package use_case.risk_assessment;

import entity.Team;

public class RiskAssessmentInputData {
    private final Team currentTeam;

    public RiskAssessmentInputData(Team currentTeam) {
        this.currentTeam = currentTeam;
    }

    public Team getCurrentTeam() {
        return currentTeam;
    }
}
