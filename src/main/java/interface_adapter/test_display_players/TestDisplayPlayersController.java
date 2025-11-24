package interface_adapter.test_display_players;

import use_case.test_display_players.TestDisplayPlayersInputBoundary;
import use_case.test_display_players.TestDisplayPlayersInputData;

/**
 * DEMO Controller for Test Display Players use case.
 *
 * This is a TEMPLATE to show how to create a Controller.
 * Each use case should have its own Controller following this pattern.
 *
 * RESPONSIBILITIES:
 * 1. Receive raw input from the View (strings, numbers, events)
 * 2. Package input into InputData objects
 * 3. Call the Interactor via the Input Boundary
 */
public class TestDisplayPlayersController {

    // Dependency: Input Boundary (interface), NOT the concrete Interactor
    private final TestDisplayPlayersInputBoundary interactor;

    /**
     * Constructor
     * @param interactor The use case interactor (implements Input Boundary)
     */
    public TestDisplayPlayersController(TestDisplayPlayersInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Filter players based on user's criteria.
     * @param searchText What the user typed in the search box (can be empty string)
     * @param positionFilter What position the user selected ("All positions", "Goalkeeper", etc.)
     * @param teamFilter What team the user selected ("All teams", "Arsenal", etc.)
     * @param maxPrice Maximum price filter (null if "Any price" selected)
     */
    public void filterPlayers(String searchText,
                              String positionFilter,
                              String teamFilter,
                              Double maxPrice) {

        // Step 1: Package the raw inputs into an InputData object
        TestDisplayPlayersInputData inputData = new TestDisplayPlayersInputData(
                searchText,
                positionFilter,
                teamFilter,
                maxPrice
        );

        // Step 2: Call the Interactor
        // The Interactor will:
        // - Fetch data
        // - Sort data
        // - Filter data
        // - Call the Presenter with results
        interactor.execute(inputData);
        
    }

    /**
     * Load all players without any filters.
     *
     * This can be called when the view first opens,
     * or when the user clicks a "Reset Filters" button.
     *
     * It's just filterPlayers() with default values.
     */
    public void loadAllPlayers() {
        filterPlayers("", "All positions", "All teams", null);
    }
}