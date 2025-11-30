package interface_adapter.team_entry;

public class TeamEntryState {

    private static final int TEAM_SIZE = 15;
    private static final int NO_PLAYER_ID = -1;

    private int[] playerIds;
    private String[] players;
    private String errorMessage;
    private String successMessage;
    private String budget;

    /**
     * Initializes the state with empty player fields.
     */
    public TeamEntryState() {
        this.players = new String[TEAM_SIZE];
        this.playerIds = new int[TEAM_SIZE];

        for (int i = 0; i < players.length; i++) {
            players[i] = "";
            playerIds[i] = NO_PLAYER_ID;
        }
    }

    public String[] getPlayers() {
        return players;
    }

    public int[] getPlayerIds() {
        return playerIds;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getBudget() {
        return budget;
    }

    public void setPlayers(String[] players) {
        this.players = players;
    }

    public void setPlayerIds(int[] playerIds) {
        this.playerIds = playerIds;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TeamEntryState{ players=[");

        if (players != null) {
            for (int i = 0; i < players.length; i++) {
                sb.append(players[i]);

                if (i < players.length - 1) {
                    sb.append(", ");
                }
            }
        }

        sb.append("], errorMessage='")
                .append(errorMessage)
                .append("' }");

        return sb.toString();
    }
}
