package use_case.team_entry;

import data_access.InMemoryPlayerDataAccess;
import data_access.InMemoryTeamDataAccess;
import entity.Player;
import entity.Team;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TeamEntryInteractorTest {

    // create a player for testing
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

    // create the correct 15 player lineup
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

    // convert list of players to array of player names
    private String[] namesOf(List<Player> players) {
        String[] names = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            names[i] = players.get(i).getWebName();
        }
        return names;
    }

    // list of players to array of player ids
    private int[] idsOf(List<Player> players) {
        int[] ids = new int[players.size()];
        for (int i = 0; i < players.size(); i++) {
            ids[i] = players.get(i).getId();
        }
        return ids;
    }

    @Test
    void testEmptyFieldsFailure() {
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        // load valid players
        List<Player> valid = makeValid15Players();
        playerRepo.saveAll(valid);

        // make one field empty
        String[] names = namesOf(valid);
        names[3] = "";

        TeamEntryInputData input = new TeamEntryInputData(names, idsOf(valid), "10");

        // presenter expecting failure
        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("Please fill in all 15 player fields before confirming your team.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail("Unexpected"); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).execute(input);
    }

    @Test
    void testInvalidPlayerFailure() {
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();
        playerRepo.saveAll(valid);

        // set one id to invalid
        int[] ids = idsOf(valid);
        ids[5] = -1;

        TeamEntryInputData input = new TeamEntryInputData(namesOf(valid), ids, "10");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("One or more entered players do not exist. Please check spelling and try again.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail("Unexpected"); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).execute(input);
    }

    @Test
    void testDuplicatePlayersFailure() {
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();
        playerRepo.saveAll(valid);

        // make one duplicate
        int[] ids = idsOf(valid);
        ids[2] = ids[0];

        TeamEntryInputData input = new TeamEntryInputData(namesOf(valid), ids, "10");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("You have entered duplicate players. Please ensure each player appears only once.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail("Unexpected"); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).execute(input);
    }

    @Test
    void testWrongPositionFailure() {
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();
        playerRepo.saveAll(valid);

        // replace forward with goalkeeper
        Player wrong = makePlayer(1, "WRONG", 1);
        List<Player> modified = new ArrayList<>(valid);
        modified.set(0, wrong);
        playerRepo.saveAll(modified);

        TeamEntryInputData input = new TeamEntryInputData(namesOf(modified), idsOf(modified), "10");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("One or more players are not in the correct position slots. Please check the lineup format.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail("Unexpected"); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).execute(input);
    }

    @Test
    void testInvalidBudgetFailure() {
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();
        playerRepo.saveAll(valid);

        // budget is not a number
        TeamEntryInputData input = new TeamEntryInputData(namesOf(valid), idsOf(valid), "abc");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareFailView(String error) {
                assertEquals("The remaining budget is not a valid number. Please try again.", error);
            }
            @Override public void prepareSuccessView(TeamEntryOutputData d) { fail("Unexpected"); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).execute(input);
    }

    @Test
    void testSuccessCase() {
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();

        List<Player> valid = makeValid15Players();
        playerRepo.saveAll(valid);

        TeamEntryInputData input = new TeamEntryInputData(namesOf(valid), idsOf(valid), "5");

        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override
            public void prepareSuccessView(TeamEntryOutputData out) {
                // team should be saved
                Team saved = teamRepo.getTeam();
                assertNotNull(saved);
                assertEquals(15, saved.getPlayers().size());
                assertEquals(5.0f, saved.getBudget());
            }

            @Override public void prepareFailView(String s) { fail("Unexpected"); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareOpenPageView() {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).execute(input);
    }

    @Test
    void testOpenPage_NoSavedTeam() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();

        // no saved team, should open empty page
        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void prepareOpenPageView() { assertTrue(true); }
            @Override public void prepareSavedTeamView(String[] n, int[] i) { fail("Unexpected"); }
            @Override public void prepareSuccessView(TeamEntryOutputData d) {}
            @Override public void prepareFailView(String s) {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).openPage();
    }

    @Test
    void testOpenPage_LoadSavedTeam() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();

        // create a saved team
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
            @Override public void prepareOpenPageView() { fail("Unexpected"); }
            @Override public void prepareSuccessView(TeamEntryOutputData d) {}
            @Override public void prepareFailView(String s) {}
            @Override public void switchToHomePage() {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).openPage();
    }

    @Test
    void testSwitchToHomePage() {
        InMemoryTeamDataAccess teamRepo = new InMemoryTeamDataAccess();
        InMemoryPlayerDataAccess playerRepo = new InMemoryPlayerDataAccess();

        // check presenter method gets called
        TeamEntryOutputBoundary presenter = new TeamEntryOutputBoundary() {
            @Override public void switchToHomePage() { assertTrue(true); }
            @Override public void prepareOpenPageView() {}
            @Override public void prepareSavedTeamView(String[] n, int[] i) {}
            @Override public void prepareSuccessView(TeamEntryOutputData d) {}
            @Override public void prepareFailView(String s) {}
        };

        new TeamEntryInteractor(presenter, playerRepo, teamRepo).switchToHomePage();
    }
}
