package interface_adapter.test_display_players;

import entity.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * DEMO State for Test Display Players use case.
 *
 * Contains all the data needed to display the filtered player list.
 */
public class TestDisplayPlayersState {

    private List<Player> players;
    private List<String> availableTeams;
    private String errorMessage;

    /**
     * Create a new empty state.
     */
    public TestDisplayPlayersState() {
        this.players = new ArrayList<>();
        this.availableTeams = new ArrayList<>();
        this.errorMessage = null;
    }

    // ========== GETTERS ==========

    /**
     * Get the list of filtered players to display.
     * Returns a copy to prevent external modification.
     *
     * @return List of players
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Get the list of available teams for the dropdown filter.
     * Returns a copy to prevent external modification.
     *
     * @return List of team names
     */
    public List<String> getAvailableTeams() {
        return new ArrayList<>(availableTeams);
    }

    /**
     * Get the error message (if any).
     *
     * @return Error message, or null if no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // ========== SETTERS ==========

    /**
     * Set the list of players to display.
     * Makes a defensive copy.
     *
     * @param players List of filtered players
     */
    public void setPlayers(List<Player> players) {
        if (players != null) {
            this.players = new ArrayList<>(players);
        } else {
            this.players = new ArrayList<>();
        }
    }

    /**
     * Set the list of available teams.
     * Makes a defensive copy.
     *
     * @param availableTeams List of team names
     */
    public void setAvailableTeams(List<String> availableTeams) {
        if (availableTeams != null) {
            this.availableTeams = new ArrayList<>(availableTeams);
        } else {
            this.availableTeams = new ArrayList<>();
        }
    }

    /**
     * Set the error message.
     *
     * @param errorMessage Error message, or null to clear
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}