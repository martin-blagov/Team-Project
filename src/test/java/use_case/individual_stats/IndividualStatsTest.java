package use_Case.individual_stats;

import entity.Player;
import org.junit.jupiter.api.Test;
import use_case.PlayerDataAccessInterface;
import use_case.display_individual_stat.DisplayIndividualStatInputData;
import use_case.display_individual_stat.DisplayIndividualStatInteractor;
import use_case.display_individual_stat.DisplayIndividualStatOutputBoundary;
import use_case.display_individual_stat.DisplayIndividualStatOutputData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndividualStatsTest {

    @Test
    void testSuccessDisplayStats() {
        TestPlayerDataAccess playerDataAccess = new TestPlayerDataAccess();
        TestOutputBoundary presenter = new TestOutputBoundary();
        DisplayIndividualStatInteractor interactor = new DisplayIndividualStatInteractor(presenter, playerDataAccess);

        Map<String, Double> seasonTotalStats = new HashMap<>();
        seasonTotalStats.put("goals_scored", 1.0);
        seasonTotalStats.put("assists", 2.0);
        seasonTotalStats.put("points", 100.0);

        Map<String, Double> seasonAvgStats = new HashMap<>();
        seasonAvgStats.put("goals_scored", 1.1);
        seasonAvgStats.put("assists", 2.1);
        seasonAvgStats.put("points", 100.1);

        Map<String, Double> seasonLast3Stats = new HashMap<>();
        seasonLast3Stats.put("goals_scored", 1.2);
        seasonLast3Stats.put("assists", 2.2);
        seasonLast3Stats.put("points", 100.2);

        Map<String, Double> seasonLast5Stats = new HashMap<>();
        seasonLast5Stats.put("goals_scored", 1.3);
        seasonLast5Stats.put("assists", 2.3);
        seasonLast5Stats.put("points", 100.3);

        // Create test player
        Player player = new Player(
                1,
                "Player1",
                1,
                "a",
                12,
                1,
                "Team1",
                seasonTotalStats,
                seasonAvgStats,
                seasonLast3Stats,
                seasonLast5Stats);

        playerDataAccess.addPlayer(player);

        DisplayIndividualStatInputData inputData =
                new DisplayIndividualStatInputData(1, "Total");

        interactor.execute(inputData);

        DisplayIndividualStatOutputData outputData = presenter.outputData;

        assertEquals("Player1", outputData.getPlayerName());
        assertEquals("1", outputData.getPlayerGoals());
        assertEquals("2", outputData.getPlayerAssists());
        assertEquals("12", outputData.getPlayerCost());
        assertEquals("forward", outputData.getPlayerPosition());
        assertEquals("Team1", outputData.getPlayerTeam());
    }

    static class TestPlayerDataAccess implements PlayerDataAccessInterface {

        private final Map<Integer, Player> players = new HashMap<>();

        void addPlayer(Player player) {
            players.put(player.getId(), player);
        }

        @Override
        public void saveAll(List<Player> players) {
        }

        @Override
        public List<Player> getAllPlayers() {
            return null;
        }

        @Override
        public Player getPlayerById(int id) {
            return players.get(id);
        }

        @Override
        public List<Player> getPlayersByPosition(int position) {
            return null;
        }

        @Override
        public List<Player> getPlayersByTeam(String teamName) {
            return null;
        }

        @Override
        public List<Player> getTopPlayersByPosition(int position, int limit) {
            return null;
        }

        @Override
        public List<Player> getTopPlayers(int limit) {
            return null;
        }

        @Override
        public List<Player> getPlayersByName(String name) {
            return null;
        }
    }

    static class TestOutputBoundary implements DisplayIndividualStatOutputBoundary {
        DisplayIndividualStatOutputData outputData;

        @Override
        public void presentView(DisplayIndividualStatOutputData outputData) { this.outputData = outputData; }
    }
}
