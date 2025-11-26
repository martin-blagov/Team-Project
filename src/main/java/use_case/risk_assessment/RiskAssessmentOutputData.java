package use_case.risk_assessment;

import java.util.List;

public class RiskAssessmentOutputData {
    private final List<String> underperformingPlayers;

    public RiskAssessmentOutputData(List<String> riskItems) {
        this.underperformingPlayers = riskItems;
    }

    public List<String> getUnderperformingPlayers() {
        return underperformingPlayers;
    }
}
