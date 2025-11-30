package use_case.team_entry;

import entity.Team;

/**
 * Data Access Interface for storing and retrieving
 * the user's confirmed fantasy team.
 */
public interface TeamDataAccessInterface {
    /**
     * Saves the user's confirmed team.
     * @param team the team to save
     */
    void saveTeam(Team team);

    /**
     * Retrieves the user's confirmed team.
     * @return the saved team, or null if none exists
     */
    Team getTeam();

    /**
     * Clears saved team.
     */
    void clearTeam();
}
