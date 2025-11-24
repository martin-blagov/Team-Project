package use_case.test_display_players;

import entity.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * DEMO Output Data for Test Display Players use case.
 *
 * This is a TEMPLATE to show teammates how to create Output Data.
 * Each use case should have its own Output Data class following this pattern.
 *
 * Contains the results after filtering/sorting players.
 */
public class TestDisplayPlayersOutputData {
    private final List<Player> filteredPlayers;
    private final List<String> availableTeams;

    /**
     * Create output data with filtered results.
     *
     * @param filteredPlayers List of players after applying filters and sorting
     * @param availableTeams List of team names for the team filter dropdown
     */
    public TestDisplayPlayersOutputData(List<Player> filteredPlayers,
                                        List<String> availableTeams) {
        // Make defensive copies to prevent external modification
        this.filteredPlayers = new ArrayList<>(filteredPlayers);
        this.availableTeams = new ArrayList<>(availableTeams);
    }

    /**
     * Get the filtered list of players.
     * Returns a copy to prevent external modification.
     *
     * @return List of filtered players
     */
    public List<Player> getFilteredPlayers() {
        return new ArrayList<>(filteredPlayers);
    }

    /**
     * Get the list of available teams for filtering.
     * Returns a copy to prevent external modification.
     *
     * @return List of team names
     */
    public List<String> getAvailableTeams() {
        return new ArrayList<>(availableTeams);
    }
}