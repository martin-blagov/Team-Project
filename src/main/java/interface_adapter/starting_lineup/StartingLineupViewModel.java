package interface_adapter.starting_lineup;

import java.util.List;

import entity.Player;
import entity.Team;
import interface_adapter.ViewModel;
import interface_adapter.team_view.TeamViewModel;

/**
 * ViewModel for the Starting Lineup use case.
 */
public class StartingLineupViewModel extends ViewModel<StartingLineupState> {
    private final TeamViewModel teamViewModel;

    public StartingLineupViewModel(TeamViewModel teamViewModel) {
        super(teamViewModel.getViewName());
        this.teamViewModel = teamViewModel;
        this.setState(new StartingLineupState());
    }

    /**
     * Returns the Team View Model.
     *
     * @return the underlying Team View Model.
     */
    public TeamViewModel getTeamViewModel() {
        return teamViewModel;
    }

    /**
     * Replace the current lineup state with the given starting team and bench.
     *
     * @param startingTeam the starting team.
     * @param benchPlayers the list of bench players.
     */
    public void updateLineup(Team startingTeam, List<Player> benchPlayers) {
        final StartingLineupState newState = new StartingLineupState();
        newState.setStartingTeam(startingTeam);
        newState.setBenchPlayers(benchPlayers);
        this.setState(newState);
        this.firePropertyChange("startingLineup");
    }

    /**
     * Returns the current view mode.
     *
     * @return the current view mode.
     */
    public StartingLineupState.ViewMode getViewMode() {
        final StartingLineupState state = getState();
        return state == null ? StartingLineupState.ViewMode.TABLE : state.getViewMode();
    }

    /**
     * Set a new view mode.
     *
     * @param viewMode the new view mode.
     */
    public void setViewMode(StartingLineupState.ViewMode viewMode) {
        final StartingLineupState newState = new StartingLineupState(getState());
        newState.setViewMode(viewMode);
        this.setState(newState);
        this.firePropertyChange("viewMode");
    }

    /**
     * Returns the current starting team.
     *
     * @return the starting team.
     */
    public Team getStartingTeam() {
        final StartingLineupState state = getState();
        if (state == null) {
            return null;
        }
        else {
            return state.getStartingTeam();
        }
    }

    /**
     * Returns the current bench players.
     *
     * @return list of bench players.
     */
    public List<Player> getBenchPlayers() {
        final StartingLineupState state = getState();
        if (state == null) {
            return null;
        }
        else {
            return state.getBenchPlayers();
        }
    }
}
