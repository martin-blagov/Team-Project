package use_case.test_display_players;

/**
 * DEMO Input Data for Test Display Players use case.
 *
 * This is a TEMPLATE to show teammates how to create Input Data.
 * Each use case should have its own Input Data class following this pattern.
 *
 * Contains filter criteria from the user.
 */
public class TestDisplayPlayersInputData {
    private final String searchText;
    private final String positionFilter;  // "All positions", "Goalkeeper", etc.
    private final String teamFilter;      // "All teams", "Arsenal", etc.
    private final Double maxPrice;        // null means no price filter

    /**
     * Create input data with all filter criteria.
     *
     * @param searchText Text to search in player names (empty string for no search)
     * @param positionFilter Position filter ("All positions" for no filter)
     * @param teamFilter Team filter ("All teams" for no filter)
     * @param maxPrice Maximum price (null for no price filter)
     */
    public TestDisplayPlayersInputData(String searchText,
                                       String positionFilter,
                                       String teamFilter,
                                       Double maxPrice) {
        this.searchText = searchText != null ? searchText : "";
        this.positionFilter = positionFilter != null ? positionFilter : "All positions";
        this.teamFilter = teamFilter != null ? teamFilter : "All teams";
        this.maxPrice = maxPrice;
    }

    // Getters
    public String getSearchText() {
        return searchText;
    }

    public String getPositionFilter() {
        return positionFilter;
    }

    public String getTeamFilter() {
        return teamFilter;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }
}
