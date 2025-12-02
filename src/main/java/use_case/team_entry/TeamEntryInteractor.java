package use_case.team_entry;

import entity.Player;
import entity.Team;

import java.util.*;

public class TeamEntryInteractor implements TeamEntryInputBoundary {

    private final TeamEntryOutputBoundary presenter;
    private final TeamDataAccessInterface teamDataAccess;

    public TeamEntryInteractor(
            TeamEntryOutputBoundary presenter,
            TeamDataAccessInterface teamDataAccess
    ) {
        this.presenter = presenter;
        this.teamDataAccess = teamDataAccess;
    }

    @Override
    public void execute(TeamEntryInputData inputData) {

        final String[] players = inputData.getPlayerNames();
        final int[] ids = inputData.getPlayerIds();
        final String[] positions = inputData.getPlayerPositions();
        final String budget = inputData.getRemainingBudget();

        final boolean noEmptyFields = validateNoEmptyFields(players);
        final boolean isDuplicate = validateNonDuplicate(ids);
        final boolean validBudget = validateBudget(budget);

        final List<Player> enteredPlayerObjects = new ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            enteredPlayerObjects.add(
                    new Player(
                            ids[i],
                            players[i],
                            -1,
                            "a",
                            0.0,
                            mapPositionToInt(positions[i]),
                            "ClubX",
                            new HashMap<>(),
                            new HashMap<>(),
                            new HashMap<>(),
                            new HashMap<>()
                    )
            );
        }

        final boolean correctPositions =
                validatePositions(enteredPlayerObjects);

        if (!noEmptyFields) {
            presenter.prepareFailView(
                    "Please fill in all 15 player fields before confirming your team."
            );
            return;
        }

        if (isDuplicate) {
            presenter.prepareFailView(
                    "You have entered duplicate players. Please ensure each player appears only once."
            );
            return;
        }

        if (!correctPositions) {
            presenter.prepareFailView(
                    "One or more players are not in the correct position slots. Please check the lineup format."
            );
            return;
        }

        if (!validBudget) {
            presenter.prepareFailView(
                    "The remaining budget is not a valid number. Please try again."
            );
            return;
        }

        final Team confirmedTeam = new Team(
                enteredPlayerObjects,
                Float.parseFloat(budget),
                true
        );

        teamDataAccess.saveTeam(confirmedTeam);

        presenter.prepareSuccessView(new TeamEntryOutputData(players));
    }

    @Override
    public void openPage() {
        final Team saved = teamDataAccess.getTeam();

        if (saved != null && saved.getPlayers() != null && !saved.getPlayers().isEmpty()) {

            final String[] names = new String[saved.getPlayers().size()];
            final int[] ids = new int[saved.getPlayers().size()];

            for (int i = 0; i < saved.getPlayers().size(); i++) {
                final Player p = saved.getPlayers().get(i);
                names[i] = p.getWebName();
                ids[i] = p.getId();
            }

            presenter.prepareSavedTeamView(names, ids);
        }
        else {
            presenter.prepareOpenPageView();
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

    private boolean validateNonDuplicate(int[] ids) {
        final Set<Integer> seen = new HashSet<>();
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
        final String[] requiredPositions = new String[]{
                "forward", "forward", "forward",
                "midfielder", "midfielder", "midfielder", "midfielder", "midfielder",
                "defender", "defender", "defender", "defender", "defender",
                "goalkeeper", "goalkeeper"
        };

        // Safety check
        if (enteredPlayerObjects.size() != requiredPositions.length) {
            return false;
        }

        for (int i = 0; i < enteredPlayerObjects.size(); i++) {
            final Player p = enteredPlayerObjects.get(i);
            final String required = requiredPositions[i];

            if (p == null || p.getPosition() == null) {
                return false;
            }

            final String actual = p.getPosition();

            if (!actual.equalsIgnoreCase(required)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateBudget(String remainingBudget) {
        if (remainingBudget == null || remainingBudget.trim().isEmpty()) {
            return false;
        }
        try {
            Float.parseFloat(remainingBudget);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private int mapPositionToInt(String position) {
        if (position == null) {
            return -1;
        }

        final String p = position.toLowerCase();

        if (p.equals("goalkeeper")) {
            return 1;
        }
        else if (p.equals("defender")) {
            return 2;
        }
        else if (p.equals("midfielder")) {
            return 3;
        }
        else if (p.equals("forward")) {
            return 4;
        }
        else {
            return -1;
        }
    }
}