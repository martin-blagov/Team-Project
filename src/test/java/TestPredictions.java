import data_access.BootstrapDataGateway;
import data_access.GameWeekDataGateway;
import data_access.InMemoryPlayerDataAccess;
import data_access.ModelCoefficientDataGateway;
import entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;
import use_case.PlayerDataAccessInterface;
import use_case.initialise_predictions.*;

import java.util.List;

/**
 * Simple test to initialize predictions and print top 10 players per position.
 * Run this from your test folder to verify the prediction system is working.
 */
public class TestPredictions {

    public static void main(String[] args) {
        try {
            // Create all data access objects
            BootstrapDataAccessInterface bootstrapGateway = new BootstrapDataGateway();
            GameWeekDataAccessInterface gameWeekGateway = new GameWeekDataGateway();
            ModelCoefficientDataAccessInterface modelCoeffGateway = new ModelCoefficientDataGateway();
            PlayerDataAccessInterface playerDataAccess = new InMemoryPlayerDataAccess();

            // Create a simple presenter that just tracks success/failure
            TestPresenter presenter = new TestPresenter();

            // Create the interactor
            InitialisePredictionsInteractor interactor = new InitialisePredictionsInteractor(
                    bootstrapGateway,
                    gameWeekGateway,
                    modelCoeffGateway,
                    presenter,
                    playerDataAccess
            );

            // Determine which gameweek we're predicting for
            String bootstrapJson = bootstrapGateway.getBootstrapData();
            JSONObject bootstrapData = new JSONObject(bootstrapJson);
            int predictingForGameweek = getCurrentGameweek(bootstrapData);

            System.out.println("=".repeat(80));
            System.out.println("INITIALIZING FPL PREDICTIONS");
            System.out.println("=".repeat(80));
            System.out.println();

            // Execute the prediction initialization
            InitialisePredictionsInputData inputData = new InitialisePredictionsInputData();
            interactor.execute(inputData);

            // Check if initialization was successful
            if (!presenter.isSuccess()) {
                System.err.println("Failed to initialize predictions: " + presenter.getErrorMessage());
                return;
            }

            System.out.println("✓ Successfully processed " + presenter.getTotalPlayers() + " players");
            System.out.println("✓ Processed " + presenter.getGameweeksProcessed() + " finished gameweeks");
            System.out.println();
            System.out.println("PREDICTING FOR GAMEWEEK: " + predictingForGameweek);
            System.out.println("=".repeat(80));
            System.out.println();

            // Print top 10 players for each position
            String[] positions = {"Goalkeeper", "Defender", "Midfielder", "Forward"};

            for (int position = 1; position <= 4; position++) {
                System.out.println("=".repeat(80));
                System.out.println("TOP 10 " + positions[position - 1].toUpperCase() + "S");
                System.out.println("=".repeat(80));
                System.out.printf("%-3s %-25s %-15s %-10s %-15s%n",
                        "Rank", "Player", "Team", "Cost", "Predicted Pts");
                System.out.println("-".repeat(80));

                List<Player> topPlayers = playerDataAccess.getTopPlayersByPosition(position, 10);

                int rank = 1;
                for (Player player : topPlayers) {
                    System.out.printf("%-3d %-25s %-15s £%.1f %-15.2f%n",
                            rank++,
                            player.getWebName(),
                            player.getTeamName(),
                            player.getNowCost(),
                            player.getPredictedPoints());
                }
                System.out.println();
            }

            System.out.println("=".repeat(80));
            System.out.println("PREDICTION SUMMARY COMPLETE");
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            System.err.println("Error during prediction test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Determine the current gameweek (the one being predicted for).
     * This is the first unfinished gameweek.
     */
    private static int getCurrentGameweek(JSONObject bootstrapData) {
        JSONArray events = bootstrapData.getJSONArray("events");

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            if (!event.getBoolean("finished")) {
                return event.getInt("id");
            }
        }

        // If all gameweeks are finished, return the last one + 1
        return events.length() + 1;
    }

    /**
     * Simple presenter implementation for testing.
     */
    static class TestPresenter implements InitialisePredictionsOutputBoundary {
        private boolean success = false;
        private String errorMessage = "";
        private int totalPlayers = 0;
        private int gameweeksProcessed = 0;

        @Override
        public void presentSuccess(InitialisePredictionsOutputData outputData) {
            this.success = true;
            this.totalPlayers = outputData.getTotalPlayersProcessed();
            this.gameweeksProcessed = outputData.getGameweeksProcessed();
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getTotalPlayers() {
            return totalPlayers;
        }

        public int getGameweeksProcessed() {
            return gameweeksProcessed;
        }
    }
}