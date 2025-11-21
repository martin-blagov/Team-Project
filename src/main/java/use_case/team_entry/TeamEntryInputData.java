package use_case.team_entry;

public class TeamEntryInputData {
    private final String[] playerNames;
    private int[] playerIds;

    public TeamEntryInputData(String[] playerNames, int[] ids) {
        this.playerNames = playerNames;
        this.playerIds = ids;
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public int[] getPlayerIds() {
        return playerIds;
    }
}
