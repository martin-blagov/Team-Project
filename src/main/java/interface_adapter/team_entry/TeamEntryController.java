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
     * Executes the Confirm Team Entry Use Case.
     * NOW ACCEPTS POSITIONS (Option 2)
     */
    public void execute(String[] names,
                        int[] ids,
                        String[] positions,
                        String budget) {

        TeamEntryInputData inputData =
                new TeamEntryInputData(
                        names,
                        ids,
                        positions,
                        budget
                );

        teamEntryUseCaseInteractor.execute(inputData);
    }

    /**
     * Opens the Team Entry Page (restore team if exists)
     */
    public void openPage() {
        teamEntryUseCaseInteractor.openPage();
    }

    /**
     * Navigates back to Home Page
     */
    public void switchToHomePage() {
        teamEntryUseCaseInteractor.switchToHomePage();
    }
}
