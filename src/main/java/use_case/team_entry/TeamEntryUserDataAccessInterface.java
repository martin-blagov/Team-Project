package use_case.team_entry;

import entity.Player;
import java.util.List;

/**
 * Data Access Interface for retrieving player information
 * during the team entry use case.
 */
public interface TeamEntryUserDataAccessInterface {

    /**
     * Returns a list of all available players.
     * @return list of all players
     */
    List<Player> getAllPlayers();

    /**
     * Returns a player by their name.
     * @param name the player's name
     * @return the matching Player, or null if none found
     */
    Player getPlayerByName(String name);
}
