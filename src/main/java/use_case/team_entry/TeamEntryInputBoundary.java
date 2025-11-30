package use_case.team_entry;

/**
 * Input Boundary for actions which are related to opening the team entry page.
 */
public interface TeamEntryInputBoundary {

    /**
     * Executes the open team entry page use case.
     * @param inputData the input data of players
     */
    void execute(TeamEntryInputData inputData);

    /**
     * Open team entry page.
     */
    void openPage();

    /**
     * Executes the switch to home page use case.
     */
    void switchToHomePage();
}
