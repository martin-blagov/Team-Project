package interface_adapter.team_entry;

import use_case.team_entry.TeamEntryInputBoundary;
import use_case.team_entry.TeamEntryInputData;

public class TeamEntryController {

    private final TeamEntryInputBoundary teamEntryUseCaseInteractor;
    private final TeamEntryViewModel viewModel;

    public TeamEntryController(TeamEntryInputBoundary teamEntryUseCaseInteractor,
                               TeamEntryViewModel viewModel) {
        this.teamEntryUseCaseInteractor = teamEntryUseCaseInteractor;
        this.viewModel = viewModel;
    }

    /**
     * Executes the Confirm Team Entry use case by packaging the provided team data
     * into an input object and passing it to the interactor.
     *
     * @param names an array of player names selected by the user
     * @param ids an array of player IDs corresponding to the selected players
     * @param positions an array of position strings for each selected player
     * @param budget the remaining budget available to the user as a string
     */
    public void execute(String[] names, int[] ids, String[] positions, String budget) {
        final TeamEntryInputData inputData =
                new TeamEntryInputData(
                        names,
                        ids,
                        positions,
                        budget
                );

        teamEntryUseCaseInteractor.execute(inputData);
    }

    /**
     * Opens the Team Entry Page (restore team if exists).
     */
    public void openPage() {
        teamEntryUseCaseInteractor.openPage();
    }

    /**
     * Navigates back to Home Page.
     */
    public void switchToHomePage() {
        teamEntryUseCaseInteractor.switchToHomePage();
    }
}
