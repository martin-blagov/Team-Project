package interface_adapter.team_entry;

import use_case.team_entry.TeamEntryInputBoundary;
import use_case.team_entry.TeamEntryInputData;

public class TeamEntryController {

    private final TeamEntryInputBoundary teamEntryUseCaseInteractor;
    private final TeamEntryViewModel viewModel;

    public TeamEntryController(TeamEntryInputBoundary teamEntryUseCaseInteractor, TeamEntryViewModel viewModel) {
        this.teamEntryUseCaseInteractor = teamEntryUseCaseInteractor;
        this.viewModel = viewModel;
    }

    /**
     * Executes the Open Team Entry Page Use Case.
     */
    public void execute(String[] players) {
        TeamEntryState state = viewModel.getState();
        TeamEntryInputData inputData =
                new TeamEntryInputData(state.getPlayers(), state.getPlayerIds());

        teamEntryUseCaseInteractor.execute(inputData);
    }

    public void openPage() {
        teamEntryUseCaseInteractor.openPage();
    }

    public void switchToHomePage() {
        teamEntryUseCaseInteractor.switchToHomePage();
    }
}
