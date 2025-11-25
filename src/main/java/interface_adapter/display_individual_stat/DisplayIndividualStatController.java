package interface_adapter.display_individual_stat;

import use_case.display_individual_stat.DisplayIndividualStatInputBoundary;
import use_case.display_individual_stat.DisplayIndividualStatInputData;

public class DisplayIndividualStatController {

    private final DisplayIndividualStatInputBoundary displayIndividualStatInteractor;

    public DisplayIndividualStatController(DisplayIndividualStatInputBoundary displayIndividualStatInteractor) {
        this.displayIndividualStatInteractor = displayIndividualStatInteractor;
    }

    /**
     * Executes the Individual Stats Use Case.
     */
    public void execute(int playerID) {
        DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(playerID);
        displayIndividualStatInteractor.execute(inputData);
    }
}
