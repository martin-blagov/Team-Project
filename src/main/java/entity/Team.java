package entity;
import java.util.List;

/**
 * An entity representing a fantasy premier league team.
 */
public class Team {
    private final List<Player> players;
    private final float budget;
    private final boolean isConfirmed;

    /**
     * Creates a new team with the given players, budget, and confirmation status.
     * @param players the list of players in the team (can contain nulls for empty slots, max 15)
     * @param budget the remaining budget for the team
     * @param isConfirmed whether the team has been finalized (will be set to false if team is incomplete)
     * @throws IllegalArgumentException if players is null, has more than 15 entries, or if budget is negative
     */
    public Team(List<Player> players, float budget, boolean isConfirmed) {
        if (players == null) {
            throw new IllegalArgumentException("Players list can't be null");
        }
        if (players.size() > 15) {
            throw new IllegalArgumentException("Team can't have more than 15 players");
        }
        if (budget < 0) {
            throw new IllegalArgumentException("Budget can't be negative");
        }

        this.players = players;
        this.budget = budget;

        // Only allow isConfirmed = true if team is complete (all 15 slots filled with non-null players)
        this.isConfirmed = isConfirmed && isComplete();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public float getBudget() {
        return budget;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    /**
     * Check if the team is complete (all 15 slots filled with non-null players).
     * @return true if all 15 players are present and non-null, false otherwise
     */
    private boolean isComplete() {
        if (players.size() != 15) {
            return false;
        }
        for (Player player : players) {
            if (player == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the number of filled slots (non-null players).
     * @return count of non-null players
     */
    public int getFilledSlots() {
        int count = 0;
        for (Player player : players) {
            if (player != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the number of empty slots (null players).
     * @return count of null players
     */
    public int getEmptySlots() {
        return players.size() - getFilledSlots();
    }
}