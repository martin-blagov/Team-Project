package use_case.transfer_suggestions;

/**
 * Output Boundary for the Transfer Suggestions use case.
 * The presenter implements this interface to handle the results.
 * The interactor calls this interface to present results.
 */
public interface TransferSuggestionsOutputBoundary {
    /**
     * Present successful transfer suggestions.
     *
     * @param outputData Contains the suggested transfers and summary
     */
    void presentSuccess(TransferSuggestionsOutputData outputData);

    /**
     * Present failure when transfer suggestions cannot be generated.
     *
     * @param errorMessage Description of why suggestions failed
     */
    void presentFailure(String errorMessage);
}