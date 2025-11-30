package use_case.display_team;

/**
 * Output Boundary for Display Team use case.
 * Presenter implements this to update the ViewModel.
 */
public interface DisplayTeamOutputBoundary {

    /**
     * Present the team to the user.
     * @param outputData Contains the team to display
     */
    void presentTeam(DisplayTeamOutputData outputData);

    /**
     * Present a failure message if team cannot be loaded.
     * @param errorMessage Description of what went wrong
     */
    void presentFailure(String errorMessage);
}