package use_case.starting_lineup;

import entity.Player;
import entity.Team;
import use_case.PlayerDataAccessInterface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case interactor for computing an optimal starting lineup from the user's team
 * based on predicted points.
 */
public class StartingLineupInteractor implements StartingLineupInputBoundary {

    private final StartingLineupOutputBoundary outputBoundary;
    private final PlayerDataAccessInterface dataAccess;

    public StartingLineupInteractor(StartingLineupOutputBoundary outputBoundary,
                                    PlayerDataAccessInterface dataAccess) {
        this.outputBoundary = outputBoundary;
        this.dataAccess = dataAccess;
    }

    @Override
    public void execute() {
        // Retrieve all players available to the user (their team/squad).
        List<Player> allPlayers = dataAccess.getAllPlayers();

        // Keep only players with a prediction to avoid nulls when sorting.
        List<Player> candidates = allPlayers.stream()
                .filter(p -> p.getPredictedPoints() != null)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            // No players with predictions: return an empty lineup.
            outputBoundary.presentLineup(new StartingLineupOutputData(null, new ArrayList<>()));
            return;
        }

        // Sort candidate pool by predicted points with the highest first.
        candidates.sort(Comparator.comparing(Player::getPredictedPoints).reversed());

        // Group by position using elementType (1=GK, 2=DEF, 3=MID, 4=FWD).
        List<Player> goalkeepers = filterByPosition(candidates, 1);
        List<Player> defenders = filterByPosition(candidates, 2);
        List<Player> midfielders = filterByPosition(candidates, 3);
        List<Player> forwards = filterByPosition(candidates, 4);

        List<Player> starting = new ArrayList<>();
        List<Player> bench = new ArrayList<>();

        if (!goalkeepers.isEmpty()) {
            starting.add(goalkeepers.get(0));
        }

        for (int i = 0; i < Math.min(4, defenders.size()); i++) {
            starting.add(defenders.get(i));
        }

        for (int i = 0; i < Math.min(3, midfielders.size()); i++) {
            starting.add(midfielders.get(i));
        }

        for (int i = 0; i < Math.min(3, forwards.size()); i++) {
            starting.add(forwards.get(i));
        }

        // Wrap starting players as a Team entity. Budget and confirmation are placeholders.
        Team startingTeam = new Team(starting, 0.0f, true);

        // Output both the starting team and bench list.
        StartingLineupOutputData outputData = new StartingLineupOutputData(startingTeam, bench);
        outputBoundary.presentLineup(outputData);
    }

    private List<Player> filterByPosition(List<Player> players, int elementType) {
        return players.stream()
                .filter(p -> p.getElementType() == elementType)
                .collect(Collectors.toList());
    }
}