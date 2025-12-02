package use_case.display_individual_stat;

/**
 * The input data for the Display Individual Stats Use Case.
 */
public class DisplayIndividualStatInputData {
    private final int playerID;
    private final String filterOption;

    public DisplayIndividualStatInputData(int playerID, String filterOption) {
        this.playerID = playerID;
        this.filterOption = filterOption;
    }

    public int getPlayerID() { return playerID; }

    public String getFilterOption() { return filterOption; }
}
