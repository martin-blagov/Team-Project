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
        // Create a team for test purposes.
        List<Player> allPlayers = dataAccess.getAllPlayers();
        List<Player> candidates = allPlayers.stream()
                .filter(p -> p.getPredictedPoints() != null)
                .collect(Collectors.toList());
        candidates.sort(Comparator.comparing(Player::getPredictedPoints).reversed());
        List<Player> players_in_team = new ArrayList<>();

        List<Player> goalkeepers = filterByPosition(candidates, 1);
        List<Player> defenders = filterByPosition(candidates, 2);
        List<Player> midfielders = filterByPosition(candidates, 3);
        List<Player> forwards = filterByPosition(candidates, 4);

        for (int i = 0; i < Math.min(2, goalkeepers.size()); i++) {
            players_in_team.add(defenders.get(i));
        }
        for (int i = 0; i < Math.min(5, defenders.size()); i++) {
            players_in_team.add(defenders.get(i));
        }
        for (int i = 0; i < Math.min(5, midfielders.size()); i++) {
            players_in_team.add(midfielders.get(i));
        }
        for (int i = 0; i < Math.min(3, forwards.size()); i++) {
            players_in_team.add(forwards.get(i));
        }

        Team testTeam = new Team(players_in_team, 0.0f, true);

        // Starting lineup computation.

        List<Player> teamPlayers = testTeam.getPlayers();

        List<Player> gkp = filterByPosition(candidates, 1);
        List<Player> dfd = filterByPosition(candidates, 2);
        List<Player> mdf = filterByPosition(candidates, 3);
        List<Player> fwd = filterByPosition(candidates, 4);

        List<Player> starting = new ArrayList<>();

        addTopPlayers(gkp, starting, 1);
        addTopPlayers(dfd, starting, 3);
        addTopPlayers(mdf, starting, 2);
        addTopPlayers(fwd, starting, 1);

        List<Player> remaining = new ArrayList<>(teamPlayers);
        remaining.removeAll(starting);

        addTopPlayers(remaining, starting, 11 - starting.size());

        if(starting.isEmpty()) {
            outputBoundary.presentLineup(new StartingLineupOutputData(null, new ArrayList<>()));
            return;
        }

        List<Player> bench = testTeam.getPlayers();
        bench.removeAll(starting);

        Team startingTeam = new Team(starting, 0.0f, true);

        // Output the starting team and bench list.
        StartingLineupOutputData outputData = new StartingLineupOutputData(startingTeam, bench);
        outputBoundary.presentLineup(outputData);
    }

    private List<Player> filterByPosition(List<Player> players, int elementType) {
        return players.stream()
                .filter(p -> p.getElementType() == elementType)
                .collect(Collectors.toList());
    }

    private void addTopPlayers(List<Player> source, List<Player> dest, int count) {
        List<Player> sorted = source.stream()
                .sorted(Comparator.comparing(Player::getPredictedPoints).reversed())
                .collect(Collectors.toList());
        for (int i = 0; i < Math.min(count, sorted.size()); i++) {
            dest.add(sorted.get(i));
        }
    }
}