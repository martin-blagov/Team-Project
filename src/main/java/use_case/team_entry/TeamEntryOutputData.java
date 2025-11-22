package use_case.team_entry;

/**
 * Output Data for the Open Team Entry Output Data Use Case.
 */
public class TeamEntryOutputData {
    private final String[] players;

    public TeamEntryOutputData(String[] players) {
        this.players = players;
    }

    public String[] getPlayers() {
        return players;
    }
}
