package use_case.team_entry;

import entity.Player;
import entity.Team;
import use_case.PlayerDataAccessInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamEntryInteractor implements TeamEntryInputBoundary {

    private final TeamEntryOutputBoundary presenter;
    private final PlayerDataAccessInterface dataAccess;
    private final TeamDataAccessInterface teamDataAccess;

    public TeamEntryInteractor(TeamEntryOutputBoundary presenter, PlayerDataAccessInterface dataAccess, TeamDataAccessInterface teamDataAccess) {
        this.presenter = presenter;
        this.dataAccess = dataAccess;
        this.teamDataAccess = teamDataAccess;
    }

    public void execute(TeamEntryInputData inputData) {

        String[] players = inputData.getPlayerNames();
        int[] ids = inputData.getPlayerIds();
        String budget = inputData.getRemainingBudet();

        boolean noEmptyFields = validateNoEmptyFields(players);
        boolean playerExists = validatePlayersExist(ids);
        boolean isDuplicate = validateNonDuplicate(inputData.getPlayerIds());

        List<Player> enteredPlayerObjects = new ArrayList<>();

        // Create arraylist of player objects that match entered players
        for (int i = 0; i < ids.length; i++) {
            int actualId = ids[i];
            Player p = dataAccess.getPlayerById(actualId);
            enteredPlayerObjects.add(p);
        }

        boolean validBudget = validateBudget(budget);
        boolean correctPositions = validatePositions(enteredPlayerObjects);

        // Order of checks: empty fields, exists, duplicate, positions, budget
        if (!noEmptyFields) {
            presenter.prepareFailView("Please fill in all 15 player fields before confirming your team.");
            return;
        } else if (!playerExists) {
            presenter.prepareFailView("One or more entered players do not exist. Please check spelling and try again.");
            return;
        } else if (isDuplicate) {
            presenter.prepareFailView("You have entered duplicate players. Please ensure each player appears only once.");
            return;
        } else if (!correctPositions) {
            presenter.prepareFailView("One or more players are not in the correct position slots. Please check the lineup format.");
            return;
        } else if (!validBudget) {
            presenter.prepareFailView("The remaining budget is not a valid number. Please try again.");
            return;
        } else {
            // New Team object
            Team confirmedTeam = new Team(
                    enteredPlayerObjects,
                    Float.parseFloat(budget), // get player's entered remaining budget
                    true
            );

            // Save it permanently
            teamDataAccess.saveTeam(confirmedTeam);

            presenter.prepareSuccessView(new TeamEntryOutputData(players));
            return;
        }
    }

    @Override
    public void openPage() {
        Team saved = teamDataAccess.getTeam();

        if (saved != null && saved.getPlayers() != null && !saved.getPlayers().isEmpty()) {

            String[] names = new String[saved.getPlayers().size()];
            int[] ids = new int[saved.getPlayers().size()];

            for (int i = 0; i < saved.getPlayers().size(); i++) {
                Player p = saved.getPlayers().get(i);
                names[i] = p.getWebName();
                ids[i] = p.getId();
            }

            presenter.prepareSavedTeamView(names, ids);
        }
        else {
            presenter.prepareOpenPageView(); // empty view as usual
        }
    }


    @Override
    public void switchToHomePage() {
        presenter.switchToHomePage();
    }

    private boolean validateNoEmptyFields(String[] playerNames) {
        for (String name : playerNames) {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean validatePlayersExist(int[] ids) {
        for (int id : ids) {
            if (id == -1) return false; // no player selected
            if (dataAccess.getPlayerById(id) == null) return false; // invalid ID
        }
        return true;
    }

    private boolean validateNonDuplicate(int[] ids) {
        Set<Integer> seen = new HashSet<>();
        for (int id : ids) {
            if (seen.contains(id)) {
                return true;
            }
            seen.add(id);
        }
        return false;
    }

    private boolean validatePositions(List<Player> enteredPlayerObjects) {

        // Expected position slots
        String[] requiredPositions = new String[]{
                "forward", "forward", "forward",
                "midfielder", "midfielder", "midfielder", "midfielder", "midfielder",
                "defender", "defender", "defender", "defender", "defender",
                "goalkeeper", "goalkeeper"
        };

        for (int i = 0; i < enteredPlayerObjects.size(); i++) {
            Player p = enteredPlayerObjects.get(i);
            String required = requiredPositions[i];

            if (p == null) {
                return false;
            }

            String actual = p.getPosition(); // already "forward" / "defender" / etc.

            if (!actual.equalsIgnoreCase(required)) {
                return false; // wrong position in this slot
            }
        }
        return true; // all positions correct
    }

    private boolean validateBudget(String remainingBudget) {
        try {
            Integer.parseInt(remainingBudget);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}