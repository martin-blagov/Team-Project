package use_case.starting_lineup;

import entity.Player;
import entity.Team;
import use_case.TeamDataAccessInterface;

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
    private final TeamDataAccessInterface teamDataAccess;

    /**
     * Constructs a new interactor for the Starting Lineup use case.
     *
     * @param outputBoundary    presenter used to display the computed lineup
     * @param teamDataAccess    gateway for reading the user's saved team
     */
    public StartingLineupInteractor(StartingLineupOutputBoundary outputBoundary,
                                    TeamDataAccessInterface teamDataAccess) {
        this.outputBoundary = outputBoundary;
        this.teamDataAccess = teamDataAccess;
    }

    @Override
    public void execute() {
        // Access the user's team.
        Team savedTeam = teamDataAccess.getTeam();

        // Empty starting lineup if user has no team saved.
        if (savedTeam == null) {
            outputBoundary.presentLineup(new StartingLineupOutputData(null, new ArrayList<>()));
            return;
        }

        // Get all players in the user's team.
        List<Player> teamPlayers = savedTeam.getPlayers();

        List<Player> gkp = filterByPosition(teamPlayers, 1);
        List<Player> dfd = filterByPosition(teamPlayers, 2);
        List<Player> mdf = filterByPosition(teamPlayers, 3);
        List<Player> fwd = filterByPosition(teamPlayers, 4);

        List<Player> starting = new ArrayList<>();

        // Guarantees at least 1 GK, 3 DEF, 2 MID, 1 FWD.
        addTopPlayers(gkp, starting, 1);
        addTopPlayers(dfd, starting, 3);
        addTopPlayers(mdf, starting, 2);
        addTopPlayers(fwd, starting, 1);

        // Fill remaining spots with the best remaining players.
        List<Player> remaining = new ArrayList<>(teamPlayers);
        remaining.removeAll(starting);
        addTopPlayers(remaining, starting, 11 - starting.size());

        List<Player> bench = new ArrayList<>(teamPlayers);
        bench.removeAll(starting);

        Team startingTeam = new Team(starting, 0.0f, true);

        // Output the starting team and bench list.
        StartingLineupOutputData outputData = new StartingLineupOutputData(startingTeam, bench);
        outputBoundary.presentLineup(outputData);
    }

    /**
     * Returns a new list containing players of the desired position.
     *
     * @param players     list of players to filter.
     * @param elementType position identifier.
     * @return list of players that matches the given position.
     */
    private List<Player> filterByPosition(List<Player> players, int elementType) {
        return players.stream()
                .filter(p -> p.getElementType() == elementType)
                .collect(Collectors.toList());
    }

    /**
     * Add players with the highest predicted points from source to dest.
     *
     * @param source the players to add from.
     * @param dest   the collection to add top players.
     * @param count  maximum number of players to add.
     */
    private void addTopPlayers(List<Player> source, List<Player> dest, int count) {
        List<Player> sorted = source.stream()
                .sorted(Comparator.comparing(Player::getPredictedPoints).reversed())
                .collect(Collectors.toList());
        for (int i = 0; i < Math.min(count, sorted.size()); i++) {
            dest.add(sorted.get(i));
        }
    }
}