package interface_adapter.risk_assessment;

import use_case.risk_assessment.RiskAssessmentOutputBoundary;
import use_case.risk_assessment.RiskAssessmentOutputData;

public class RiskAssessmentPresenter implements RiskAssessmentOutputBoundary {

    @Override
    public void presentRiskResults(RiskAssessmentOutputData outputData) {
    }

    @Override
    public void presentFailView(String s) {

    }
}
