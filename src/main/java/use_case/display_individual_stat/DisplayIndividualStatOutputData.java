package use_case.display_individual_stat;

public class DisplayIndividualStatOutputData {
    private final String playerName;
    private final String playerPosition;
    private final String playerTeam;
    private final double playerCost;
    // private final String playerGoals;
    // private final String playerAssists;
    // private final String playerPoints;

    public DisplayIndividualStatOutputData(String playerName, String playerPosition,
                                           String playerTeam, double playerCost) {
        this.playerName = playerName;
        this.playerPosition = playerPosition;
        this.playerTeam = playerTeam;
        this.playerCost = playerCost;
        // this.playerGoals = playerGoals;
        // this.playerAssists = playerAssists;
        // this.playerPoints = playerPoints;
    }

    public String getPlayerName() { return playerName; }
    public String getPlayerPosition() { return playerPosition; }
    public String getPlayerTeam() { return playerTeam; }
    public double getPlayerCost() { return playerCost; }
    // public String getPlayerPoints() { return playerPoints; }
}
