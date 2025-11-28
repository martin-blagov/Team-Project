package use_case.display_team;

/**
 * Input Boundary for Display Team use case.
 * Controller calls this to load the team.
 */
public interface DisplayTeamInputBoundary {

    /**
     * Execute the display team use case.
     * Loads the current team and displays it.
     * @param inputData Contains request parameters (currently empty)
     */
    void execute(DisplayTeamInputData inputData);
}