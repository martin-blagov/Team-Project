package use_case.starting_lineup;

import java.util.Collections;
import java.util.List;

import entity.Player;
import entity.Team;

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
     * Get the starting team.
     *
     * @return the computed starting lineup.
     */
    public Team getStartingTeam() {
        return startingTeam;
    }

    /**
     * Gets the bench players.
     *
     * @return an unmodifiable view of the bench players list.
     */
    public List<Player> getBenchPlayers() {
        return Collections.unmodifiableList(benchPlayers);
    }
}
