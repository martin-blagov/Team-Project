package use_case.starting_lineup;

import entity.Team;

/**
 * Boundary for retrieving the user's saved team for building starting lineup.
 */
public interface StartingLineupTeamDataAccessInterface {
    /**
     * Retrieves the saved team or {@code null} if none exists.
     */
    Team getTeam();
}

