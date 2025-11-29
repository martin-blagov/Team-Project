package interface_adapter.display_team;

import interface_adapter.ViewModel;

/**
 * ViewModel for Display Team use case.
 *
 * The ViewModel:
 * - Holds the DisplayTeamState
 * - Notifies observers (the View) when State changes
 */
public class DisplayTeamViewModel extends ViewModel<DisplayTeamState> {

    /**
     * Constructor - Initialize with view name and empty state.
     */
    public DisplayTeamViewModel() {
        super("display team");
        setState(new DisplayTeamState());
    }
}