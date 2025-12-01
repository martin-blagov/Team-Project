package use_case.display_team;

import entity.Team;

/**
 * Data Access Interface for Display Team use case.
 * Read-only access to team data.
 */
public interface DisplayTeamDataAccessInterface {
    /**
     * Retrieves the user's team.
     * @return the saved team, or null if none exists
     */
    Team getTeam();
}