package use_case.transfer_suggestions;

/**
 * Input Data for the Transfer Suggestions use case.
 * Contains the user's request for transfer recommendations.
 */
public class TransferSuggestionsInputData {
    private final int numberOfTransfers;

    /**
     * Create input data for transfer suggestions.
     *
     * @param numberOfTransfers Number of transfers the user wants to make (typically 1-4)
     */
    public TransferSuggestionsInputData(int numberOfTransfers) {
        this.numberOfTransfers = numberOfTransfers;
    }

    public int getNumberOfTransfers() {
        return numberOfTransfers;
    }
}