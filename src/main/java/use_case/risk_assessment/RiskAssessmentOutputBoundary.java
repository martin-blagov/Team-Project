package use_case.risk_assessment;

public interface RiskAssessmentOutputBoundary {
    void presentRiskResults(RiskAssessmentOutputData data);
    void presentFailView(String errorMessage);
}
