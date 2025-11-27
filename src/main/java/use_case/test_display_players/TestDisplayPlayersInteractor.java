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
            TestDisplayPlayersOutputBoundary presenter, PlayerDataAccessInterface playerDataAccess1, TestDisplayPlayersOutputBoundary presenter1) {

        this.playerDataAccess = playerDataAccess1;
        this.presenter = presenter1;
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
        try {
            // STEP 1: Fetch all players
            List<Player> allPlayers = new ArrayList<>(playerDataAccess.getAllPlayers());

            // STEP 2: Sort players by predicted points (highest first)
            allPlayers.sort((p1, p2) -> {
                double p1Pts;
                if (p1.getPredictedPoints() != null) {
                    p1Pts = p1.getPredictedPoints();
                } else {
                    p1Pts = 0.0;
                }

                double p2Pts;
                if (p2.getPredictedPoints() != null) {
                    p2Pts = p2.getPredictedPoints();
                } else {
                    p2Pts = 0.0;
                }

                return Double.compare(p2Pts, p1Pts);
            });

            // Get filters from input
            String searchText = inputData.getSearchText();
            String positionFilter = inputData.getPositionFilter();
            String teamFilter = inputData.getTeamFilter();
            Double maxPrice = inputData.getMaxPrice();

            // Normalize some values for comparisons
            String searchLower;
            if (searchText != null) {
                searchLower = searchText.trim().toLowerCase();
            } else {
                searchLower = "";
            }
            boolean hasSearch  = !searchLower.isEmpty();

            boolean filterPosition = positionFilter != null
                    && !positionFilter.equalsIgnoreCase("All positions");

            boolean filterTeam = teamFilter != null
                    && !teamFilter.equalsIgnoreCase("All teams");

            boolean filterPrice;
            if (maxPrice != null) {
                filterPrice = true;
            } else {
                filterPrice = false;
            }

            // STEP 3: Apply filters from inputData
            List<Player> filteredPlayers = allPlayers.stream()
                    .filter(player -> {
                        // Filter 1: Search text
                        if (hasSearch) {
                            String name = player.getWebName();
                            if (name == null ||
                                    !name.toLowerCase().contains(searchLower)) {
                                return false;
                            }
                        }

                        // Filter 2: Position
                        if (filterPosition) {
                            String playerPos = player.getPosition();
                            if (playerPos == null ||
                                    !playerPos.equalsIgnoreCase(positionFilter)) {
                                return false;
                            }
                        }

                        // Filter 3: Team
                        if (filterTeam) {
                            String teamName = player.getTeamName();
                            if (teamName == null ||
                                    !teamName.equalsIgnoreCase(teamFilter)) {
                                return false;
                            }
                        }

                        // Filter 4: Price
                        if (filterPrice && player.getNowCost() > maxPrice) {
                            return false;
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            // STEP 4: Extract available teams for dropdown
            List<String> availableTeams = filteredPlayers.stream()
                    .map(Player::getTeamName)                // STEP 1: Get all team names
                    .filter(name -> name != null && !name.trim().isEmpty()) // STEP 2: Remove nulls
                    .distinct()                              // STEP 3: Get unique values
                    .sorted(String::compareToIgnoreCase)     // STEP 4: Sort alphabetically
                    .collect(Collectors.toList());           // STEP 5: Collect to list

            // STEP 5: Create OutputData
            TestDisplayPlayersOutputData outputData =
                    new TestDisplayPlayersOutputData(filteredPlayers, availableTeams);

            // STEP 6: Pass to Presenter
            presenter.presentPlayers(outputData);

        } catch (Exception e) {
            // ERROR HANDLING:
            presenter.presentFailure(e.getMessage());
        }
    }

}