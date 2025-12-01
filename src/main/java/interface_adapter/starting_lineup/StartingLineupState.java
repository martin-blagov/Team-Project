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

    public enum ViewMode {
        TABLE,
        GRAPHIC
    }

    private String statusMessage = "No lineup loaded.";
    private Team startingTeam;
    private List<Player> benchPlayers = new ArrayList<>();
    private ViewMode viewMode = ViewMode.TABLE;

    public StartingLineupState() {
    }

    public StartingLineupState(StartingLineupState other) {
        if (other == null) {
            return;
        }
        this.statusMessage = other.statusMessage;
        this.startingTeam = other.startingTeam;
        this.benchPlayers = new ArrayList<>(other.benchPlayers);
        this.viewMode = other.viewMode;
    }

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

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode == null ? ViewMode.TABLE : viewMode;
    }
}
