package use_case.team_entry;

public class TeamEntryInputData {
    private final String[] playerNames;
    private int[] playerIds;
    private String remainingBudget;

    public TeamEntryInputData(String[] playerNames, int[] ids, String remainingBudget) {
        this.playerNames = playerNames;
        this.playerIds = ids;
        this.remainingBudget = remainingBudget;
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public String getRemainingBudet() { return remainingBudget; }

    public int[] getPlayerIds() {
        return playerIds;
    }
}
