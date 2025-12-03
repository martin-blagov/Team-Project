package use_case.best_team;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.PlayerDataAccessInterface;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
     * This hits the first "break" condition in the main while-loop.
     */
    @Test
    void testNoValidSquad_NotEnoughPlayers() {
        // Only 1 GK, 2 DEF, 2 MID, 2 FWD -> cannot satisfy 2/5/5/3 requirements.
        List<Player> players = new ArrayList<>();
        players.addAll(createPlayersUniqueTeams(1, 1, 5.0, 1, "TeamA"));
        players.addAll(createPlayersUniqueTeams(2, 2, 6.0, 10, "TeamB"));
        players.addAll(createPlayersUniqueTeams(2, 3, 7.0, 20, "TeamC"));
        players.addAll(createPlayersUniqueTeams(2, 4, 8.0, 30, "TeamD"));
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
     * This covers the successful path where a squad is found in the first iteration.
     */
    @Test
    void testBuildsFullSquadAndTeam() {
        // Exactly 15 players: 2 GK, 5 DEF, 5 MID, 3 FWD.
        List<Player> players = new ArrayList<>();
        players.addAll(createPlayersUniqueTeams(2, 1, 4.0, 1,  "TeamA")); // GK
        players.addAll(createPlayersUniqueTeams(5, 2, 5.0, 10, "TeamB")); // DEF
        players.addAll(createPlayersUniqueTeams(5, 3, 6.0, 20, "TeamC")); // MID
        players.addAll(createPlayersUniqueTeams(3, 4, 7.0, 30, "TeamD")); // FWD

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
     * Enough players exist in each position, but they are concentrated in a small
     * number of teams. Because at most 3 players per team can be chosen, there is
     * no valid 15-player squad. This forces the while-loop to exit using the
     * second "no more candidates to expand" break condition and exercises
     * canAddPlayer() / removeTeamCount() branches where a player cannot be added.
     */
    @Test
    void testNoValidSquad_TooManyFromSameTeamsTriggersSecondBreak() {
        List<Player> players = new ArrayList<>();

        // Many players, but only two teams overall.
        players.addAll(createPlayersSameTeam(4, 1, 4.0, 1,  "TeamX")); // GK
        players.addAll(createPlayersSameTeam(7, 2, 5.0, 10, "TeamX")); // DEF
        players.addAll(createPlayersSameTeam(7, 3, 6.0, 20, "TeamY")); // MID
        players.addAll(createPlayersSameTeam(6, 4, 7.0, 30, "TeamY")); // FWD

        playerGateway.saveAll(players);

        interactor.execute(null);

        BestTeamResponseModel out = presenter.capturedResponse;
        assertNotNull(out, "Presenter should have been called.");

        assertNotNull(out.getPlayers(), "Players list should not be null.");
        assertTrue(out.getPlayers().isEmpty(), "Players list should be empty when team constraint prevents a squad.");
        assertNull(out.getTeam(), "Team should be null when team constraint prevents a squad.");
    }

    /**
     * Uses reflection to directly exercise private helper methods so that
     * some branches that are hard to reach through the public API are covered:
     *  safePoints(Player) with null predicted points
     *  searchGK/searchDEF/searchMID/searchFWD with costSoFar > BUDGET, hitting the early-return branch in each of
     *  those methods.
     */
    @Test
    void testPrivateBranchesViaReflection() throws Exception {
        // safePoints(Player) null branch
        Player p = new Player(
                999,
                "NoPoints",
                1,
                "a",
                5.0,
                1,
                "TeamZ",
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        );
        // Do not call calculatePredictedPoints -> predicted points remain null.

        Method safePoints = BestTeamInteractor.class
                .getDeclaredMethod("safePoints", Player.class);
        safePoints.setAccessible(true);

        Double result = (Double) safePoints.invoke(interactor, p);
        assertEquals(0.0, result, 1e-6,
                "safePoints should return 0.0 when predicted points are null.");

        // costSoFar > BUDGET branches in search* methods
        Class<?> bestResultClass = Arrays.stream(BestTeamInteractor.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("BestResult"))
                .findFirst()
                .orElseThrow();

        List<Player> emptyPlayers = Collections.emptyList();
        List<Player> current = new ArrayList<>();
        Map<String, Integer> teamCounts = new HashMap<>();
        Object best = null; // safe because methods will return immediately

        // searchGK
        Method searchGK = BestTeamInteractor.class.getDeclaredMethod(
                "searchGK",
                List.class, int.class, int.class,
                List.class, List.class, List.class,
                List.class, Map.class,
                double.class, double.class,
                bestResultClass
        );
        searchGK.setAccessible(true);
        searchGK.invoke(interactor,
                emptyPlayers, 0, 2,
                emptyPlayers, emptyPlayers, emptyPlayers,
                current, teamCounts,
                200.0, 0.0, best);

        // searchDEF
        Method searchDEF = BestTeamInteractor.class.getDeclaredMethod(
                "searchDEF",
                List.class, int.class, int.class,
                List.class, List.class,
                List.class, Map.class,
                double.class, double.class,
                bestResultClass
        );
        searchDEF.setAccessible(true);
        searchDEF.invoke(interactor,
                emptyPlayers, 0, 5,
                emptyPlayers, emptyPlayers,
                current, teamCounts,
                200.0, 0.0, best);

        // searchMID
        Method searchMID = BestTeamInteractor.class.getDeclaredMethod(
                "searchMID",
                List.class, int.class, int.class,
                List.class, List.class, Map.class,
                double.class, double.class,
                bestResultClass
        );
        searchMID.setAccessible(true);
        searchMID.invoke(interactor,
                emptyPlayers, 0, 5,
                emptyPlayers, current, teamCounts,
                200.0, 0.0, best);

        // searchFWD (cost branch)
        Method searchFWD = BestTeamInteractor.class.getDeclaredMethod(
                "searchFWD",
                List.class, int.class, int.class,
                List.class, Map.class,
                double.class, double.class,
                bestResultClass
        );
        searchFWD.setAccessible(true);
        searchFWD.invoke(interactor,
                emptyPlayers, 0, 3,
                current, teamCounts,
                200.0, 0.0, best);
    }

    /**
     * Directly exercises buildTeamFromSquad so that both branches are covered:
     * remaining budget clamped to 0 when totalCost > BUDGET
     * team is not confirmed when squad size is not 15.
     */
    @Test
    void testBuildTeamFromSquadBranches() throws Exception {
        // create a small squad (size != 15)
        List<Player> smallSquad = createPlayersUniqueTeams(5, 1, 4.0, 500, "TmpTeam");

        Method buildTeamMethod = BestTeamInteractor.class
                .getDeclaredMethod("buildTeamFromSquad", List.class, double.class);
        buildTeamMethod.setAccessible(true);

        double totalCostOverBudget = 150.0;
        Team team = (Team) buildTeamMethod.invoke(interactor, smallSquad, totalCostOverBudget);

        // remainingBudget should be clamped to 0
        assertEquals(0.0f, team.getBudget(), 1e-6);
        // squad size is not 15 -> isConfirmed should be false
        assertFalse(team.isConfirmed(), "Team should not be confirmed when squad size is not 15.");
    }

    /**
     * Directly exercises canAddPlayer and removeTeamCount to cover their true/false branches (team count < 3 vs >=
     * 3, and count <= 1 vs > 1).
     */
    @Test
    void testCanAddAndRemoveTeamCountBranches() throws Exception {
        Method canAddPlayerMethod = BestTeamInteractor.class
                .getDeclaredMethod("canAddPlayer", Player.class, Map.class);
        canAddPlayerMethod.setAccessible(true);

        Method removeTeamCountMethod = BestTeamInteractor.class
                .getDeclaredMethod("removeTeamCount", Player.class, Map.class);
        removeTeamCountMethod.setAccessible(true);

        Map<String, Integer> teamCounts = new HashMap<>();
        Player p = new Player(
                1000,
                "TestP",
                2,
                "a",
                5.0,
                1,
                "TeamX",
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        );

        // Case 1: no existing entry -> count = 0 -> canAdd should be true
        boolean canAddFirst = (boolean) canAddPlayerMethod.invoke(interactor, p, teamCounts);
        assertTrue(canAddFirst, "First player from a team should be allowed.");

        // Simulate having 3 players already -> canAdd should now be false
        teamCounts.put("TeamX", 3);
        boolean canAddFourth = (boolean) canAddPlayerMethod.invoke(interactor, p, teamCounts);
        assertFalse(canAddFourth, "Fourth player from the same team should be rejected.");

        // removeTeamCount with count <= 1: entry should be removed from map
        teamCounts.put("TeamX", 1);
        removeTeamCountMethod.invoke(interactor, p, teamCounts);
        assertFalse(teamCounts.containsKey("TeamX"),
                "When count <= 1, removeTeamCount should remove the team entry.");

        // removeTeamCount with count > 1: entry should remain with decremented count
        teamCounts.put("TeamX", 3);
        removeTeamCountMethod.invoke(interactor, p, teamCounts);
        assertEquals(2, teamCounts.get("TeamX"),
                "When count > 1, removeTeamCount should decrement the count.");
    }

    /**
     * Directly exercises searchFWD for the case fwdNeeded == 0 so that both branches of the inner comparison
     * (pointsSoFar > best.totalPoints) are covered (true and false).
     */
    @Test
    void testSearchFwdBestResultBranches() throws Exception {
        // Find the nested BestResult class and create an instance
        Class<?> bestResultClass = Arrays.stream(BestTeamInteractor.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("BestResult"))
                .findFirst()
                .orElseThrow();

        Constructor<?> ctor = bestResultClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object best = ctor.newInstance();

        Field totalPointsField = bestResultClass.getDeclaredField("totalPoints");
        Field totalCostField = bestResultClass.getDeclaredField("totalCost");
        Field squadField = bestResultClass.getDeclaredField("squad");
        totalPointsField.setAccessible(true);
        totalCostField.setAccessible(true);
        squadField.setAccessible(true);

        List<Player> fwds = Collections.emptyList();
        List<Player> current = new ArrayList<>();
        current.add(new Player(
                2000,
                "CurrFWD",
                4,
                "a",
                5.0,
                1,
                "TeamY",
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        ));
        Map<String, Integer> teamCounts = new HashMap<>();

        Method searchFwdMethod = BestTeamInteractor.class.getDeclaredMethod(
                "searchFWD",
                List.class, int.class, int.class,
                List.class, Map.class,
                double.class, double.class,
                bestResultClass
        );
        searchFwdMethod.setAccessible(true);

        // First call: condition is true (10 > -1) so BestResult is updated.
        searchFwdMethod.invoke(interactor,
                fwds, 0, 0,
                current, teamCounts,
                50.0, 10.0, best);

        double afterFirst = (double) totalPointsField.get(best);
        assertEquals(10.0, afterFirst, 1e-6);

        // Second call: condition is false (5 <= 10), so BestResult should not change.
        searchFwdMethod.invoke(interactor,
                fwds, 0, 0,
                Collections.emptyList(), teamCounts,
                60.0, 5.0, best);

        double afterSecond = (double) totalPointsField.get(best);
        double storedCost = (double) totalCostField.get(best);
        @SuppressWarnings("unchecked")
        List<Player> storedSquad = (List<Player>) squadField.get(best);

        assertEquals(10.0, afterSecond, 1e-6,
                "Second call should not overwrite totalPoints because 5 <= 10.");
        assertEquals(50.0, storedCost, 1e-6,
                "totalCost should remain as from the first call.");
        assertEquals(1, storedSquad.size(),
                "Stored squad should remain as the one from the first call.");
    }

    /**
     * Helper: create a list of dummy players of a given position, with each player
     * placed on a unique team (to avoid team-size constraints).
     */
    private List<Player> createPlayersUniqueTeams(int count, int elementType,
                                                  double basePoints, int startingID,
                                                  String teamNamePrefix) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String teamName = teamNamePrefix + "_" + i;

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
     * Helper: create players that all belong to the same concrete team name.
     * This is used to trigger the per-team limit and related branches.
     */
    private List<Player> createPlayersSameTeam(int count, int elementType,
                                               double basePoints, int startingID,
                                               String teamName) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Player p = new Player(
                    startingID + i,
                    "SameTeamP" + elementType + "_" + i,
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

    @Test
    void testWhileLoopBreakWhenOnlyOnePositionStopsGrowing() {
        // We create a scenario where GK/DEF/MID lists keep expanding,
        // but FWD list stops growing early → triggers "prevFWDsize == fwds.size()" branch.

        List<Player> players = new ArrayList<>();

        // Many GK, DEF, MID (so they can increase)
        players.addAll(createPlayersUniqueTeams(10, 1, 4.0, 1, "GKTeam"));   // GK
        players.addAll(createPlayersUniqueTeams(10, 2, 5.0, 20, "DEFTeam")); // DEF
        players.addAll(createPlayersUniqueTeams(10, 3, 6.0, 40, "MIDTeam")); // MID

        // BUT only exactly 3 forwards → enough to satisfy requirements,
        // but they will never grow beyond limit so fwds.size() stops growing.
        players.addAll(createPlayersUniqueTeams(3, 4, 7.0, 60, "FWDTeam"));

        playerGateway.saveAll(players);

        interactor.execute(null);

        BestTeamResponseModel out = presenter.capturedResponse;
        assertNotNull(out);
        assertNotNull(out.getPlayers());
    }

    @Test
    void testBreakCondition_DefendersTooFew() {
        List<Player> players = new ArrayList<>();

        players.addAll(createPlayersUniqueTeams(2, 1, 4.0, 1, "GKTeam"));
        players.addAll(createPlayersUniqueTeams(3, 2, 5.0, 20, "DEFTeam"));
        players.addAll(createPlayersUniqueTeams(5, 3, 6.0, 40, "MIDTeam"));
        players.addAll(createPlayersUniqueTeams(3, 4, 7.0, 60, "FWDTeam"));

        playerGateway.saveAll(players);
        interactor.execute(null);

        BestTeamResponseModel out = presenter.capturedResponse;
        assertNotNull(out);
        assertTrue(out.getPlayers().isEmpty());
        assertNull(out.getTeam());
    }

    @Test
    void testBreakCondition_MidfieldersTooFew() {
        List<Player> players = new ArrayList<>();

        players.addAll(createPlayersUniqueTeams(2, 1, 4.0, 1, "GKTeam"));
        players.addAll(createPlayersUniqueTeams(5, 2, 5.0, 20, "DEFTeam"));
        players.addAll(createPlayersUniqueTeams(3, 3, 6.0, 40, "MIDTeam"));
        players.addAll(createPlayersUniqueTeams(3, 4, 7.0, 60, "FWDTeam"));

        playerGateway.saveAll(players);
        interactor.execute(null);

        BestTeamResponseModel out = presenter.capturedResponse;
        assertNotNull(out);
        assertTrue(out.getPlayers().isEmpty());
        assertNull(out.getTeam());
    }

    @Test
    void testBranchCoverage_AllSearchIndexAndPrevSizeBreaks() {

        List<Player> players = new ArrayList<>();

        // Make ALL positions have exactly 1 player
        // so that index >= size quickly becomes true inside recursive search methods.
        players.addAll(createPlayersUniqueTeams(1, 1, 4.0, 1, "GKteam"));   // 1 GK
        players.addAll(createPlayersUniqueTeams(1, 2, 5.0, 10, "DEFteam")); // 1 DEF
        players.addAll(createPlayersUniqueTeams(1, 3, 6.0, 20, "MIDteam")); // 1 MID
        players.addAll(createPlayersUniqueTeams(1, 4, 7.0, 30, "FWDteam")); // 1 FWD

        playerGateway.saveAll(players);

        interactor.execute(null);

        BestTeamResponseModel out = presenter.capturedResponse;

        // Should fail due to not enough players
        assertNotNull(out);
        assertTrue(out.getPlayers().isEmpty());
        assertNull(out.getTeam());
    }

}
