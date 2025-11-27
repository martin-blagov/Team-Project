package use_case.test_display_players;

import entity.Player;
import use_case.PlayerDataAccessInterface;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * DEMO Interactor for Test Display Players use case.
 *
 * This is a TEMPLATE to show teammates how to create an Interactor.
 * Each use case should have its own Interactor following this pattern.
 *
 * The Interactor contains the BUSINESS LOGIC for the use case.
 *
 * RESPONSIBILITIES:
 * 1. Receive InputData from Controller (via Input Boundary)
 * 2. Fetch data from Data Access layer
 * 3. Apply business rules (sorting, filtering, validation)
 * 4. Create OutputData with results
 * 5. Pass OutputData to Presenter (via Output Boundary)
 */
public class TestDisplayPlayersInteractor implements TestDisplayPlayersInputBoundary {

    // Dependencies (injected via constructor)
    private final PlayerDataAccessInterface playerDataAccess;
    private final TestDisplayPlayersOutputBoundary presenter;

    /**
     * Constructor
     * @param playerDataAccess The data access object to fetch players
     * @param presenter The presenter to send results to
     */
    public TestDisplayPlayersInteractor(
            PlayerDataAccessInterface playerDataAccess,
            TestDisplayPlayersOutputBoundary presenter) {

        this.playerDataAccess = playerDataAccess;
        this.presenter = presenter;
    }

    /**
     * Execute the use case.
     *
     * IMPLEMENT:
     *
     * STEP 1: Fetch all players
     * STEP 2: Sort players by predicted points (highest first)
     * STEP 3: Apply filters from inputData
     *
     *      Filter 1: Search text
     *        - If inputData.getSearchText() is not empty
     *        - Keep only players whose webName contains the search text (case-insensitive)
     *
     *      Filter 2: Position
     *        - If inputData.getPositionFilter() is NOT "All positions"
     *        - Keep only players whose position matches (case-insensitive)
     *
     *      Filter 3: Team
     *        - If inputData.getTeamFilter() is NOT "All teams"
     *        - Keep only players whose team name matches exactly
     *
     *      Filter 4: Price
     *        - If inputData.getMaxPrice() is not null
     *        - Keep only players whose nowCost <= maxPrice
     *
     * STEP 4: Extract available teams for dropdown
     *      * STEP 1: Get all team names
     *      * STEP 2: Remove nulls
     *      * - Some players might have null team names
     *      * STEP 3: Get unique values
     *      * STEP 4: Sort alphabetically
     *      * STEP 5: Collect to list
     *
     * STEP 5: Create OutputData
     * - new TestDisplayPlayersOutputData(filteredPlayers, availableTeams)
     *
     * STEP 6: Pass to Presenter
     * - Call presenter.presentPlayers(outputData)
     *
     * ERROR HANDLING:
     * - Wrap everything in try-catch
     * - If exception occurs, call presenter.presentFailure(error message)
     *
     * @param inputData Contains filter criteria from user
     */
    @Override
    public void execute(TestDisplayPlayersInputData inputData) {
        // TODO: Implement the 6 steps above
        // TODO: Add try-catch for error handling
    }

}