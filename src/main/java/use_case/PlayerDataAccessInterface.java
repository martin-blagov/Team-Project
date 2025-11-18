package use_case;

import entity.Player;
import java.util.List;

/**
 * Interface for accessing player data stored in memory.
 * This allows use cases to query and store player information.
 */
public interface PlayerDataAccessInterface {

    /**
     * Save all players to storage.
     * @param players List of all players to save
     */
    void saveAll(List<Player> players);

    /**
     * Get all players.
     * @return List of all players
     */
    List<Player> getAllPlayers();

    /**
     * Get a specific player by ID.
     * @param id Player ID
     * @return Player with the given ID, or null if not found
     */
    Player getPlayerById(int id);

    /**
     * Get all players of a specific position.
     * @param position Position type (1=GK, 2=DEF, 3=MID, 4=FWD)
     * @return List of players in that position
     */
    List<Player> getPlayersByPosition(int position);

    /**
     * Get all players from a specific team.
     * @param teamName Team name (case-insensitive, partial match allowed)
     * @return List of players in that team
     */
    List<Player> getPlayersByTeam(String teamName);

    /**
     * Get top N players by position, sorted by predicted points (highest first).
     * @param position Position type (1=GK, 2=DEF, 3=MID, 4=FWD)
     * @param limit Maximum number of players to return (use -1 for no limit, returns all)
     * @return List of top players, sorted by predicted points descending
     */
    List<Player> getTopPlayersByPosition(int position, int limit);

    /**
     * Get top N players overall, sorted by predicted points (highest first).
     * @param limit Maximum number of players to return (use -1 for no limit, returns all)
     * @return List of top players, sorted by predicted points descending
     */
    List<Player> getTopPlayers(int limit);

    /**
     * Get players by name (case-insensitive search).
     * Returns all players whose web_name contains the search string.
     * @param name Name to search for (case-insensitive, partial match allowed)
     * @return List of players matching the name (may be empty, may have duplicates)
     */
    List<Player> getPlayersByName(String name);
}