package interface_adapter.risk_assessment;

import use_case.risk_assessment.*;

public class RiskAssessmentController {

    private final RiskAssessmentInputBoundary interactor;

    public RiskAssessmentController(RiskAssessmentInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute() {
        interactor.execute(new RiskAssessmentInputData());
    }
}
