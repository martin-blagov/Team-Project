package interface_adapter.transfer_suggestions;

import interface_adapter.ViewModel;

/**
 * ViewModel for the Transfer Suggestions view.
 * Manages the state and notifies the view of changes.
 */
public class TransferSuggestionsViewModel extends ViewModel<TransferSuggestionsState> {

    // View labels
    public static final String TITLE_LABEL = "Transfer Suggestions";
    public static final String NUMBER_OF_TRANSFERS_LABEL = "Number of Transfers:";
    public static final String SUGGEST_BUTTON_LABEL = "Suggest Transfers";
    public static final String BACK_BUTTON_LABEL = "Back to Home";

    public TransferSuggestionsViewModel() {
        super("transfer suggestions");
        setState(new TransferSuggestionsState());
    }
}