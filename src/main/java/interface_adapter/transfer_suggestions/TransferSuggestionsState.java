package interface_adapter.transfer_suggestions;

import entity.Team;
import use_case.transfer_suggestions.TransferSuggestionsOutputData;

import java.util.ArrayList;
import java.util.List;

/**
 * State class for Transfer Suggestions view.
 * Holds all data needed to display transfer suggestions to the user.
 */
public class TransferSuggestionsState {
    private int numberOfTransfers;
    private Team originalTeam;
    private Team suggestedTeam;
    private List<TransferSuggestionsOutputData.PlayerSwap> swaps;
    private double totalPointsImprovement;
    private String errorMessage;
    private String successMessage;
    private boolean isLoading;

    /**
     * Initialize with default values.
     */
    public TransferSuggestionsState() {
        this.numberOfTransfers = 1;
        this.swaps = new ArrayList<>();
        this.totalPointsImprovement = 0.0;
        this.isLoading = false;
    }

    // Getters
    public int getNumberOfTransfers() {
        return numberOfTransfers;
    }

    public Team getOriginalTeam() {
        return originalTeam;
    }

    public Team getSuggestedTeam() {
        return suggestedTeam;
    }

    public List<TransferSuggestionsOutputData.PlayerSwap> getSwaps() {
        return swaps;
    }

    public double getTotalPointsImprovement() {
        return totalPointsImprovement;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public boolean isLoading() {
        return isLoading;
    }

    // Setters
    public void setNumberOfTransfers(int numberOfTransfers) {
        this.numberOfTransfers = numberOfTransfers;
    }

    public void setOriginalTeam(Team originalTeam) {
        this.originalTeam = originalTeam;
    }

    public void setSuggestedTeam(Team suggestedTeam) {
        this.suggestedTeam = suggestedTeam;
    }

    public void setSwaps(List<TransferSuggestionsOutputData.PlayerSwap> swaps) {
        this.swaps = swaps;
    }

    public void setTotalPointsImprovement(double totalPointsImprovement) {
        this.totalPointsImprovement = totalPointsImprovement;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
    }
}