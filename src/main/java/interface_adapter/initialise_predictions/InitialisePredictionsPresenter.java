package interface_adapter.initialise_predictions;

import interface_adapter.ViewManagerModel;
import use_case.initialise_predictions.InitialisePredictionsOutputBoundary;
import use_case.initialise_predictions.InitialisePredictionsOutputData;

/**
 * Presenter for Initialize Predictions use case.
 * Responsible for:
 * 1. Updating the ViewModel with results
 * 2. Switching to home view when initialization completes
 */
public class InitialisePredictionsPresenter implements InitialisePredictionsOutputBoundary {
    private final InitialisePredictionsViewModel viewModel;
    private final ViewManagerModel viewManagerModel;
    private final String homeViewName;

    public InitialisePredictionsPresenter(
            InitialisePredictionsViewModel viewModel,
            ViewManagerModel viewManagerModel,
            String homeViewName) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        this.homeViewName = homeViewName;
    }

    @Override
    public void presentSuccess(InitialisePredictionsOutputData outputData) {
        // Update ViewModel with results
        viewModel.setStats(
                outputData.getTotalPlayersProcessed(),
                outputData.getGameweeksProcessed()
        );
        viewModel.setStatusMessage(
                String.format("Successfully loaded %d players from %d gameweeks",
                        outputData.getTotalPlayersProcessed(),
                        outputData.getGameweeksProcessed())
        );
        viewModel.setLoading(false);
        viewModel.setComplete(true);

        // Wait a moment to show success message, then switch to home
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Switch to home view
            viewManagerModel.setState(homeViewName);
            viewManagerModel.firePropertyChange();
        }).start();
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setStatusMessage("Initialization failed: " + errorMessage);
        viewModel.setLoading(false);
        viewModel.setComplete(false);
    }
}