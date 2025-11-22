package interface_adapter.starting_lineup;

import entity.Player;
import entity.Team;
import interface_adapter.ViewManagerModel;
import interface_adapter.team_view.TeamViewModel;
import use_case.starting_lineup.StartingLineupOutputBoundary;
import use_case.starting_lineup.StartingLineupOutputData;

import java.util.List;

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
        Team startingTeam = outputData.getStartingTeam();
        List<Player> benchPlayers = outputData.getBenchPlayers();

        // Update the backing TeamViewModel so the TeamDisplayView renders the players.
        TeamViewModel teamViewModel = viewModel.getTeamViewModel();
        teamViewModel.setTeam(startingTeam);

        // Update the view model state with bench players.
        viewModel.updateLineup(startingTeam, benchPlayers);

        viewManagerModel.setState(viewModel.getViewName());
        viewManagerModel.firePropertyChange();
    }
}
