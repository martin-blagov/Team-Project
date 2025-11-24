package interface_adapter.transfer_suggestions;

import interface_adapter.ViewManagerModel;
import interface_adapter.home.HomeViewModel;
import use_case.transfer_suggestions.TransferSuggestionsOutputBoundary;
import use_case.transfer_suggestions.TransferSuggestionsOutputData;

/**
 * Presenter for the Transfer Suggestions use case.
 * Formats the interactor's output data for the view.
 */
public class TransferSuggestionsPresenter implements TransferSuggestionsOutputBoundary {

    private final TransferSuggestionsViewModel viewModel;
    private final ViewManagerModel viewManagerModel;
    private final HomeViewModel homeViewModel;

    public TransferSuggestionsPresenter(TransferSuggestionsViewModel viewModel,
                                        ViewManagerModel viewManagerModel,
                                        HomeViewModel homeViewModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        this.homeViewModel = homeViewModel;
    }

    @Override
    public void presentSuccess(TransferSuggestionsOutputData outputData) {
        TransferSuggestionsState state = viewModel.getState();

        // Update state with results
        state.setOriginalTeam(outputData.getOriginalTeam());
        state.setSuggestedTeam(outputData.getSuggestedTeam());
        state.setSwaps(outputData.getSwaps());
        state.setTotalPointsImprovement(outputData.getTotalPointsImprovement());

        // Format success message
        String successMessage = String.format(
                "Found %d optimal transfers! Expected improvement: +%.2f points",
                outputData.getSwaps().size(),
                outputData.getTotalPointsImprovement()
        );
        state.setSuccessMessage(successMessage);
        state.setErrorMessage(null);
        state.setLoading(false);

        // Notify view
        viewModel.setState(state);
        viewModel.firePropertyChange();
    }

    @Override
    public void presentFailure(String errorMessage) {
        TransferSuggestionsState state = viewModel.getState();

        state.setErrorMessage(errorMessage);
        state.setSuccessMessage(null);
        state.setLoading(false);

        // Clear any previous results
        state.setSuggestedTeam(null);
        state.setSwaps(null);

        // Notify view
        viewModel.setState(state);
        viewModel.firePropertyChange();
    }

    @Override
    public void switchToHomePage() {
        viewManagerModel.setState(homeViewModel.getViewName());
        viewManagerModel.firePropertyChange();
    }
}