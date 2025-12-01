package use_case.risk_assessment;

import use_case.risk_assessment.risk.PlayerRisk;
import java.util.List;

public class RiskAssessmentOutputData {
    private final List<PlayerRisk> playerRisks;

    public RiskAssessmentOutputData(List<PlayerRisk> playerRisks) {
        this.playerRisks = playerRisks;
    }

    public List<PlayerRisk> getPlayerRisks() {
        return playerRisks;
    }
}
