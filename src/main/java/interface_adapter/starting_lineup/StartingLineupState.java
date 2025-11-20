package interface_adapter.starting_lineup;

/**
 * Initializes the state empty starting lineup.
 */
public class StartingLineupState {
    private String statusMessage = "No lineup selected.";

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}

