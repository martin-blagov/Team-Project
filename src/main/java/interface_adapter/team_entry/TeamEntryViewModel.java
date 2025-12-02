package interface_adapter.team_entry;

import interface_adapter.ViewModel;

/**
 * The ViewModel for the Team Entry View.
 */
public class TeamEntryViewModel extends ViewModel<TeamEntryState> {

    // Labels
    public static final String TITLE_LABEL = "Enter Your Team";
    public static final String CONFIRM_BUTTON_LABEL = "Confirm Team";
    public static final String MENU_BUTTON_LABEL = "Back";

    // Player labels
    private final String[] playerLabels = {
            "Attacker 1",
            "Attacker 2",
            "Attacker 3",
            "Midfielder 1",
            "Midfielder 2",
            "Midfielder 3",
            "Midfielder 4",
            "Midfielder 5",
            "Defender 1",
            "Defender 2",
            "Defender 3",
            "Defender 4",
            "Defender 5",
            "Goalkeeper 1",
            "Goalkeeper 2",
    };

    private final String budgetLabel = "Remaining Budget: ";

    public TeamEntryViewModel() {
        super("team entry");
        setState(new TeamEntryState());
    }

    public String getTitleLabel() {
        return TITLE_LABEL;
    }

    public String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    public String[] getPlayerLabels() {
        return playerLabels;
    }

    public String getBudgetLabel() {
        return budgetLabel;
    }

}
