package use_case.display_team;

import entity.Player;
import entity.Team;
import use_case.TeamDataAccessInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Interactor for Display Team use case.
 *
 * RESPONSIBILITIES:
 * 1. Fetch team from TeamDataAccessInterface
 * 2. If no team exists, create empty team (15 nulls)
 * 3. Pass team to Presenter
 */
public class DisplayTeamInteractor implements DisplayTeamInputBoundary {

    private final TeamDataAccessInterface teamDataAccess;
    private final DisplayTeamOutputBoundary presenter;

    public DisplayTeamInteractor(TeamDataAccessInterface teamDataAccess,
                                 DisplayTeamOutputBoundary presenter) {
        this.teamDataAccess = teamDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(DisplayTeamInputData inputData) {
        try {
            // Step 1: Try to get the saved team
            Team team = teamDataAccess.getTeam();

            // Step 2: If no team exists, create empty team
            if (team == null) {
                team = createEmptyTeam();
            }

            // Step 3: Create output data
            DisplayTeamOutputData outputData = new DisplayTeamOutputData(team);

            // Step 4: Pass to presenter
            presenter.presentTeam(outputData);

        } catch (Exception e) {
            // If something goes wrong, present failure
            presenter.presentFailure("Failed to load team: " + e.getMessage());
        }
    }

    /**
     * Create an empty team with all 15 slots as null.
     * @return Team with 15 null players, budget 100.0, not confirmed
     */
    private Team createEmptyTeam() {
        List<Player> emptyPlayers = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            emptyPlayers.add(null);
        }
        return new Team(emptyPlayers, 100.0f, false);
    }
}