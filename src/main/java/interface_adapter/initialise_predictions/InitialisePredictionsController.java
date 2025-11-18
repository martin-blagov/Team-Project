package interface_adapter.initialise_predictions;

import use_case.initialise_predictions.InitialisePredictionsInputBoundary;
import use_case.initialise_predictions.InitialisePredictionsInputData;

/**
 * Controller for Initialize Predictions use case.
 * Handles the triggering of prediction initialization.
 */
public class InitialisePredictionsController {
    private final InitialisePredictionsInputBoundary interactor;

    public InitialisePredictionsController(InitialisePredictionsInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Execute the initialization process.
     * Called at app startup by the view.
     */
    public void execute() {
        InitialisePredictionsInputData inputData = new InitialisePredictionsInputData();
        interactor.execute(inputData);
    }
}