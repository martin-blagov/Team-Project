package use_case.display_individual_stat;

import entity.Player;

public class DisplayIndividualStatInputData {
    private final int playerID;

    public DisplayIndividualStatInputData(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return playerID;
    }
}
