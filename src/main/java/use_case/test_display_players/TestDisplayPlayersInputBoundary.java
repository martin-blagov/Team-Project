package use_case.test_display_players;

/**
 * DEMO Input Boundary for Test Display Players use case.
 */
public interface TestDisplayPlayersInputBoundary {

    /**
     * Execute the display players use case.
     *
     * @param inputData Contains the filter criteria (search, position, team, price)
     */
    void execute(TestDisplayPlayersInputData inputData);
}