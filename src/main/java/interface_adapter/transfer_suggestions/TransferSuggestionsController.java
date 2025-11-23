package interface_adapter.transfer_suggestions;

import use_case.transfer_suggestions.TransferSuggestionsInputBoundary;
import use_case.transfer_suggestions.TransferSuggestionsInputData;

/**
 * Controller for the Transfer Suggestions use case.
 * Handles user actions and triggers the use case.
 */
public class TransferSuggestionsController {

    private final TransferSuggestionsInputBoundary interactor;
    private final TransferSuggestionsViewModel viewModel;

    public TransferSuggestionsController(TransferSuggestionsInputBoundary interactor,
                                         TransferSuggestionsViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    /**
     * Execute the transfer suggestions use case.
     * Called when the user clicks "Suggest Transfers" button.
     */
    public void execute() {
        TransferSuggestionsState state = viewModel.getState();

        // Set loading state
        state.setLoading(true);
        state.setErrorMessage(null);
        state.setSuccessMessage(null);
        viewModel.setState(state);
        viewModel.firePropertyChange();

        // Create input data and execute
        int numberOfTransfers = state.getNumberOfTransfers();
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(numberOfTransfers);

        interactor.execute(inputData);
    }

    /**
     * Switch to the home page view.
     * Called when the user clicks "Back to Home" button.
     */
    public void switchToHomePage() {
        interactor.switchToHomePage();
    }
}