package interface_adapter.display_team;

import entity.Team;

/**
 * State for Display Team use case.
 * Contains the team data and any error messages.
 */
public class DisplayTeamState {

    private Team team;
    private String errorMessage;

    /**
     * Create a new empty state.
     */
    public DisplayTeamState() {
        this.team = null;
        this.errorMessage = null;
    }

    // ========== GETTERS ==========

    /**
     * Get the team to display.
     * May be null if not loaded yet, or may contain null players for empty slots.
     * @return The team
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Get the error message (if any).
     * @return Error message, or null if no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // ========== SETTERS ==========

    /**
     * Set the team to display.
     * @param team The team (can be null, can contain null players)
     */
    public void setTeam(Team team) {
        this.team = team;
    }

    /**
     * Set the error message.
     * @param errorMessage Error message, or null to clear
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}