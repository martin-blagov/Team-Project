package interface_adapter.display_individual_stat;

/**
 * The state for the Individual Stats.
 */
public class DisplayIndividualStatState {
    private String playerName;
    private String playerPosition;
    private String playerTeam;
    private String playerCost;
    private String playerGoals;
    private String playerAssists;
    private String playerPoints;

    public String getPlayerName() { return playerName; }

    public String getPlayerPosition() { return playerPosition; }

    public String getPlayerTeam() { return playerTeam; }

    public String getPlayerCost() { return playerCost; }

    public String getPlayerGoals() { return playerGoals; }

    public String getPlayerAssists() { return playerAssists; }

    public String getPlayerPoints() { return playerPoints; }

    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public void setPlayerPosition(String playerPosition) { this.playerPosition = playerPosition; }

    public void setPlayerTeam(String playerTeam) { this.playerTeam = playerTeam; }

    public void setPlayerCost(String playerCost) { this.playerCost = playerCost; }

    public void setPlayerGoals(String playerGoals) { this.playerGoals = playerGoals; }

    public void setPlayerAssists(String playerAssists) { this.playerAssists = playerAssists; }

    public void setPlayerPoints(String playerPoints) { this.playerPoints = playerPoints; }
}
