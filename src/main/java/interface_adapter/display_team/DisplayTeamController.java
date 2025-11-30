package interface_adapter.display_team;

import use_case.display_team.DisplayTeamInputBoundary;
import use_case.display_team.DisplayTeamInputData;

/**
 * Controller for Display Team use case.
 *
 * RESPONSIBILITIES:
 * 1. Receive action from View ("load team")
 * 2. Create InputData
 * 3. Call Interactor
 */
public class DisplayTeamController {

    private final DisplayTeamInputBoundary interactor;

    /**
     * Constructor
     * @param interactor The use case interactor
     */
    public DisplayTeamController(DisplayTeamInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Load and display the current team.
     * Called by the View when it needs to show the team.
     */
    public void loadTeam() {
        // Step 1: Create InputData (empty in this case)
        DisplayTeamInputData inputData = new DisplayTeamInputData();

        // Step 2: Call Interactor
        interactor.execute(inputData);
    }
}