package interface_adapter.risk_assessment;

import use_case.risk_assessment.RiskAssessmentOutputBoundary;
import use_case.risk_assessment.RiskAssessmentOutputData;

public class RiskAssessmentPresenter implements RiskAssessmentOutputBoundary {

    @Override
    public void presentUnderperformingPlayers(RiskAssessmentOutputData outputData) {
        // Minimal placeholder: print to console
        System.out.println("Underperforming players: " + outputData.getUnderperformingPlayers());
    }
}
