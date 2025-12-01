package use_case.transfer_suggestions;

import entity.Team;

/**
 * Data Access Interface for Transfer Suggestions use case.
 * Read-only access to team data.
 */
public interface TransferSuggestionsTeamDataAccessInterface {
    /**
     * Retrieves the user's current team.
     * @return the saved team, or null if none exists
     */
    Team getTeam();
}