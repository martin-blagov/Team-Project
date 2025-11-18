package interface_adapter.starting_lineup;

import entity.Player;
import entity.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * State for the starting lineup view.
 */
public class StartingLineupState {

    private String statusMessage = "No lineup loaded.";
    private Team startingTeam;
    private List<Player> benchPlayers = new ArrayList<>();

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Team getStartingTeam() {
        return startingTeam;
    }

    public void setStartingTeam(Team startingTeam) {
        this.startingTeam = startingTeam;
    }

    public List<Player> getBenchPlayers() {
        return Collections.unmodifiableList(benchPlayers);
    }

    public void setBenchPlayers(List<Player> benchPlayers) {
        this.benchPlayers = benchPlayers == null ? new ArrayList<>() : new ArrayList<>(benchPlayers);
    }
}