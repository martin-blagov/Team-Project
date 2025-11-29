package use_case.team_entry;

public class TeamEntryInputData {

    private final String[] playerNames;
    private final int[] playerIds;
    private final String[] playerPositions;
    private final String remainingBudget;

    public TeamEntryInputData(String[] playerNames,
                              int[] playerIds,
                              String[] playerPositions,
                              String remainingBudget) {
        this.playerNames = playerNames;
        this.playerIds = playerIds;
        this.playerPositions = playerPositions;
        this.remainingBudget = remainingBudget;
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public int[] getPlayerIds() {
        return playerIds;
    }

    public String[] getPlayerPositions() {
        return playerPositions;
    }

    public String getRemainingBudget() {
        return remainingBudget;
    }
}