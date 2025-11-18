package use_case.initialise_predictions;

/**
 * Output Data for Initialize Predictions use case.
 * Contains summary information about initialization.
 */
public class InitialisePredictionsOutputData {
    private final int totalPlayersProcessed;
    private final int gameweeksProcessed;

    public InitialisePredictionsOutputData(int totalPlayersProcessed,
                                           int gameweeksProcessed) {
        this.totalPlayersProcessed = totalPlayersProcessed;
        this.gameweeksProcessed = gameweeksProcessed;
    }

    public int getTotalPlayersProcessed() {
        return totalPlayersProcessed;
    }

    public int getGameweeksProcessed() {
        return gameweeksProcessed;
    }
}