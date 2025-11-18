package use_case.starting_lineup;

import entity.Player;
import entity.Team;

import java.util.Collections;
import java.util.List;

public class StartingLineupOutputData {

    private final Team startingTeam;
    private final List<Player> benchPlayers;

    public StartingLineupOutputData(Team startingTeam, List<Player> benchPlayers) {
        this.startingTeam = startingTeam;
        this.benchPlayers = benchPlayers;
    }

    public Team getStartingTeam() {
        return startingTeam;
    }

    public List<Player> getBenchPlayers() {
        return Collections.unmodifiableList(benchPlayers);
    }
}
