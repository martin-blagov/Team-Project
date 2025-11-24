package use_case.best_team;

import entity.Player;
import java.util.List;

public class BestTeamResponseModel {
    private final List<Player> players;
    private final double totalCost;
    private final double totalPredictedPoints;

    public BestTeamResponseModel(List<Player> players, double totalCost, double totalPredictedPoints) {
        this.players = players;
        this.totalCost = totalCost;
        this.totalPredictedPoints = totalPredictedPoints;
    }

    public List<Player> getPlayers() {return players;}
    public double getTotalCost() {return totalCost;}
    public double getTotalPredictedPoints() {return totalPredictedPoints;}
}
