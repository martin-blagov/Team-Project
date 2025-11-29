package use_case.starting_lineup;

import entity.Player;
import entity.Team;

import java.util.Collections;
import java.util.List;

/**
 * Data output object for starting lineup use case.
 */
public class StartingLineupOutputData {

    private final Team startingTeam;
    private final List<Player> benchPlayers;

    public StartingLineupOutputData(Team startingTeam, List<Player> benchPlayers) {
        this.startingTeam = startingTeam;
        this.benchPlayers = benchPlayers;
    }

    /**
     * @return the computed starting lineup.
     */
    public Team getStartingTeam() {
        return startingTeam;
    }

    /**
     * @return an unmodifiable view of the bench players list.
     */
    public List<Player> getBenchPlayers() {
        return Collections.unmodifiableList(benchPlayers);
    }
}
