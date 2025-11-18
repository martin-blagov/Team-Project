package interface_adapter.starting_lineup;

import use_case.starting_lineup.StartingLineupInputBoundary;

/**
 * Controller for the starting lineup use case.
 */
public class StartingLineupController {
    private final StartingLineupInputBoundary inputBoundary;

    public StartingLineupController(StartingLineupInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void execute() {
        inputBoundary.execute();
    }
}

