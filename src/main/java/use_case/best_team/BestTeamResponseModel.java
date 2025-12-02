package use_case.best_team;

import entity.Player;
import entity.Team;

import java.util.List;

public class BestTeamResponseModel {
    private final Team team;
    private final List<Player> players;
    private final double totalCost;
    private final double totalPredictedPoints;

    public BestTeamResponseModel(Team team,
                                 List<Player> players,
                                 double totalCost,
                                 double totalPredictedPoints) {
        this.team = team;
        this.players = players;
        this.totalCost = totalCost;
        this.totalPredictedPoints = totalPredictedPoints;
    }

    public Team getTeam() {
        return team;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getTotalPredictedPoints() {
        return totalPredictedPoints;
    }
}
