package interface_adapter.starting_lineup;

import entity.Player;
import entity.Team;
import interface_adapter.ViewModel;
import interface_adapter.team_view.TeamViewModel;

import java.util.List;

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
     * @return the underlying Team View Model.
     */
    public TeamViewModel getTeamViewModel() {
        return teamViewModel;
    }

    /**
     * Replace the current lineup state with the given starting team and bench.
     */
    public void updateLineup(Team startingTeam, List<Player> benchPlayers) {
        StartingLineupState newState = new StartingLineupState();
        newState.setStartingTeam(startingTeam);
        newState.setBenchPlayers(benchPlayers);
        this.setState(newState);
        this.firePropertyChange("startingLineup");
    }

    public Team getStartingTeam() {
        StartingLineupState state = getState();
        if (state == null) {
            return null;
        } else {
            return state.getStartingTeam();
        }
    }

    public List<Player> getBenchPlayers() {
        StartingLineupState state = getState();
        if (state == null) {
            return null;
        } else {
            return state.getBenchPlayers();
        }
    }
}
