package use_case.display_team;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DisplayTeamInteractorTest {

    private CapturingPresenter presenter;
    private InMemoryTeamGateway teamGateway;
    private DisplayTeamInteractor interactor;

    @BeforeEach
    void setUp() {
        presenter = new CapturingPresenter();
        teamGateway = new InMemoryTeamGateway();
        interactor = new DisplayTeamInteractor(teamGateway, presenter);
    }

    /**
     * When a team is already saved, the interactor should pass it directly
     * to the presenter without creating a new empty team.
     */
    @Test
    void testExistingTeamIsReturnedToPresenter() {
        // create a team with 15 real players
        List<Player> players = createDummyPlayers(15);
        Team existingTeam = new Team(players, 80.0f, true);
        teamGateway.setTeam(existingTeam);

        // execute use case (inputData is unused, so null).
        interactor.execute(null);

        // presenter should have received the same team
        assertNotNull(presenter.capturedOutput, "Presenter should have been called with output data.");
        Team outTeam = presenter.capturedOutput.getTeam();
        assertNotNull(outTeam, "Output team should not be null when a team exists.");
        assertSame(existingTeam, outTeam, "Interactor should return the team from the gateway unchanged.");

        // no failure should be reported
        assertNull(presenter.capturedError, "No error message should be reported for a successful load.");
    }

    /**
     * When no team exists in the data access,
     * the interactor should create an empty team with 15 null players,
     * budget 100.0, and squad not confirmed.
     */
    @Test
    void testCreatesEmptyTeamWhenNoneExists() {
        // gateway returns null by default (no team saved).

        interactor.execute(null);

        assertNotNull(presenter.capturedOutput, "Presenter should have been called with output data.");
        Team outTeam = presenter.capturedOutput.getTeam();
        assertNotNull(outTeam, "Interactor should create a new empty team when none exists.");

        // check players list if 15 slots all null
        assertNotNull(outTeam.getPlayers(), "Players list in team should not be null.");
        assertEquals(15, outTeam.getPlayers().size(),
                "Empty team should contain exactly 15 player slots.");
        assertTrue(outTeam.getPlayers().stream().allMatch(Objects::isNull),
                "All player slots should be null in an empty team.");

        // check default budget and confirmation flag
        assertEquals(100.0f, outTeam.getBudget(), 1e-6,
                "Empty team should start with a budget of 100.0.");
        assertFalse(outTeam.isConfirmed(),
                "Empty team should not be confirmed.");

        // no failure should be reported
        assertNull(presenter.capturedError, "No error message should be reported for a successful load.");
    }

    /**
     * If the data access layer throws an exception,
     * the interactor should call presentFailure with an appropriate message.
     */
    @Test
    void testGatewayExceptionTriggersFailure() {
        // use a failing gateway that always throws
        DisplayTeamDataAccessInterface failingGateway = new DisplayTeamDataAccessInterface() {
            @Override
            public Team getTeam() {
                throw new RuntimeException("DB down");
            }
        };
        DisplayTeamInteractor failingInteractor = new DisplayTeamInteractor(failingGateway, presenter);

        failingInteractor.execute(null);

        // no team output, but failure message should be present.
        assertNull(presenter.capturedOutput, "No team should be presented when an exception occurs.");
        assertNotNull(presenter.capturedError, "Failure message should be presented when an exception occurs.");
        assertTrue(presenter.capturedError.contains("Failed to load team"),
                "Failure message should contain a generic 'Failed to load team' prefix.");
        assertTrue(presenter.capturedError.contains("DB down"),
                "Failure message should include the original exception message for debugging.");
    }

    /**
     * helper method for creating a list of dummy players.
     */
    private List<Player> createDummyPlayers(int count) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Player p = new Player(
                    i + 1,
                    "Player_" + i,
                    (i % 4) + 1,
                    "a",
                    5.0,
                    5,
                    "Team_" + (i % 3),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>()
            );
            // predicted points are not used here, but we can set something simple
            p.calculatePredictedPoints(Map.of("intercept", 10.0 + i));
            players.add(p);
        }
        return players;
    }

    /**
     * Simple in-memory Team gateway for testing DisplayTeamInteractor.
     */
    private static class InMemoryTeamGateway implements DisplayTeamDataAccessInterface {

        private Team storedTeam;

        @Override
        public Team getTeam() {
            return storedTeam;
        }

        public void setTeam(Team team) {
            this.storedTeam = team;
        }
    }

    /**
     * Output boundary implementation that captures calls for assertions.
     */
    private static class CapturingPresenter implements DisplayTeamOutputBoundary {

        DisplayTeamOutputData capturedOutput;
        String capturedError;

        @Override
        public void presentTeam(DisplayTeamOutputData outputData) {
            this.capturedOutput = outputData;
        }

        @Override
        public void presentFailure(String message) {
            this.capturedError = message;
        }
    }
}
