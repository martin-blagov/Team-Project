package interface_adapter.team_entry;

public class TeamEntryState {
    private int[] playerIds = new int[15];
    private String[] players;
    private String errorMessage;
    private String successMessage;

    /**
     * Initializes the state with empty player fields.
     */
    public TeamEntryState() {
        this.players = new String[15];
        this.playerIds = new int[15];
        for (int i = 0; i < players.length; i++) {
            players[i] = "";
            playerIds[i] = -1; // -1 = no player selected
        }
    }

    public String[] getPlayers() { return players; }

    public String getErrorMessage() { return errorMessage; }

    public String getSuccessMessage() { return successMessage; }

    public int[] getPlayerIds() { return playerIds; }

    public void setPlayers(String[] players) { this.players = players; }

    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public void setSuccessMessage(String successMessage) { this.successMessage = successMessage; }

    public void setPlayerIds(int[] playerIds) { this.playerIds = playerIds; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OpenTeamEntryState{ players=[");
        if (players != null) {
            for (int i = 0; i < players.length; i++) {
                sb.append(players[i]);
                if (i < players.length - 1) sb.append(", ");
            }
        }
        sb.append("], errorMessage='").append(errorMessage).append("' }");
        return sb.toString();
    }
}
