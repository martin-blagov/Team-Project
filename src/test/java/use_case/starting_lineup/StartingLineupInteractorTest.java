package use_case.starting_lineup;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.PlayerDataAccessInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StartingLineupInteractor.
 */
class StartingLineupInteractorTest {

    private CapturingPresenter presenter;
    private InMemoryTeamGateway teamGateway;
    private InMemoryPlayerGateway playerGateway;
    private StartingLineupInteractor interactor;

    @BeforeEach
    void setUp() {
        presenter = new CapturingPresenter();
        teamGateway = new InMemoryTeamGateway();
        playerGateway = new InMemoryPlayerGateway();
        interactor = new StartingLineupInteractor(presenter, teamGateway, playerGateway);
    }

    /**
     * Test case with no team created.
     */
    @Test
    void testNoTeam() {
        teamGateway.setTeam(null);

        interactor.execute();

        assertNotNull(presenter.capturedOutput, "No output data.");
        assertNull(presenter.capturedOutput.getStartingTeam(), "Starting team should be null.");
        assertNotNull(presenter.capturedOutput.getBenchPlayers(), "Bench list should not be null.");
        assertTrue(presenter.capturedOutput.getBenchPlayers().isEmpty(), "Bench list should be empty.");
    }

    /**
     * Test case with a complete team.
     */
    @Test
    void testCompleteTeam() {
        List<Player> players = new ArrayList<>();
        // Create 15 players:
        players.addAll(createPlayers(2, 1, 0.5, 0));
        players.addAll(createPlayers(5, 2, 2.5, 2));
        players.addAll(createPlayers(5, 3, 4.5, 7));
        players.addAll(createPlayers(3, 4, 6.5, 10));
        playerGateway.saveAll(players);

        Team testTeam = new Team(players, 0.0f, true);
        teamGateway.setTeam(testTeam);

        interactor.execute();

        assertNotNull(presenter.capturedOutput, "No output data.");
        Team startingLineup = presenter.capturedOutput.getStartingTeam();
        assertNotNull(startingLineup, "Starting lineup should not be null.");
        assertEquals(11, startingLineup.getPlayers().size(),
                "Starting lineup should have 11 players.");

        List<Player> bench = presenter.capturedOutput.getBenchPlayers();
        assertNotNull(bench, "Bench list should not be null.");
        assertEquals(players.size() - startingLineup.getPlayers().size(), bench.size(),
                "Bench should contain all remaining players.");
    }

    /**
     * Test case with incomplete team (only 5 players).
     */
    @Test
    void testIncompleteTeam() {
        List<Player> players = new ArrayList<>();
        players.addAll(createPlayers(1, 1, 0.6, 0));
        players.addAll(createPlayers(2, 2, 2.6, 1));
        players.addAll(createPlayers(1, 3, 4.6, 3));
        players.addAll(createPlayers(1, 4, 6.6, 4));
        playerGateway.saveAll(players);

        Team testTeam = new Team(players, 0.0f, false);
        teamGateway.setTeam(testTeam);

        interactor.execute();

        assertNotNull(presenter.capturedOutput, "No output data.");
        Team startingLineup = presenter.capturedOutput.getStartingTeam();
        assertNotNull(startingLineup, "Starting team should not be null.");
        assertEquals(5, startingLineup.getPlayers().size(),
                "Starting lineup should have 5 players.");

        List<Player> bench = presenter.capturedOutput.getBenchPlayers();
        assertNotNull(bench, "Bench list should not be null.");
        assertTrue(bench.isEmpty(), "Bench list should be empty.");
    }

    /**
     * Helper method to create dummy players for testing.
     */
    private List<Player> createPlayers(int count, int elementType, double basePoints, int startingID) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(new Player(
                    startingID + i,                      // id
                    "Player" + elementType + "_" + i,   // webName
                    elementType,                        // elementType
                    "a",                                // status
                    0.0,                                // nowCost
                    elementType,                        // position
                    "Team",                             // team name
                    new HashMap<>(),                    // seasonTotalStats (create empty)
                    new HashMap<>(),                    // seasonAvgStats (create empty)
                    new HashMap<>(),                    // last3Stats (create empty)
                    new HashMap<>()                     // last5Stats (create empty)
            ));
            // Set predicted points.
            players.get(players.size() - 1).calculatePredictedPoints(
                    java.util.Map.of("intercept", basePoints + i)
            );
        }
        return players;
    }

    /**
     * Output boundary for capturing interactor output.
     */
    private static class CapturingPresenter implements StartingLineupOutputBoundary {
        StartingLineupOutputData capturedOutput;

        @Override
        public void presentLineup(StartingLineupOutputData outputData) {
            this.capturedOutput = outputData;
        }
    }

    /**
     * In-memory team data access gateway for testing.
     */
    private static class InMemoryTeamGateway implements StartingLineupTeamDataAccessInterface {
        private Team team;

        public void setTeam(Team team) {
            this.team = team;
        }

        @Override
        public Team getTeam() {
            return team;
        }
    }

    private static class InMemoryPlayerGateway implements PlayerDataAccessInterface {
        private final java.util.Map<Integer, Player> players = new java.util.HashMap<>();

        @Override
        public void saveAll(List<Player> players) {
            for (Player player : players) {
                this.players.put(player.getId(), player);
            }
        }

        @Override
        public List<Player> getAllPlayers() {
            return new ArrayList<>(players.values());
        }

        @Override
        public Player getPlayerById(int id) {
            return players.get(id);
        }

        @Override
        public List<Player> getPlayersByPosition(int position) {
            List<Player> result = new ArrayList<>();
            for (Player player : players.values()) {
                if (player.getElementType() == position) {
                    result.add(player);
                }
            }
            return result;
        }

        @Override
        public List<Player> getPlayersByTeam(String teamName) {
            List<Player> result = new ArrayList<>();
            for (Player player : players.values()) {
                if (player.getTeamName().equalsIgnoreCase(teamName)) {
                    result.add(player);
                }
            }
            return result;
        }

        @Override
        public List<Player> getTopPlayersByPosition(int position, int limit) {
            return players.values().stream()
                    .filter(p -> p.getElementType() == position)
                    .sorted(java.util.Comparator.comparing(Player::getPredictedPoints,
                            java.util.Comparator.nullsLast(Double::compareTo)).reversed())
                    .limit(limit < 0 ? Long.MAX_VALUE : limit)
                    .collect(Collectors.toList());
        }

        @Override
        public List<Player> getTopPlayers(int limit) {
            return players.values().stream()
                    .sorted(java.util.Comparator.comparing(Player::getPredictedPoints,
                            java.util.Comparator.nullsLast(Double::compareTo)).reversed())
                    .limit(limit < 0 ? Long.MAX_VALUE : limit)
                    .collect(Collectors.toList());
        }

        @Override
        public List<Player> getPlayersByName(String name) {
            List<Player> result = new ArrayList<>();
            for (Player player : players.values()) {
                if (player.getWebName().toLowerCase().contains(name.toLowerCase())) {
                    result.add(player);
                }
            }
            return result;
        }
    }
}
