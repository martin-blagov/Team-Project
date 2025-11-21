package interface_adapter.home;

import interface_adapter.ViewModel;

/**
 * The ViewModel for the Home View.
 */
public class HomeViewModel extends ViewModel<Void> {

    public static final String TITLE_LABEL = "Premier League Fantasy Home";

    // Button labels
    public static final String BEST_TEAM_BUTTON_LABEL = "Best Team";
    public static final String TEAM_INPUT_BUTTON_LABEL = "Build/Edit Team";
    public static final String REPLACEMENT_BUTTON_LABEL = "Underperforming Players";
    public static final String BEST_PLAYERS_BUTTON_LABEL = "Best Players";
    public static final String TRANSFER_BUTTON_LABEL = "Transfers";
    public static final String STATS_BUTTON_LABEL = "Player Stats";
    public static final String LINEUP_BUTTON_LABEL = "Starting Lineup";

    //TODO REMOVE
    public static final String TEST_SCROLLABLE_LIST_BUTTON_LABEL = "Test Scrollable List";
    public static final String TEST_DISPLAY_PLAYERS_BUTTON_LABEL = "Test Display Players (Clean Arch Demo)";
    public static final String TEST_TEAM_BUTTON_LABEL = "Test Team";

    public HomeViewModel() {
        super("home");
    }
}
