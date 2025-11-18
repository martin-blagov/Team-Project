package interface_adapter.starting_lineup;

import interface_adapter.ViewModel;
import interface_adapter.team_view.TeamViewModel;

/**
 * ViewModel for the starting lineup view.
 */
public class StartingLineupViewModel extends ViewModel<Void> {
    private final TeamViewModel teamViewModel;

    public StartingLineupViewModel(TeamViewModel teamViewModel) {
        super(teamViewModel.getViewName());
        this.teamViewModel = teamViewModel;
    }

    public TeamViewModel getTeamViewModel() {
        return teamViewModel;
    }
}

