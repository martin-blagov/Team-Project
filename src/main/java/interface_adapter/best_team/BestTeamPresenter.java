package interface_adapter.best_team;

import use_case.best_team.BestTeamOutputBoundary;
import use_case.best_team.BestTeamResponseModel;
import interface_adapter.ViewManagerModel;

public class BestTeamPresenter implements BestTeamOutputBoundary {
    private final BestTeamViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public BestTeamPresenter(BestTeamViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void present(BestTeamResponseModel responseModel){
        viewModel.setPlayers(responseModel.getPlayers());
        viewModel.setTotalCost(responseModel.getTotalCost());
        viewModel.setTotalPredictedPoints(responseModel.getTotalPredictedPoints());

        viewManagerModel.setState(BestTeamViewModel.VIEW_TITLE);
        viewManagerModel.firePropertyChange();
    }
}
