package use_case.team_entry;

import java.util.*;

import entity.Player;
import entity.Team;

public class TeamEntryInteractor implements TeamEntryInputBoundary {

    private static final String FORWARD = "forward";
    private static final String MIDFIELDER = "midfielder";
    private static final String DEFENDER = "defender";
    private static final String GOALKEEPER = "goalkeeper";

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

        final boolean correctPositions = validatePositions(enteredPlayerObjects);

        String errorMessage = null;

        if (!noEmptyFields) {
            errorMessage = "Please fill in all 15 player fields before confirming your team.";
        }
        else if (isDuplicate) {
            errorMessage = "You have entered duplicate players. Please ensure each player appears only once.";
        }
        else if (!correctPositions) {
            errorMessage = "One or more players are not in the correct position slots. Please check the lineup format.";
        }
        else if (!validBudget) {
            errorMessage = "The remaining budget is not a valid number. Please try again.";
        }

        if (errorMessage != null) {
            presenter.prepareFailView(errorMessage);
        } else {
            final Team confirmedTeam = new Team(
                    enteredPlayerObjects,
                    Float.parseFloat(budget),
                    true
            );

            teamDataAccess.saveTeam(confirmedTeam);
            presenter.prepareSuccessView();
        }
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
        boolean isValid = true;

        for (String name : playerNames) {
            if (name == null || name.trim().isEmpty()) {
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateNonDuplicate(int[] ids) {
        final Set<Integer> seen = new HashSet<>();
        boolean isDuplicate = false;

        for (int id : ids) {
            if (seen.contains(id)) {
                isDuplicate = true;
            } else {
                seen.add(id);
            }
        }

        return isDuplicate;
    }

    @SuppressWarnings({"checkstyle:Indentation", "checkstyle:SuppressWarnings"})
    private boolean validatePositions(List<Player> enteredPlayerObjects) {

        // Expected position slots - note: I'm not able to de-indent the text for some reason, so I suppressed
        // the warning here
        final String[] requiredPositions = new String[]{
                FORWARD, FORWARD, FORWARD, MIDFIELDER, MIDFIELDER,
                MIDFIELDER, MIDFIELDER, MIDFIELDER, DEFENDER, DEFENDER, DEFENDER, DEFENDER, DEFENDER,
                GOALKEEPER, GOALKEEPER};

        // Safety check
        if (enteredPlayerObjects.size() != requiredPositions.length) {
            return false;
        }

        for (int i = 0; i < enteredPlayerObjects.size(); i++) {
            final Player p = enteredPlayerObjects.get(i);

            if (p == null || p.getPosition() == null) {
                return false;
            }

            final String actual = p.getPosition();
            final String required = requiredPositions[i];

            if (!actual.equalsIgnoreCase(required)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateBudget(String remainingBudget) {
        boolean isValid = true;

        if (remainingBudget == null || remainingBudget.trim().isEmpty()) {
            isValid = false;
        }
        else {
            try {
                Float.parseFloat(remainingBudget);
            }
            catch (NumberFormatException err) {
                isValid = false;
            }
        }

        return isValid;
    }

    private int mapPositionToInt(String position) {
        int result = -1;

        if (position != null) {
            final String p = position.toLowerCase();

            if (p.equals(GOALKEEPER)) {
                result = 1;
            }
            else if (p.equals(DEFENDER)) {
                result = 2;
            }
            else if (p.equals(MIDFIELDER)) {
                result = 3;
            }
            else if (p.equals(FORWARD)) {
                result = 4;
            }
        }
        return result;
    }
}