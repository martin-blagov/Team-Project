package use_case.transfer_suggestions;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransferSuggestionsOutputData class.
 */
public class TransferSuggestionsOutputDataTest {

    /**
     * Helper method to create a test Player with specified values.
     * Uses empty stat maps since we only need basic player info for these tests.
     */
    private Player createTestPlayer(int id, String name, int position, double cost, double predictedPoints) {
        Player player = new Player(
                id,
                name,
                position,       // elementType
                "a",            // status
                cost,           // nowCost
                position,       // position
                "TestClub",     // team
                new HashMap<>(), // seasonTotalStats
                new HashMap<>(), // seasonAvgStats
                new HashMap<>(), // last3Stats
                new HashMap<>()  // last5Stats
        );
        // Set predicted points using the coefficient method
        player.calculatePredictedPoints(java.util.Map.of("intercept", predictedPoints));
        return player;
    }

    // ========== TransferSuggestionsOutputData Tests ==========

    @Test
    void testConstructorAndGetters() {
        // Arrange: Create test data
        List<Player> originalPlayers = List.of(
                createTestPlayer(1, "PlayerA", 4, 8.0, 5.0)
        );
        Team originalTeam = new Team(originalPlayers, 10.0f, false);

        List<Player> suggestedPlayers = List.of(
                createTestPlayer(2, "PlayerB", 4, 9.0, 7.0)
        );
        Team suggestedTeam = new Team(suggestedPlayers, 9.0f, false);

        List<TransferSuggestionsOutputData.PlayerSwap> swaps = new ArrayList<>();
        double totalImprovement = 2.0;

        // Act: Create output data
        TransferSuggestionsOutputData outputData = new TransferSuggestionsOutputData(
                originalTeam,
                suggestedTeam,
                swaps,
                totalImprovement
        );

        // Assert: Verify all getters return correct values
        assertEquals(originalTeam, outputData.getOriginalTeam());
        assertEquals(suggestedTeam, outputData.getSuggestedTeam());
        assertEquals(swaps, outputData.getSwaps());
        assertEquals(2.0, outputData.getTotalPointsImprovement());
    }

    // ========== PlayerSwap Tests ==========

    @Test
    void testPlayerSwapConstructorAndGetters() {
        // Arrange: Create two players for the swap
        Player playerOut = createTestPlayer(1, "OldPlayer", 4, 8.0, 5.0);
        Player playerIn = createTestPlayer(2, "NewPlayer", 4, 9.0, 7.0);

        // Act: Create a PlayerSwap
        TransferSuggestionsOutputData.PlayerSwap swap =
                new TransferSuggestionsOutputData.PlayerSwap(playerOut, playerIn);

        // Assert: Verify getters return correct players
        assertEquals(playerOut, swap.getPlayerOut());
        assertEquals(playerIn, swap.getPlayerIn());
    }

    @Test
    void testGetCostDifferencePositive() {
        // Arrange: playerIn is MORE expensive than playerOut
        Player playerOut = createTestPlayer(1, "CheapPlayer", 4, 6.0, 5.0);
        Player playerIn = createTestPlayer(2, "ExpensivePlayer", 4, 10.0, 7.0);

        TransferSuggestionsOutputData.PlayerSwap swap =
                new TransferSuggestionsOutputData.PlayerSwap(playerOut, playerIn);

        // Act & Assert: Cost difference should be positive (10.0 - 6.0 = 4.0)
        assertEquals(4.0, swap.getCostDifference(), 0.001);
    }

    @Test
    void testGetCostDifferenceNegative() {
        // Arrange: playerIn is CHEAPER than playerOut
        Player playerOut = createTestPlayer(1, "ExpensivePlayer", 4, 12.0, 5.0);
        Player playerIn = createTestPlayer(2, "CheapPlayer", 4, 7.0, 6.0);

        TransferSuggestionsOutputData.PlayerSwap swap =
                new TransferSuggestionsOutputData.PlayerSwap(playerOut, playerIn);

        // Act & Assert: Cost difference should be negative (7.0 - 12.0 = -5.0)
        assertEquals(-5.0, swap.getCostDifference(), 0.001);
    }

    @Test
    void testGetCostDifferenceZero() {
        // Arrange: Both players have the same cost
        Player playerOut = createTestPlayer(1, "PlayerA", 4, 8.5, 5.0);
        Player playerIn = createTestPlayer(2, "PlayerB", 4, 8.5, 7.0);

        TransferSuggestionsOutputData.PlayerSwap swap =
                new TransferSuggestionsOutputData.PlayerSwap(playerOut, playerIn);

        // Act & Assert: Cost difference should be zero
        assertEquals(0.0, swap.getCostDifference(), 0.001);
    }

    // ========== getPointsImprovement Tests ==========

    @Test
    void testGetPointsImprovementPositive() {
        // Arrange: playerIn has MORE predicted points than playerOut (upgrade)
        Player playerOut = createTestPlayer(1, "WeakPlayer", 3, 7.0, 4.0);
        Player playerIn = createTestPlayer(2, "StrongPlayer", 3, 8.0, 9.0);

        TransferSuggestionsOutputData.PlayerSwap swap =
                new TransferSuggestionsOutputData.PlayerSwap(playerOut, playerIn);

        // Act & Assert: Points improvement should be positive (9.0 - 4.0 = 5.0)
        assertEquals(5.0, swap.getPointsImprovement(), 0.001);
    }

    @Test
    void testGetPointsImprovementNegative() {
        // Arrange: playerIn has FEWER predicted points than playerOut (downgrade)
        Player playerOut = createTestPlayer(1, "StarPlayer", 3, 12.0, 10.0);
        Player playerIn = createTestPlayer(2, "BenchPlayer", 3, 5.0, 3.0);

        TransferSuggestionsOutputData.PlayerSwap swap =
                new TransferSuggestionsOutputData.PlayerSwap(playerOut, playerIn);

        // Act & Assert: Points improvement should be negative (3.0 - 10.0 = -7.0)
        assertEquals(-7.0, swap.getPointsImprovement(), 0.001);
    }

    @Test
    void testGetPointsImprovementZero() {
        // Arrange: Both players have the same predicted points
        Player playerOut = createTestPlayer(1, "PlayerX", 2, 6.0, 5.5);
        Player playerIn = createTestPlayer(2, "PlayerY", 2, 7.0, 5.5);

        TransferSuggestionsOutputData.PlayerSwap swap =
                new TransferSuggestionsOutputData.PlayerSwap(playerOut, playerIn);

        // Act & Assert: Points improvement should be zero
        assertEquals(0.0, swap.getPointsImprovement(), 0.001);
    }

}