package use_case.transfer_suggestions;

/**
 * Input Boundary for the Transfer Suggestions use case.
 * The controller calls this interface to execute the use case.
 * The interactor implements this interface.
 */
public interface TransferSuggestionsInputBoundary {
    /**
     * Execute the transfer suggestions use case.
     * Analyzes the user's current team and suggests optimal transfers.
     *
     * @param inputData Contains the number of transfers to suggest
     */
    void execute(TransferSuggestionsInputData inputData);

    /**
     * Switch to the home page view.
     */
    void switchToHomePage();

    /**
     * Open the transfer suggestions page.
     * Loads the current team before displaying the view.
     */
    void openPage();
}