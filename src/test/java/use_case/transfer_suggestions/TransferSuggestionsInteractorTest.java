package use_case.transfer_suggestions;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import use_case.PlayerDataAccessInterface;
import use_case.transfer_suggestions.TransferSuggestionsTeamDataAccessInterface;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for TransferSuggestionsInteractor.
 * Building this up step by step.
 */
class TransferSuggestionsInteractorTest {

    // The three things the interactor needs
    private TransferSuggestionsTeamDataAccessInterface mockTeamDataAccess;
    private PlayerDataAccessInterface mockPlayerDataAccess;
    private TransferSuggestionsOutputBoundary mockPresenter;

    // The thing we're testing
    private TransferSuggestionsInteractor interactor;

    @BeforeEach
    void setUp() {
        // Create mocks - these are fake versions of the real objects
        mockTeamDataAccess = mock(TransferSuggestionsTeamDataAccessInterface.class);
        mockPlayerDataAccess = mock(PlayerDataAccessInterface.class);
        mockPresenter = mock(TransferSuggestionsOutputBoundary.class);

        // Create the interactor with our mocks
        interactor = new TransferSuggestionsInteractor(
                mockTeamDataAccess,
                mockPlayerDataAccess,
                mockPresenter
        );
    }

    // ========== VALIDATION TESTS ==========

    /**
     * TEST 1: What happens when there's no team?
     * Expected: Should call presentFailure with error message
     */
    @Test
    void testNoTeam_CallsPresentFailure() {
        // ARRANGE: Set up the scenario
        // When someone asks for the team, return null (no team exists)
        when(mockTeamDataAccess.getTeam()).thenReturn(null);

        // Create input asking for 2 transfers
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(2);

        // ACT: Run the use case
        interactor.execute(inputData);

        // ASSERT: Check what happened
        // Should have called presentFailure with this exact message
        verify(mockPresenter).presentFailure("No team found. Please create a team first.");

        // Should NOT have called presentSuccess
        verify(mockPresenter, never()).presentSuccess(any());
    }

