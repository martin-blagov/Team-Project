package use_case.risk_assessment;

public interface RiskAssessmentOutputBoundary {

    void presentRiskResults(RiskAssessmentOutputData outputData);

    void presentFailView(String s);
}
