package use_case.best_team;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.PlayerDataAccessInterface;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BestTeamInteractorTest {

    private CapturingPresenter presenter;
    private InMemoryPlayerGateway playerGateway;
    private BestTeamInteractor interactor;

    @BeforeEach
    void setUp() {
        presenter = new CapturingPresenter();
        playerGateway = new InMemoryPlayerGateway();
        interactor = new BestTeamInteractor(playerGateway, presenter);
    }

    /**
     * When there are not enough players to form a valid 15-player squad,
     * the interactor should return an empty players list and null Team.
     */
    @Test
    void testNoValidSquad_NotEnoughPlayers() {
        // Only 1 GK, 2 DEF, 2 MID, 2 FWD -> cannot satisfy 2/5/5/3 requirements.
        List<Player> players = new ArrayList<>();
        players.addAll(createPlayers(1, 1, 5.0, 1, "TeamA"));
        players.addAll(createPlayers(2, 2, 6.0, 10, "TeamB"));
        players.addAll(createPlayers(2, 3, 7.0, 20, "TeamC"));
        players.addAll(createPlayers(2, 4, 8.0, 30, "TeamD"));
        playerGateway.saveAll(players);

        // Since request model is unused, we can pass null.
        interactor.execute(null);

        assertNotNull(presenter.capturedResponse, "Presenter should have been called.");
        BestTeamResponseModel out = presenter.capturedResponse;

        // players list should exist but be empty; Team should be null.
        assertNotNull(out.getPlayers(), "Players list should not be null.");
        assertTrue(out.getPlayers().isEmpty(), "Players list should be empty when no valid squad exists.");
        assertNull(out.getTeam(), "Team should be null when no valid squad exists.");
        assertEquals(0.0, out.getTotalCost(), 1e-6, "Total cost should be 0.0 when no squad.");
        assertEquals(0.0, out.getTotalPredictedPoints(), 1e-6, "Total predicted points should be 0.0 when no squad.");
    }

    /**
     * With a valid set of players, the interactor should build a full 15-player squad
     * and a Team object with correct remaining budget and confirmation flag.
     */
    @Test
    void testBuildsFullSquadAndTeam() {
        // Exactly 15 players: 2 GK, 5 DEF, 5 MID, 3 FWD.
        List<Player> players = new ArrayList<>();
        players.addAll(createPlayers(2, 1, 4.0, 1,  "TeamA")); // GK
        players.addAll(createPlayers(5, 2, 5.0, 10, "TeamB")); // DEF
        players.addAll(createPlayers(5, 3, 6.0, 20, "TeamC")); // MID
        players.addAll(createPlayers(3, 4, 7.0, 30, "TeamD")); // FWD

        playerGateway.saveAll(players);

        interactor.execute(null);

        assertNotNull(presenter.capturedResponse, "Presenter should have been called.");
        BestTeamResponseModel out = presenter.capturedResponse;

        // Check players list from response model.
        List<Player> squad = out.getPlayers();
        assertNotNull(squad, "Players list should not be null for valid input.");
        assertEquals(15, squad.size(), "There should be 15 players in the best squad.");

        // Check Team object.
        Team team = out.getTeam();
        assertNotNull(team, "Team should not be null for valid input.");
        assertEquals(15, team.getPlayers().size(), "Team should contain 15 players.");
        assertTrue(team.isConfirmed(), "Team should be confirmed when it has 15 non-null players.");

        // Budget / remaining budget relation:
        // Interactor uses BUDGET = 100.0 and Team budget = max(0, BUDGET - totalCost).
        double totalCost = out.getTotalCost();
        float expectedRemaining = (float) Math.max(0.0, 100.0 - totalCost);
        assertEquals(expectedRemaining, team.getBudget(), 1e-4,
                "Team budget should equal remaining budget (100 - totalCost).");

        // predicted points should be > 0.
        assertTrue(out.getTotalPredictedPoints() > 0.0,
                "Total predicted points should be positive for a valid squad.");
    }

    /**
     * Helper: create a list of dummy players of a given position.
     * @param count       how many players
     * @param elementType position (1 = GK, 2 = DEF, 3 = MID, 4 = FWD)
     * @param basePoints  base predicted points for differentiation
     * @param startingID  first player ID
     */
    private List<Player> createPlayers(int count, int elementType,
                                       double basePoints, int startingID,
                                       String teamNamePrefix) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {

            // Each player gets its own team name, so we never exceed 3 per team
            String teamName = teamNamePrefix + "_" + i;  // e.g. TeamA_0, TeamA_1, ...

            Player p = new Player(
                    startingID + i,
                    "P" + elementType + "_" + i,
                    elementType,
                    "a",
                    5.0,
                    elementType,
                    teamName,
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>()
            );
            p.calculatePredictedPoints(Map.of("intercept", basePoints + i));
            players.add(p);
        }
        return players;
    }



    /**
     * Output boundary implementation that captures the response for assertions.
     */
    private static class CapturingPresenter implements BestTeamOutputBoundary {
        BestTeamResponseModel capturedResponse;

        @Override
        public void present(BestTeamResponseModel responseModel) {
            this.capturedResponse = responseModel;
        }
    }

    /**
     * Simple in-memory Player gateway for testing BestTeamInteractor.
     */
    private static class InMemoryPlayerGateway implements PlayerDataAccessInterface {

        private final Map<Integer, Player> players = new HashMap<>();

        @Override
        public void saveAll(List<Player> players) {
            for (Player p : players) {
                this.players.put(p.getId(), p);
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
            return players.values().stream()
                    .filter(p -> p.getElementType() == position)
                    .collect(Collectors.toList());
        }

        @Override
        public List<Player> getPlayersByTeam(String teamName) {
            return players.values().stream()
                    .filter(p -> p.getTeamName().equalsIgnoreCase(teamName))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Player> getTopPlayersByPosition(int position, int limit) {
            return players.values().stream()
                    .filter(p -> p.getElementType() == position)
                    .sorted(Comparator.comparing(
                            Player::getPredictedPoints,
                            Comparator.nullsLast(Double::compareTo)
                    ).reversed())
                    .limit(limit < 0 ? Long.MAX_VALUE : limit)
                    .collect(Collectors.toList());
        }

        @Override
        public List<Player> getTopPlayers(int limit) {
            return players.values().stream()
                    .sorted(Comparator.comparing(
                            Player::getPredictedPoints,
                            Comparator.nullsLast(Double::compareTo)
                    ).reversed())
                    .limit(limit < 0 ? Long.MAX_VALUE : limit)
                    .collect(Collectors.toList());
        }

        @Override
        public List<Player> getPlayersByName(String name) {
            return players.values().stream()
                    .filter(p -> p.getWebName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }
}
