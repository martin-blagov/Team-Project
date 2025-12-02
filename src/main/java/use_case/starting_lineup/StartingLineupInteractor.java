package use_case.starting_lineup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import entity.Player;
import entity.Team;
import use_case.PlayerDataAccessInterface;

/**
 * Use case interactor for computing an optimal starting lineup from the user's team
 * based on predicted points.
 */
public class StartingLineupInteractor implements StartingLineupInputBoundary {

    private final StartingLineupOutputBoundary outputBoundary;
    private final StartingLineupTeamDataAccessInterface teamDataAccess;
    private final PlayerDataAccessInterface playerDataAccess;

    /**
     * Constructs a new interactor for the Starting Lineup use case.
     *
     * @param outputBoundary    presenter used to display the computed lineup
     * @param teamDataAccess    gateway for reading the user's saved team
     * @param playerDataAccess  gateway for reading player data
     */
    public StartingLineupInteractor(StartingLineupOutputBoundary outputBoundary,
                                    StartingLineupTeamDataAccessInterface teamDataAccess,
                                    PlayerDataAccessInterface playerDataAccess) {
        this.outputBoundary = outputBoundary;
        this.teamDataAccess = teamDataAccess;
        this.playerDataAccess = playerDataAccess;
    }

    @Override
    public void execute() {
        // Access the user's team.
        final Team savedTeam = teamDataAccess.getTeam();

        // Empty starting lineup if user has no team saved.
        if (savedTeam == null) {
            outputBoundary.presentLineup(new StartingLineupOutputData(null, new ArrayList<>()));
            return;
        }

        // Get all players in the user's team.
        final List<Player> teamPlayers = getPrediction(savedTeam.getPlayers());

        final List<Player> gkp = filterByPosition(teamPlayers, 1);
        final List<Player> dfd = filterByPosition(teamPlayers, 2);
        final List<Player> mdf = filterByPosition(teamPlayers, 3);
        final List<Player> fwd = filterByPosition(teamPlayers, 4);

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

        Team startingTeam = new Team(starting, savedTeam.getBudget(), savedTeam.isConfirmed());

        // Output the starting team and bench list.
        StartingLineupOutputData outputData = new StartingLineupOutputData(startingTeam, bench);
        outputBoundary.presentLineup(outputData);
    }

    /**
     * Get latest prediction data for players objects in the given list from the player data gateway.
     *
     * @param players     list of players to get data.
     * @return list of player objects with prediction data.
     */
    private List<Player> getPrediction(List<Player> players) {
        if (players == null) {
            return new ArrayList<>();
        }

        List<Player> result = new ArrayList<>(players.size());
        for (Player player : players) {
            Player current = playerDataAccess.getPlayerById(player.getId());
            if (current != null && current.getPredictedPoints() != null) {
                result.add(current);
            }
            else {
                result.add(player);
            }
        }
        return result;
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
                .filter(player -> player.getElementType() == elementType)
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
                .sorted(Comparator.comparing(Player::getPredictedPoints,
                        Comparator.nullsLast(Double::compareTo)).reversed())
                .collect(Collectors.toList());
        for (int i = 0; i < Math.min(count, sorted.size()); i++) {
            dest.add(sorted.get(i));
        }
    }
}
