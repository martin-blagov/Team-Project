package use_case.test_display_players;

/**
 * DEMO Output Boundary for Test Display Players use case.
 */
public interface TestDisplayPlayersOutputBoundary {

    /**
     * Present the filtered players to the user.
     * @param outputData Contains the filtered players and available teams
     */
    void presentPlayers(TestDisplayPlayersOutputData outputData);

    /**
     * Present a failure message to the user.
     *
     * Called if something goes wrong (e.g., data access fails).
     *
     * @param errorMessage Description of what went wrong
     */
    void presentFailure(String errorMessage);
}