    /**
     * TEST 2: What happens when number of transfers is negative?
     * Expected: Should call presentFailure
     */
    @Test
    void testNegativeTransfers_CallsPresentFailure() {
        // ARRANGE: Need a valid team (even though we won't get past validation)
        Team dummyTeam = createSimpleTeam(15);
        when(mockTeamDataAccess.getTeam()).thenReturn(dummyTeam);

        // Input with negative transfers
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(-1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Note: actual validation allows 0, so message says "between 0 and 15"
        verify(mockPresenter).presentFailure("Number of transfers must be between 0 and 15.");
        verify(mockPresenter, never()).presentSuccess(any());
    }

    /**
     * TEST 3: What happens when number of transfers is too high (>15)?
     * Expected: Should call presentFailure
     */
    @Test
    void testTooManyTransfers_CallsPresentFailure() {
        // ARRANGE
        Team dummyTeam = createSimpleTeam(15);
        when(mockTeamDataAccess.getTeam()).thenReturn(dummyTeam);

        // Input with way too many transfers
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(20);

        // ACT
        interactor.execute(inputData);

        // ASSERT
        verify(mockPresenter).presentFailure("Number of transfers must be between 0 and 15.");
        verify(mockPresenter, never()).presentSuccess(any());
    }

    /**
     * TEST 4: What happens when you ask for more transfers than players in team?
     * Expected: Should call presentFailure
     */
    @Test
    void testMoreTransfersThanPlayers_CallsPresentFailure() {
        // ARRANGE: Team with only 3 players
        Team smallTeam = createSimpleTeam(3);
        when(mockTeamDataAccess.getTeam()).thenReturn(smallTeam);

        // Asking for 5 transfers but only 3 players exist
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(5);

        // ACT
        interactor.execute(inputData);

        // ASSERT
        verify(mockPresenter).presentFailure("Cannot transfer more players than you have in your team.");
        verify(mockPresenter, never()).presentSuccess(any());
    }

    /**
     * TEST 5: What happens when 0 transfers requested?
     * Expected: Should return the original team unchanged (this is valid!)
     */
    @Test
    void testZeroTransfers_ReturnsOriginalTeam() {
        // ARRANGE
        Team originalTeam = createSimpleTeam(15);
        when(mockTeamDataAccess.getTeam()).thenReturn(originalTeam);

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(0);

        // ACT
        interactor.execute(inputData);

        // ASSERT
        // Should call presentSuccess (0 transfers is valid!)
        verify(mockPresenter).presentSuccess(any(TransferSuggestionsOutputData.class));
        verify(mockPresenter, never()).presentFailure(any());
    }

    // ========== SUCCESS CASE TESTS ==========

    /**
     * TEST 6: Simple success - transfer out 1 bad player for 1 good player
     *
     * Scenario:
     * - Team has a bad goalkeeper (2.0 pts predicted)
     * - Better goalkeeper available (6.0 pts predicted, same price)
     * - Should suggest swapping them
     */
    @Test
    void testOneTransfer_SimpleSuccess() {
        // ARRANGE: Build a realistic scenario

        // Create a team with one bad GK and some decent other players
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // The bad GK we want to replace
        Player badGK = createPlayerWithPoints(1, "BadKeeper", 1, 5.0, 2.0, "Arsenal");
        teamPlayers.add(badGK);

        // Add other decent players to fill the team (need 15 total)
        teamPlayers.add(createPlayerWithPoints(2, "GoodGK", 1, 5.0, 5.0, "Liverpool"));

        // Add defenders - each from different teams to avoid club constraint
        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 5.0, "Team" + i));
        }

        // Add midfielders - each from different teams
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }

        // Add forwards - each from different teams
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 6.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true);

        // Mock: When asked for team, return our team
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Create the better GK that's available
        Player betterGK = createPlayerWithPoints(100, "BetterKeeper", 1, 5.0, 6.0, "Man City");

        // Mock getPlayersByPosition for percentile calculation (returns all GKs including team's)
        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(badGK);  // The one in our team
        allGKs.add(teamPlayers.get(1)); // The other GK in our team
        allGKs.add(betterGK);  // The better option
        allGKs.add(createPlayerWithPoints(101, "OtherGK", 1, 5.0, 4.0, "Chelsea"));
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        // Mock for other positions (just return empty or the team players)
        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(
                teamPlayers.subList(2, 7)  // Defenders
        );
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(
                teamPlayers.subList(7, 12)  // Midfielders
        );
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(
                teamPlayers.subList(12, 15)  // Forwards
        );

        // Mock getAllPlayers for price percentiles
        java.util.List<Player> allPlayers = new java.util.ArrayList<>(teamPlayers);
        allPlayers.add(betterGK);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        // Mock getTopPlayersByPosition - this is what finds the replacements
        // Should return players sorted by predicted points (best first)
        java.util.List<Player> topGKs = new java.util.ArrayList<>();
        topGKs.add(betterGK);  // 6.0 pts - best
        topGKs.add(teamPlayers.get(1));  // 5.0 pts
        topGKs.add(createPlayerWithPoints(101, "OtherGK", 1, 5.0, 4.0, "Chelsea"));  // 4.0 pts
        topGKs.add(badGK);  // 2.0 pts - worst
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(topGKs);

        // For other positions, return empty (we're only transferring GKs)
        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT
        // Should call presentSuccess
        ArgumentCaptor<TransferSuggestionsOutputData> captor =
                ArgumentCaptor.forClass(TransferSuggestionsOutputData.class);
        verify(mockPresenter).presentSuccess(captor.capture());
        verify(mockPresenter, never()).presentFailure(any());

        // Check the output data
        TransferSuggestionsOutputData output = captor.getValue();
        assertNotNull(output);
        assertEquals(1, output.getSwaps().size(), "Should suggest exactly 1 swap");

        // Check that it's swapping the bad GK for the better one
        TransferSuggestionsOutputData.PlayerSwap swap = output.getSwaps().get(0);
        assertEquals(badGK.getId(), swap.getPlayerOut().getId(), "Should transfer out the bad GK");
        assertEquals(betterGK.getId(), swap.getPlayerIn().getId(), "Should transfer in the better GK");

        // Check points improvement
        assertTrue(swap.getPointsImprovement() > 0, "Should improve predicted points");
        assertEquals(4.0, swap.getPointsImprovement(), 0.01, "Should improve by 4 points (6.0 - 2.0)");
    }

    /**
     * TEST 7: Exception handling
     * When data access throws exception, should catch and present failure
     *
     * COVERS: catch (Exception e) block in execute()
     */
    @Test
    void testException_CaughtAndPresentedAsFailure() {
        // ARRANGE: Mock throws exception
        when(mockTeamDataAccess.getTeam()).thenThrow(new RuntimeException("Database error!"));

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT
        verify(mockPresenter).presentFailure("Error generating transfer suggestions: Database error!");
        verify(mockPresenter, never()).presentSuccess(any());
    }

    /**
     * TEST 8: No valid transfers found - budget constraint
     * All replacement options are too expensive, algorithm tries all k values
     *
     * COVERS: "No valid transfers found" presentFailure line and budget constraint checking
     */
    @Test
    void testNoValidTransfers_AllTooExpensive() {
        // ARRANGE: ONE bad GK, and MANY expensive replacements (forces MAX_K iterations)
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // ONE truly bad GK (1.0 pts)
        Player badGK = createPlayerWithPoints(1, "BadGK", 1, 5.0, 1.0, "Arsenal");
        teamPlayers.add(badGK);

        // Second GK is mediocre (4.0 pts) - not great but better than bad one
        Player okGK = createPlayerWithPoints(2, "OkayGK", 1, 5.0, 4.0, "Liverpool");
        teamPlayers.add(okGK);

        // All other players are GOOD - won't be flagged for replacement
        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 7.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 8.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 7.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 0.5f, true); // Only 0.5m budget!
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Create 15 expensive GKs - forces algorithm to try k=1,2,3...up to MAX_K=10
        // ALL are too expensive for our 0.5m budget
        java.util.List<Player> expensiveGKs = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Player expensiveGK = createPlayerWithPoints(100 + i, "ExpensiveGK" + i, 1,
                    15.0 + i, 9.0 + (i * 0.1), "Team" + (100 + i));
            expensiveGKs.add(expensiveGK);
        }

        // Mock GKs for percentile calculation
        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(badGK);
        allGKs.add(okGK);
        allGKs.addAll(expensiveGKs);
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));

        java.util.List<Player> allPlayers = new java.util.ArrayList<>(teamPlayers);
        allPlayers.addAll(expensiveGKs);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        // Top GKs: All expensive ones first (sorted by points), then ok GK, then bad GK
        java.util.List<Player> topGKs = new java.util.ArrayList<>(expensiveGKs);
        topGKs.add(okGK);  // Already in team
        topGKs.add(badGK);  // Being replaced
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(topGKs);

        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - All options too expensive, even with MAX_K attempts
        verify(mockPresenter).presentFailure("No valid transfers found within budget and club constraints.");
        verify(mockPresenter, never()).presentSuccess(any());
    }

    /**
     * TEST 9: Single player in position gets 50th percentile
     * Tests the edge case where only 1 player exists for percentile calculation
     *
     * COVERS: percentile = 50.0 lines in both calculatePerformancePercentiles and calculatePricePercentiles
     */
    @Test
    void testSinglePlayerPercentile_Gets50Percent() {
        // ARRANGE: Normal formation but only 1 forward has >0 points for percentile calc
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // 2 GKs
        teamPlayers.add(createPlayerWithPoints(1, "GK1", 1, 5.0, 5.0, "Arsenal"));
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.0, "Liverpool"));

        // 5 Defenders - all good
        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 6.0, "Team" + i));
        }

        // 5 Midfielders - all good
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }

        // 3 Forwards: only 1 has non-zero points, others have 0
        Player onlyGoodFwd = createPlayerWithPoints(13, "GoodFwd", 4, 7.0, 6.0, "Chelsea");
        teamPlayers.add(onlyGoodFwd);

        // These 2 forwards have 0 points but are in team (will be included via teamPlayerIds)
        Player zeroFwd1 = createPlayerWithPoints(14, "ZeroFwd1", 4, 7.0, 0.0, "Man City");
        Player zeroFwd2 = createPlayerWithPoints(15, "ZeroFwd2", 4, 7.0, 0.0, "Tottenham");
        teamPlayers.add(zeroFwd1);
        teamPlayers.add(zeroFwd2);

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Mock forwards: team forwards + external 0-point forwards
        // Only onlyGoodFwd has >0 points, but zero forwards are in team so included
        // Result: eligiblePlayers will have onlyGoodFwd + the 2 zero forwards (in team)
        // For percentile, only onlyGoodFwd has >0 points
        java.util.List<Player> allFwds = new java.util.ArrayList<>();
        allFwds.add(onlyGoodFwd); // Has points
        allFwds.add(zeroFwd1); // 0 pts but in team
        allFwds.add(zeroFwd2); // 0 pts but in team
        allFwds.add(createPlayerWithPoints(50, "ExtFwd1", 4, 7.0, 0.0, "Brighton")); // 0 pts, NOT in team -> excluded
        allFwds.add(createPlayerWithPoints(51, "ExtFwd2", 4, 7.0, 0.0, "Villa")); // 0 pts, NOT in team -> excluded

        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(teamPlayers.subList(0, 2));
        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(allFwds);

        // For price percentiles - team players only
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(teamPlayers);

        // Top forwards - better one available, same price so affordable
        Player betterFwd = createPlayerWithPoints(100, "SuperFwd", 4, 7.0, 8.0, "Newcastle");
        java.util.List<Player> topFwds = new java.util.ArrayList<>();
        topFwds.add(betterFwd);
        topFwds.add(onlyGoodFwd);
        topFwds.add(zeroFwd1);
        topFwds.add(zeroFwd2);
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(topFwds);

        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Should successfully handle and suggest swapping a 0-point forward
        ArgumentCaptor<TransferSuggestionsOutputData> captor = ArgumentCaptor.forClass(TransferSuggestionsOutputData.class);
        verify(mockPresenter).presentSuccess(captor.capture());

        TransferSuggestionsOutputData outputData = captor.getValue();
        assertEquals(1, outputData.getSwaps().size(), "Should suggest 1 swap");
    }

    /**
     * TEST 10: Club constraint blocks transfer
     * Team has 3 Liverpool players, all replacements are Liverpool (forces MAX_K iterations)
     *
     * COVERS: return true in hasClubViolation (when club exceeds limit)
     */
    @Test
    void testClubConstraintViolation_BlocksTransfer() {
        // ARRANGE: ONE bad GK, 3 Liverpool players at limit, MANY Liverpool GK replacements
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // ONE truly bad GK (1.0 pts)
        Player badGK = createPlayerWithPoints(1, "BadGK", 1, 5.0, 1.0, "Arsenal");
        teamPlayers.add(badGK);

        // Second GK is mediocre (4.0 pts)
        Player okGK = createPlayerWithPoints(2, "OkayGK", 1, 5.0, 4.0, "Chelsea");
        teamPlayers.add(okGK);

        // 3 Liverpool players (at maximum!) - all GOOD so won't be replaced
        teamPlayers.add(createPlayerWithPoints(3, "LivDef1", 2, 5.0, 7.0, "Liverpool"));
        teamPlayers.add(createPlayerWithPoints(4, "LivDef2", 2, 5.0, 7.0, "Liverpool"));
        teamPlayers.add(createPlayerWithPoints(5, "LivMid1", 3, 6.0, 8.0, "Liverpool"));

        // Rest are all GOOD players
        teamPlayers.add(createPlayerWithPoints(6, "Def3", 2, 5.0, 7.0, "Man City"));
        teamPlayers.add(createPlayerWithPoints(7, "Def4", 2, 5.0, 7.0, "Chelsea"));
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 8.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 7.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true); // Good budget
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Create 15 Liverpool GKs - forces algorithm to try k=1,2,3...up to MAX_K=10
        // ALL are Liverpool, so ALL would violate club constraint (make 4th Liverpool player)
        java.util.List<Player> liverpoolGKs = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Player livGK = createPlayerWithPoints(100 + i, "LiverpoolGK" + i, 1,
                    5.0 + (i * 0.1), 9.0 + (i * 0.1), "Liverpool");
            liverpoolGKs.add(livGK);
        }

        // Mock GKs for percentile calculation
        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(badGK);
        allGKs.add(okGK);
        allGKs.addAll(liverpoolGKs);
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));

        java.util.List<Player> allPlayers = new java.util.ArrayList<>(teamPlayers);
        allPlayers.addAll(liverpoolGKs);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        // Top GKs: All Liverpool ones first (sorted by points), then ok GK, then bad GK
        java.util.List<Player> topGKs = new java.util.ArrayList<>(liverpoolGKs);
        topGKs.add(okGK);  // Already in team
        topGKs.add(badGK);  // Being replaced
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(topGKs);

        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - All Liverpool options violate club constraint, even with MAX_K attempts
        verify(mockPresenter).presentFailure("No valid transfers found within budget and club constraints.");
        verify(mockPresenter, never()).presentSuccess(any());
    }

    /**
     * TEST 11: openPage() method
     * Tests the openPage use case which loads the current team
     *
     * COVERS: openPage() method and enrichTeamWithFullPlayerData
     */
    @Test
    void testOpenPage_LoadsTeamSuccessfully() {
        // ARRANGE
        Team testTeam = createSimpleTeam(15);
        when(mockTeamDataAccess.getTeam()).thenReturn(testTeam);

        // Mock getAllPlayers to return players for enrichment
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(testTeam.getPlayers());

        // ACT
        interactor.openPage();

        // ASSERT
        verify(mockPresenter).presentOpenPage(any(TransferSuggestionsOutputData.class));
    }

    /**
     * TEST 12: openPage() when no team exists
     *
     * COVERS: openPage() null team check
     */
    @Test
    void testOpenPage_NoTeam() {
        // ARRANGE
        when(mockTeamDataAccess.getTeam()).thenReturn(null);

        // ACT
        interactor.openPage();

        // ASSERT - Note: actual message has 2 spaces after period
        verify(mockPresenter).presentFailure("No team found. Please create a team first.");
    }

    /**
     * TEST 13: openPage() exception handling
     *
     * COVERS: openPage() catch block
     */
    @Test
    void testOpenPage_ExceptionHandling() {
        // ARRANGE
        when(mockTeamDataAccess.getTeam()).thenThrow(new RuntimeException("Load error!"));

        // ACT
        interactor.openPage();

        // ASSERT
        verify(mockPresenter).presentFailure("Failed to load team: Load error!");
    }

    /**
     * TEST 14: switchToHomePage() method
     *
     * COVERS: switchToHomePage() method
     */
    @Test
    void testSwitchToHomePage() {
        // ACT
        interactor.switchToHomePage();

        // ASSERT
        verify(mockPresenter).switchToHomePage();
    }

    /**
     * TEST 16: Club count becomes 0 after swap
     * Tests cleanup logic when removing the last player from a club
     *
     * COVERS: if (clubCounts.get(club) == 0) { clubCounts.remove(club); }
     */
    @Test
    void testClubCountBecomesZero_Cleanup() {
        // ARRANGE: Team with only 1 Arsenal player (bad GK), swap will reduce Arsenal to 0
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // The ONLY Arsenal player - bad GK (will be swapped out)
        Player arsenalGK = createPlayerWithPoints(1, "ArsenalGK", 1, 5.0, 1.0, "Arsenal");
        teamPlayers.add(arsenalGK);

        // Rest from different clubs
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.0, "Liverpool"));
        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 6.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 6.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Better GK from Chelsea available
        Player chelseaGK = createPlayerWithPoints(100, "ChelseaGK", 1, 5.0, 7.0, "Chelsea");

        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(arsenalGK);
        allGKs.add(teamPlayers.get(1));
        allGKs.add(chelseaGK);
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));

        java.util.List<Player> allPlayers = new java.util.ArrayList<>(teamPlayers);
        allPlayers.add(chelseaGK);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        java.util.List<Player> topGKs = new java.util.ArrayList<>();
        topGKs.add(chelseaGK);
        topGKs.add(teamPlayers.get(1));
        topGKs.add(arsenalGK);
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(topGKs);

        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Should successfully swap and handle Arsenal club count going to 0
        ArgumentCaptor<TransferSuggestionsOutputData> captor = ArgumentCaptor.forClass(TransferSuggestionsOutputData.class);
        verify(mockPresenter).presentSuccess(captor.capture());

        TransferSuggestionsOutputData outputData = captor.getValue();
        assertEquals(1, outputData.getSwaps().size(), "Should have 1 swap");
        assertEquals(arsenalGK.getId(), outputData.getSwaps().get(0).getPlayerOut().getId());
        assertEquals(chelseaGK.getId(), outputData.getSwaps().get(0).getPlayerIn().getId());
    }

    /**
     * TEST 17: Player with 0 predicted points but in team
     * Tests the OR condition: included because in team even with 0 points
     *
     * COVERS: Second part of (player.getPredictedPoints() > 0.0 || teamPlayerIds.contains(player.getId()))
     */
    @Test
    void testZeroPointsPlayerInTeam_StillIncluded() {
        // ARRANGE: Team with a 0-point GK
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // GK with 0 predicted points (injured/suspended) but still in our team
        Player zeroPointGK = createPlayerWithPoints(1, "InjuredGK", 1, 5.0, 0.0, "Arsenal");
        teamPlayers.add(zeroPointGK);
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.0, "Liverpool"));

        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 6.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 6.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Better GK available
        Player betterGK = createPlayerWithPoints(100, "GoodGK", 1, 5.0, 7.0, "Chelsea");

        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(zeroPointGK); // 0 points but in team -> should be included
        allGKs.add(teamPlayers.get(1));
        allGKs.add(betterGK);
        allGKs.add(createPlayerWithPoints(50, "ZeroGK", 1, 5.0, 0.0, "Brighton")); // 0 points, not in team -> excluded
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));

        java.util.List<Player> allPlayers = new java.util.ArrayList<>(teamPlayers);
        allPlayers.add(betterGK);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        java.util.List<Player> topGKs = new java.util.ArrayList<>();
        topGKs.add(betterGK);
        topGKs.add(teamPlayers.get(1));
        topGKs.add(zeroPointGK);
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(topGKs);

        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Should include the 0-point GK because it's in the team
        ArgumentCaptor<TransferSuggestionsOutputData> captor = ArgumentCaptor.forClass(TransferSuggestionsOutputData.class);
        verify(mockPresenter).presentSuccess(captor.capture());

        TransferSuggestionsOutputData outputData = captor.getValue();
        // The 0-point GK should be replaced
        assertEquals(1, outputData.getSwaps().size());
        assertEquals(zeroPointGK.getId(), outputData.getSwaps().get(0).getPlayerOut().getId());
    }

    // ========== HELPER METHODS ==========

    /**
     * Creates a simple team with the specified number of players.
     * All players are identical - just for testing structure.
     */
    private Team createSimpleTeam(int numberOfPlayers) {
        java.util.List<Player> players = new java.util.ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            // Each player from different team to avoid club constraint issues
            players.add(createSimplePlayer(i + 1, "Team" + i));
        }
        return new Team(players, 10.0f, true);
    }

    /**
     * Creates a very basic player - just enough to not crash.
     * We'll make more realistic players later when needed.
     */
    private Player createSimplePlayer(int id, String teamName) {
        java.util.Map<String, Double> emptyStats = new java.util.HashMap<>();
        return new Player(
                id,                    // id
                "Player" + id,         // name
                1,                     // elementType (1=GK for simplicity)
                "a",                   // status
                5.0,                   // nowCost
                1,                     // position
                teamName,              // team
                emptyStats,            // season totals
                emptyStats,            // season averages
                emptyStats,            // last 3
                emptyStats             // last 5
        );
    }

    /**
     * Creates a player with specific details for testing transfers.
     * This includes predicted points which are critical for the algorithm.
     *
     * @param id Player ID
     * @param name Player name
     * @param elementType Position (1=GK, 2=DEF, 3=MID, 4=FWD)
     * @param cost Player cost
     * @param predictedPoints Predicted points for next gameweek
     * @param teamName Team name (for club constraints)
     */
    private Player createPlayerWithPoints(int id, String name, int elementType,
                                          double cost, double predictedPoints, String teamName) {
        java.util.Map<String, Double> emptyStats = new java.util.HashMap<>();

        Player player = new Player(
                id,
                name,
                elementType,
                "a",                   // status
                cost,
                elementType,           // position matches elementType
                teamName,
                emptyStats,
                emptyStats,
                emptyStats,
                emptyStats
        );

        // Manually set predicted points (simulating what would happen after prediction)
        player.calculatePredictedPoints(new java.util.HashMap<String, Double>() {{
            put("intercept", predictedPoints);  // Hack: just use intercept to set the value
        }});

        return player;
    }

    @Test
    void testPlayerWithNullPredictedPoints_SkippedInPercentileCalculation() {
        // ARRANGE: Team with normal players
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // Normal team setup (2 GK, 5 DEF, 5 MID, 3 FWD)
        teamPlayers.add(createPlayerWithPoints(1, "GK1", 1, 5.0, 5.0, "Arsenal"));
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 4.0, "Liverpool"));

        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 6.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 6.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Create a player with NULL predicted points (external, not in team)
        Player nullPointsPlayer = createPlayerWithNullPredictedPoints(100, "NullGK", 1, 5.0, "Chelsea");

        // Mock getPlayersByPosition to include the null-points player
        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(teamPlayers.get(0));
        allGKs.add(teamPlayers.get(1));
        allGKs.add(nullPointsPlayer);  // This should be skipped
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        // Mock other positions with team players
        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));

        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(teamPlayers);

        // No transfers - just verify percentile calculation doesn't crash
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(0);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Should succeed without crashing on null predicted points
        verify(mockPresenter).presentSuccess(any(TransferSuggestionsOutputData.class));
        verify(mockPresenter, never()).presentFailure(any());
    }

    // Helper method to create a player with null predicted points
    private Player createPlayerWithNullPredictedPoints(int id, String name, int elementType,
                                                       double cost, String teamName) {
        java.util.Map<String, Double> emptyStats = new java.util.HashMap<>();

        // Create player WITHOUT calling calculatePredictedPoints
        // so getPredictedPoints() returns null
        return new Player(
                id,
                name,
                elementType,
                "a",
                cost,
                elementType,
                teamName,
                emptyStats,
                emptyStats,
                emptyStats,
                emptyStats
        );
    }

    @Test
    void testPlayerWithZeroCost_SkippedInPricePercentile() {
        // ARRANGE: Valid team with all good players
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // Bad GK that will be replaced
        Player badGK = createPlayerWithPoints(1, "BadGK", 1, 5.0, 1.0, "Arsenal");
        teamPlayers.add(badGK);
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.0, "Liverpool"));

        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 6.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 6.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Create a zero-cost player (corrupted data) - will be skipped in price percentile
        Player zeroCostGK = createPlayerWithPoints(100, "FreeGK", 1, 0.0, 5.0, "Chelsea");

        // Create a valid replacement GK
        Player betterGK = createPlayerWithPoints(101, "BetterGK", 1, 5.0, 7.0, "Newcastle");

        // Mock getPlayersByPosition - include zero cost player alongside valid ones
        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(teamPlayers.get(0));  // badGK
        allGKs.add(teamPlayers.get(1));  // GK2
        allGKs.add(zeroCostGK);          // Zero cost - should be SKIPPED in price percentile
        allGKs.add(betterGK);            // Valid replacement
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));

        // getAllPlayers for any other checks
        java.util.List<Player> allPlayers = new java.util.ArrayList<>(teamPlayers);
        allPlayers.add(zeroCostGK);
        allPlayers.add(betterGK);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        // Top GKs - betterGK is the best valid option
        java.util.List<Player> topGKs = new java.util.ArrayList<>();
        topGKs.add(betterGK);
        topGKs.add(teamPlayers.get(1));
        topGKs.add(badGK);
        // Note: zeroCostGK excluded or ranked lower
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(topGKs);

        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Should succeed, the zero-cost player was skipped in percentile calculation
        verify(mockPresenter).presentSuccess(any(TransferSuggestionsOutputData.class));
    }

    @Test
    void testNotEnoughCandidates_ReturnsNoValidTransfers() {
        // ARRANGE: Team needs 2 FWD replacements but only 1 candidate exists
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        teamPlayers.add(createPlayerWithPoints(1, "GK1", 1, 5.0, 5.0, "Arsenal"));
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.0, "Liverpool"));

        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 7.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 7.0, "Team" + i));
        }

        // 3 bad forwards - algorithm will try to replace 2 of them
        Player badFwd1 = createPlayerWithPoints(13, "BadFwd1", 4, 7.0, 1.0, "Chelsea");
        Player badFwd2 = createPlayerWithPoints(14, "BadFwd2", 4, 7.0, 1.5, "Man City");
        Player badFwd3 = createPlayerWithPoints(15, "BadFwd3", 4, 7.0, 2.0, "Tottenham");
        teamPlayers.add(badFwd1);
        teamPlayers.add(badFwd2);
        teamPlayers.add(badFwd3);

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Mock percentile calculation
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(teamPlayers.subList(0, 2));
        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(teamPlayers);

        // Only 1 forward candidate available (not enough for 2 replacements)
        Player onlyCandidate = createPlayerWithPoints(100, "OnlyFwd", 4, 7.0, 8.0, "Newcastle");
        java.util.List<Player> topFwds = new java.util.ArrayList<>();
        topFwds.add(onlyCandidate);  // Only 1 candidate!
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(topFwds);

        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());

        // Request 2 transfers - will try to replace 2 forwards but only 1 candidate exists
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(2);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Should fail because can't form valid combinations
        verify(mockPresenter).presentFailure("No valid transfers found within budget and club constraints.");
    }

    @Test
    void testEnrichTeam_PlayerNotFoundInApi_KeepsSavedPlayer() {
        // ARRANGE: Team with a player that doesn't exist in the API
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        Player savedGK = createPlayerWithPoints(1, "SavedGK", 1, 5.0, 5.0, "Arsenal");
        teamPlayers.add(savedGK);
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.0, "Liverpool"));

        for (int i = 3; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 6.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 6.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Mock getPlayerById - return null for player ID 1 (simulating deleted player)
        when(mockPlayerDataAccess.getPlayerById(1)).thenReturn(null);  // Player not found!

        // Return full data for all other players
        for (int i = 2; i <= 15; i++) {
            when(mockPlayerDataAccess.getPlayerById(i)).thenReturn(teamPlayers.get(i - 1));
        }

        // ACT
        interactor.openPage();

        // ASSERT - Should succeed, using the saved player data for ID 1
        ArgumentCaptor<TransferSuggestionsOutputData> captor =
                ArgumentCaptor.forClass(TransferSuggestionsOutputData.class);
        verify(mockPresenter).presentOpenPage(captor.capture());

        TransferSuggestionsOutputData output = captor.getValue();
        assertNotNull(output.getOriginalTeam());
        assertEquals(15, output.getOriginalTeam().getPlayers().size());
    }

    @Test
    void testMultiplePlayersOfSamePosition_GroupedCorrectly() {
        // ARRANGE: Team where 2 forwards are clearly the worst relative to their position
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // GKs - GOOD relative to other GKs (high predicted points)
        teamPlayers.add(createPlayerWithPoints(1, "GK1", 1, 5.0, 6.0, "Club1"));
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.5, "Club2"));

        // DEFs - GOOD relative to other DEFs
        teamPlayers.add(createPlayerWithPoints(3, "Def1", 2, 5.0, 7.0, "Club3"));
        teamPlayers.add(createPlayerWithPoints(4, "Def2", 2, 5.0, 6.5, "Club4"));
        teamPlayers.add(createPlayerWithPoints(5, "Def3", 2, 5.0, 6.8, "Club5"));
        teamPlayers.add(createPlayerWithPoints(6, "Def4", 2, 5.0, 6.2, "Club6"));
        teamPlayers.add(createPlayerWithPoints(7, "Def5", 2, 5.0, 6.4, "Club7"));

        // MIDs - GOOD relative to other MIDs
        teamPlayers.add(createPlayerWithPoints(8, "Mid1", 3, 6.0, 7.5, "Club8"));
        teamPlayers.add(createPlayerWithPoints(9, "Mid2", 3, 6.0, 7.0, "Club9"));
        teamPlayers.add(createPlayerWithPoints(10, "Mid3", 3, 6.0, 7.2, "Club10"));
        teamPlayers.add(createPlayerWithPoints(11, "Mid4", 3, 6.0, 6.8, "Club11"));
        teamPlayers.add(createPlayerWithPoints(12, "Mid5", 3, 6.0, 7.1, "Club12"));

        // FWDs - 2 BAD forwards (will be replaced) + 1 okay forward
        Player badFwd1 = createPlayerWithPoints(13, "BadFwd1", 4, 7.0, 1.0, "Club13");
        Player badFwd2 = createPlayerWithPoints(14, "BadFwd2", 4, 7.0, 1.5, "Club14");
        Player okFwd = createPlayerWithPoints(15, "OkFwd", 4, 7.0, 5.0, "Club15");
        teamPlayers.add(badFwd1);
        teamPlayers.add(badFwd2);
        teamPlayers.add(okFwd);

        // HIGH budget
        Team currentTeam = new Team(teamPlayers, 100.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // 2 GOOD forward replacements
        Player goodFwd1 = createPlayerWithPoints(100, "GoodFwd1", 4, 7.0, 8.0, "Club16");
        Player goodFwd2 = createPlayerWithPoints(101, "GoodFwd2", 4, 7.0, 7.5, "Club17");

        // Mock getPlayerById for team enrichment
        for (Player player : teamPlayers) {
            when(mockPlayerDataAccess.getPlayerById(player.getId())).thenReturn(player);
        }
        when(mockPlayerDataAccess.getPlayerById(100)).thenReturn(goodFwd1);
        when(mockPlayerDataAccess.getPlayerById(101)).thenReturn(goodFwd2);

        // Create external players for each position (worse than team players so team players rank high)

        // External GKs - WORSE than team GKs
        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(teamPlayers.get(0));  // GK1 - 6.0 pts
        allGKs.add(teamPlayers.get(1));  // GK2 - 5.5 pts
        allGKs.add(createPlayerWithPoints(200, "ExtGK1", 1, 4.5, 3.0, "ExtClub1"));
        allGKs.add(createPlayerWithPoints(201, "ExtGK2", 1, 4.5, 2.5, "ExtClub2"));
        allGKs.add(createPlayerWithPoints(202, "ExtGK3", 1, 4.5, 2.0, "ExtClub3"));

        // External DEFs - WORSE than team DEFs
        java.util.List<Player> allDefs = new java.util.ArrayList<>();
        allDefs.addAll(teamPlayers.subList(2, 7));  // Team DEFs - 6.2-7.0 pts
        allDefs.add(createPlayerWithPoints(210, "ExtDef1", 2, 4.5, 3.0, "ExtClub4"));
        allDefs.add(createPlayerWithPoints(211, "ExtDef2", 2, 4.5, 2.5, "ExtClub5"));
        allDefs.add(createPlayerWithPoints(212, "ExtDef3", 2, 4.5, 2.0, "ExtClub6"));

        // External MIDs - WORSE than team MIDs
        java.util.List<Player> allMids = new java.util.ArrayList<>();
        allMids.addAll(teamPlayers.subList(7, 12));  // Team MIDs - 6.8-7.5 pts
        allMids.add(createPlayerWithPoints(220, "ExtMid1", 3, 5.0, 3.0, "ExtClub7"));
        allMids.add(createPlayerWithPoints(221, "ExtMid2", 3, 5.0, 2.5, "ExtClub8"));
        allMids.add(createPlayerWithPoints(222, "ExtMid3", 3, 5.0, 2.0, "ExtClub9"));

        // External FWDs - Include replacements (BETTER) and some worse ones
        java.util.List<Player> allFwds = new java.util.ArrayList<>();
        allFwds.add(goodFwd1);  // 8.0 pts - BEST
        allFwds.add(goodFwd2);  // 7.5 pts
        allFwds.add(okFwd);     // 5.0 pts - team
        allFwds.add(badFwd1);   // 1.5 pts - team (BAD)
        allFwds.add(badFwd2);   // 1.0 pts - team (WORST)
        allFwds.add(createPlayerWithPoints(230, "ExtFwd1", 4, 6.0, 4.0, "ExtClub10"));
        allFwds.add(createPlayerWithPoints(231, "ExtFwd2", 4, 6.0, 3.5, "ExtClub11"));

        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);
        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(allDefs);
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(allMids);
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(allFwds);

        // ALL players for price percentile
        java.util.List<Player> allPlayers = new java.util.ArrayList<>();
        allPlayers.addAll(allGKs);
        allPlayers.addAll(allDefs);
        allPlayers.addAll(allMids);
        allPlayers.addAll(allFwds);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        // TOP forwards sorted by predicted points (descending)
        java.util.List<Player> topFwds = new java.util.ArrayList<>();
        topFwds.add(goodFwd1);  // 8.0 pts - best
        topFwds.add(goodFwd2);  // 7.5 pts
        topFwds.add(okFwd);     // 5.0 pts
        topFwds.add(createPlayerWithPoints(230, "ExtFwd1", 4, 6.0, 4.0, "ExtClub10"));
        topFwds.add(createPlayerWithPoints(231, "ExtFwd2", 4, 6.0, 3.5, "ExtClub11"));
        topFwds.add(badFwd2);   // 1.5 pts
        topFwds.add(badFwd1);   // 1.0 pts - worst
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(topFwds);

        // Empty for other positions (no replacements needed)
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());

        // Request 2 transfers
        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(2);

        // ACT
        interactor.execute(inputData);

        // ASSERT
        ArgumentCaptor<TransferSuggestionsOutputData> captor =
                ArgumentCaptor.forClass(TransferSuggestionsOutputData.class);
        verify(mockPresenter).presentSuccess(captor.capture());

        TransferSuggestionsOutputData output = captor.getValue();
        assertEquals(2, output.getSwaps().size(), "Should have 2 swaps");

        // Verify both swaps are forwards
        for (TransferSuggestionsOutputData.PlayerSwap swap : output.getSwaps()) {
            assertEquals(4, swap.getPlayerOut().getElementType(), "Player out should be forward");
            assertEquals(4, swap.getPlayerIn().getElementType(), "Player in should be forward");
        }
    }

    @Test
    void testClubCountStaysPositive_NoCleanup() {
        // Team has 2 Arsenal players, swap out 1 Arsenal player
        java.util.List<Player> teamPlayers = new java.util.ArrayList<>();

        // 2 Arsenal players - one bad GK, one good defender
        Player badArsenalGK = createPlayerWithPoints(1, "BadArsenalGK", 1, 5.0, 1.0, "Arsenal");
        teamPlayers.add(badArsenalGK);
        teamPlayers.add(createPlayerWithPoints(2, "GK2", 1, 5.0, 5.0, "Liverpool"));

        // Arsenal defender stays - so Arsenal count won't go to 0
        teamPlayers.add(createPlayerWithPoints(3, "ArsenalDef", 2, 5.0, 6.0, "Arsenal"));

        for (int i = 4; i <= 7; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Def" + i, 2, 5.0, 6.0, "Team" + i));
        }
        for (int i = 8; i <= 12; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Mid" + i, 3, 6.0, 6.0, "Team" + i));
        }
        for (int i = 13; i <= 15; i++) {
            teamPlayers.add(createPlayerWithPoints(i, "Fwd" + i, 4, 7.0, 6.0, "Team" + i));
        }

        Team currentTeam = new Team(teamPlayers, 10.0f, true);
        when(mockTeamDataAccess.getTeam()).thenReturn(currentTeam);

        // Better GK from Chelsea available
        Player chelseaGK = createPlayerWithPoints(100, "ChelseaGK", 1, 5.0, 7.0, "Chelsea");

        java.util.List<Player> allGKs = new java.util.ArrayList<>();
        allGKs.add(badArsenalGK);
        allGKs.add(teamPlayers.get(1));
        allGKs.add(chelseaGK);
        when(mockPlayerDataAccess.getPlayersByPosition(1)).thenReturn(allGKs);

        when(mockPlayerDataAccess.getPlayersByPosition(2)).thenReturn(teamPlayers.subList(2, 7));
        when(mockPlayerDataAccess.getPlayersByPosition(3)).thenReturn(teamPlayers.subList(7, 12));
        when(mockPlayerDataAccess.getPlayersByPosition(4)).thenReturn(teamPlayers.subList(12, 15));

        java.util.List<Player> allPlayers = new java.util.ArrayList<>(teamPlayers);
        allPlayers.add(chelseaGK);
        when(mockPlayerDataAccess.getAllPlayers()).thenReturn(allPlayers);

        java.util.List<Player> topGKs = new java.util.ArrayList<>();
        topGKs.add(chelseaGK);
        topGKs.add(teamPlayers.get(1));
        topGKs.add(badArsenalGK);
        when(mockPlayerDataAccess.getTopPlayersByPosition(1, -1)).thenReturn(topGKs);

        when(mockPlayerDataAccess.getTopPlayersByPosition(2, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(3, -1)).thenReturn(new java.util.ArrayList<>());
        when(mockPlayerDataAccess.getTopPlayersByPosition(4, -1)).thenReturn(new java.util.ArrayList<>());

        TransferSuggestionsInputData inputData = new TransferSuggestionsInputData(1);

        // ACT
        interactor.execute(inputData);

        // ASSERT - Arsenal count goes from 2 to 1 (not 0), so no cleanup
        verify(mockPresenter).presentSuccess(any(TransferSuggestionsOutputData.class));
    }



}
