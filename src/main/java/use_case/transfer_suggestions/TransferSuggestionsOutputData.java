package use_case.transfer_suggestions;

import entity.Team;
import entity.Player;
import java.util.List;

/**
 * Output Data for the Transfer Suggestions use case.
 * Contains the original team, suggested team after transfers, and swap details.
 */
public class TransferSuggestionsOutputData {
    private final Team originalTeam;
    private final Team suggestedTeam;
    private final List<PlayerSwap> swaps;
    private final double totalPointsImprovement;

    /**
     * Create output data with transfer suggestions.
     *
     * @param originalTeam The user's current team before transfers
     * @param suggestedTeam The team after applying suggested transfers
     * @param swaps List of player swaps (which player out for which player in)
     * @param totalPointsImprovement Total predicted points improvement from all transfers
     */
    public TransferSuggestionsOutputData(Team originalTeam,
                                         Team suggestedTeam,
                                         List<PlayerSwap> swaps,
                                         double totalPointsImprovement) {
        this.originalTeam = originalTeam;
        this.suggestedTeam = suggestedTeam;
        this.swaps = swaps;
        this.totalPointsImprovement = totalPointsImprovement;
    }

    public Team getOriginalTeam() {
        return originalTeam;
    }

    public Team getSuggestedTeam() {
        return suggestedTeam;
    }

    public List<PlayerSwap> getSwaps() {
        return swaps;
    }

    public double getTotalPointsImprovement() {
        return totalPointsImprovement;
    }

    /**
     * Simple data structure representing a single player swap.
     * Not an entity - just a DTO for transferring data.
     */
    public static class PlayerSwap {
        private final Player playerOut;
        private final Player playerIn;

        public PlayerSwap(Player playerOut, Player playerIn) {
            this.playerOut = playerOut;
            this.playerIn = playerIn;
        }

        public Player getPlayerOut() {
            return playerOut;
        }

        public Player getPlayerIn() {
            return playerIn;
        }

        /**
         * Get the cost difference (positive = more expensive, negative = cheaper).
         */
        public double getCostDifference() {
            return playerIn.getNowCost() - playerOut.getNowCost();
        }

        /**
         * Get the predicted points improvement (positive = better, negative = worse).
         */
        public double getPointsImprovement() {
            return playerIn.getPredictedPoints() - playerOut.getPredictedPoints();
        }
    }
}