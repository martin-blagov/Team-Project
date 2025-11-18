package interface_adapter.initialise_predictions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * ViewModel for tracking initialization status.
 * This runs at startup and tracks loading state.
 */
public class InitialisePredictionsViewModel {
    private boolean isLoading;
    private boolean isComplete;
    private String statusMessage;
    private int totalPlayers;
    private int gameweeksProcessed;

    private final PropertyChangeSupport support;

    public InitialisePredictionsViewModel() {
        this.support = new PropertyChangeSupport(this);
        this.isLoading = false;
        this.isComplete = false;
        this.statusMessage = "Initializing...";
    }

    // Getters
    public boolean isLoading() {
        return isLoading;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public int getGameweeksProcessed() {
        return gameweeksProcessed;
    }

    // Setters with property change support
    public void setLoading(boolean loading) {
        boolean oldValue = this.isLoading;
        this.isLoading = loading;
        support.firePropertyChange("loading", oldValue, loading);
    }

    public void setComplete(boolean complete) {
        boolean oldValue = this.isComplete;
        this.isComplete = complete;
        support.firePropertyChange("complete", oldValue, complete);
    }

    public void setStatusMessage(String message) {
        String oldValue = this.statusMessage;
        this.statusMessage = message;
        support.firePropertyChange("status", oldValue, message);
    }

    public void setStats(int totalPlayers, int gameweeksProcessed) {
        this.totalPlayers = totalPlayers;
        this.gameweeksProcessed = gameweeksProcessed;
        support.firePropertyChange("stats", null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}