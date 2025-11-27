package interface_adapter.risk_assessment;

import use_case.risk_assessment.*;

import interface_adapter.ViewManagerModel;

public class RiskAssessmentPresenter implements RiskAssessmentOutputBoundary {

    private final RiskAssessmentViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public RiskAssessmentPresenter(RiskAssessmentViewModel viewModel,
                                   ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void presentRiskResults(RiskAssessmentOutputData data) {
        viewModel.setResults(data.getPlayerRisks());
        viewManagerModel.setState(viewModel.getViewName());
        viewManagerModel.firePropertyChange();
    }

    @Override
    public void presentFailView(String errorMessage) {
        // You can customize this, but simplest is:
        System.out.println(errorMessage);
    }
}
