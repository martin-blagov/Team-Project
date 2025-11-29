package interface_adapter.test_display_players;

import interface_adapter.ViewModel;

/**
 * DEMO ViewModel for Test Display Players use case.
 *
 * The ViewModel:
 * - Holds the State
 * - Notifies observers (the View) when State changes
 * - Extends the base ViewModel class
 *
 * The View listens to this ViewModel for updates.
 */
public class TestDisplayPlayersViewModel extends ViewModel<TestDisplayPlayersState> {

    /**
     * Constructor - Initialize with view name and empty state.
     */
    public TestDisplayPlayersViewModel() {
        super("test display players");
        setState(new TestDisplayPlayersState());
    }
}