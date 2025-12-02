package interface_adapter.starting_lineup;

import java.util.List;

import entity.Player;
import entity.Team;
import interface_adapter.ViewManagerModel;
import interface_adapter.team_view.TeamViewModel;
import use_case.starting_lineup.StartingLineupOutputBoundary;
import use_case.starting_lineup.StartingLineupOutputData;

/**
 * Presenter that shows the starting lineup view.
 */
public class StartingLineupPresenter implements StartingLineupOutputBoundary {

    private final ViewManagerModel viewManagerModel;
    private final StartingLineupViewModel viewModel;

    public StartingLineupPresenter(ViewManagerModel viewManagerModel, StartingLineupViewModel viewModel) {
        this.viewManagerModel = viewManagerModel;
        this.viewModel = viewModel;
    }

    @Override
    public void presentLineup(StartingLineupOutputData outputData) {
        final Team startingTeam = outputData.getStartingTeam();
        final List<Player> benchPlayers = outputData.getBenchPlayers();

        final TeamViewModel teamViewModel = viewModel.getTeamViewModel();
        teamViewModel.setTeam(startingTeam);

        // Update the view model state with bench players.
        viewModel.updateLineup(startingTeam, benchPlayers);

        viewManagerModel.setState(viewModel.getViewName());
        viewManagerModel.firePropertyChange();
    }
}
