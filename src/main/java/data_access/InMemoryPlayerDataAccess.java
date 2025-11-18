package data_access;

import entity.Player;
import use_case.PlayerDataAccessInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory storage for player data.
 * Stores all players in an ArrayList and provides query methods.
 */
public class InMemoryPlayerDataAccess implements PlayerDataAccessInterface {

    private List<Player> players;

    public InMemoryPlayerDataAccess() {
        this.players = new ArrayList<>();
    }

    @Override
    public void saveAll(List<Player> players) {
        // Store a copy to prevent external modification
        this.players = new ArrayList<>(players);
    }

    @Override
    public List<Player> getAllPlayers() {
        // Return a copy to prevent external modification
        return new ArrayList<>(players);
    }

    @Override
    public Player getPlayerById(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;  // Not found
    }

    @Override
    public List<Player> getPlayersByPosition(int position) {
        List<Player> result = new ArrayList<>();
        for (Player player : players) {
            if (player.getElementType() == position) {
                result.add(player);
            }
        }
        return result;
    }

    @Override
    public List<Player> getPlayersByTeam(String teamName) {
        List<Player> result = new ArrayList<>();
        String searchTeam = teamName.toLowerCase();  // Case-insensitive search

        for (Player player : players) {
            if (player.getTeamName().toLowerCase().contains(searchTeam)) {
                result.add(player);
            }
        }

        return result;
    }

    @Override
    public List<Player> getTopPlayersByPosition(int position, int limit) {
        List<Player> filtered = players.stream()
                .filter(p -> p.getElementType() == position)
                .filter(p -> p.getPredictedPoints() != null)  // Only players with predictions
                .sorted((a, b) -> Double.compare(b.getPredictedPoints(), a.getPredictedPoints()))
                .collect(Collectors.toList());

        // If limit is -1, return all; otherwise limit
        if (limit == -1) {
            return filtered;
        }
        return filtered.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<Player> getTopPlayers(int limit) {
        List<Player> filtered = players.stream()
                .filter(p -> p.getPredictedPoints() != null)  // Only players with predictions
                .sorted((a, b) -> Double.compare(b.getPredictedPoints(), a.getPredictedPoints()))
                .collect(Collectors.toList());

        // If limit is -1, return all; otherwise limit
        if (limit == -1) {
            return filtered;
        }
        return filtered.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<Player> getPlayersByName(String name) {
        List<Player> result = new ArrayList<>();
        String searchName = name.toLowerCase();  // Case-insensitive search

        for (Player player : players) {
            if (player.getWebName().toLowerCase().contains(searchName)) {
                result.add(player);
            }
        }

        return result;
    }
}