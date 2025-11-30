package use_case.team_entry;

import data_access.InMemoryTeamDataAccess;
import entity.Player;
import entity.Team;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TeamEntryInteractorTest {

    private Player makePlayer(int id, String name, int positionInt) {
        return new Player(
                id,
                name,
                positionInt,
                "a",
                5.0,
                positionInt,
                "ClubX",
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        );
    }

    private List<Player> makeValid15Players() {
        return List.of(
                makePlayer(1,  "F1", 4),
                makePlayer(2,  "F2", 4),
                makePlayer(3,  "F3", 4),

                makePlayer(4,  "M1", 3),
                makePlayer(5,  "M2", 3),
                makePlayer(6,  "M3", 3),
                makePlayer(7,  "M4", 3),
                makePlayer(8,  "M5", 3),

                makePlayer(9,  "D1", 2),
                makePlayer(10, "D2", 2),
                makePlayer(11, "D3", 2),
                makePlayer(12, "D4", 2),
                makePlayer(13, "D5", 2),

                makePlayer(14, "G1", 1),
                makePlayer(15, "G2", 1)
        );
    }

    private String[] namesOf(List<Player> players) {
        String[] names = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            names[i] = players.get(i).getWebName();
        }
        return names;
    }

    private int[] idsOf(List<Player> players) {
        int[] ids = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            ids[i] = players.get(i).getId();
        }
        return ids;
    }

    private String[] slotPositions() {
        return new String[]{
                "forward", "forward", "forward",
                "midfielder", "midfielder", "midfielder", "midfielder", "midfielder",
                "defender", "defender", "defender", "defender", "defender",
                "goalkeeper", "goalkeeper"
        };
    }

    @Test
    void testEmptyFieldsFailure() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();
        String[] names = namesOf(valid);
        names[3] = "";

        TeamEntryInputData input =
                new TeamEntryInputData(names, idsOf(valid), slotPositions(), "10");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("Please fill in all 15 player fields before confirming your team.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail(); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, teamRepo).execute(input);
    }

    @Test
    void testDuplicatePlayersFailure() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();
        int[] ids = idsOf(valid);
        ids[2] = ids[0];

        TeamEntryInputData input =
                new TeamEntryInputData(namesOf(valid), ids, slotPositions(), "10");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("You have entered duplicate players. Please ensure each player appears only once.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail(); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, teamRepo).execute(input);
    }

    @Test
    void testInvalidBudgetFailure() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();

        TeamEntryInputData input =
                new TeamEntryInputData(namesOf(valid), idsOf(valid), slotPositions(), "abc");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("The remaining budget is not a valid number. Please try again.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail(); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, teamRepo).execute(input);
    }

    @Test
    void testSuccessCase() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();

        TeamEntryInputData input =
                new TeamEntryInputData(namesOf(valid), idsOf(valid), slotPositions(), "5");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override
            public void prepareSuccessView(TeamEntryOutputData out) {
                Team saved = teamRepo.getTeam();
                assertNotNull(saved);
                assertEquals(15, saved.getPlayers().size());
                assertEquals(5.0f, saved.getBudget());
            }

            @Override public void prepareFailView(String s) { fail(); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, teamRepo).execute(input);
    }

    @Test
    void testOpenPage_NoSavedTeam() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareOpenPageView() { assertTrue(true); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) { fail(); }
            @Override public void prepareSuccessView(TeamEntryOutputData d) {}
            @Override public void prepareFailView(String s) {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, teamRepo).openPage();
    }

    @Test
    void testOpenPage_LoadSavedTeam() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> savedPlayers = List.of(
                makePlayer(10, "Saved1", 4),
                makePlayer(11, "Saved2", 3)
        );

        teamRepo.saveTeam(new Team(savedPlayers, 20f, true));

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareSavedTeamView(String[] names, int[] ids) {
                assertEquals("Saved1", names[0]);
                assertEquals(10, ids[0]);
            }
            @Override public void prepareOpenPageView() { fail(); }
            @Override public void prepareSuccessView(TeamEntryOutputData d) {}
            @Override public void prepareFailView(String s) {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, teamRepo).openPage();
    }

    @Test
    void testSwitchToHomePage() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void switchToHomePage() { assertTrue(true); }
            @Override public void prepareOpenPageView() {}
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareSuccessView(TeamEntryOutputData d) {}
            @Override public void prepareFailView(String s) {}
        };

        new TeamEntryInteractor(presenter, teamRepo).switchToHomePage();
    }
}
