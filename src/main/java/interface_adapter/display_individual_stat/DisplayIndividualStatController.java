package interface_adapter.display_individual_stat;

import use_case.display_individual_stat.DisplayIndividualStatInputBoundary;
import use_case.display_individual_stat.DisplayIndividualStatInputData;

/**
 * Controller for the Individual Stats Use Case.
 */
public class DisplayIndividualStatController {

    private final DisplayIndividualStatInputBoundary displayIndividualStatInteractor;

    public DisplayIndividualStatController(DisplayIndividualStatInputBoundary displayIndividualStatInteractor) {
        this.displayIndividualStatInteractor = displayIndividualStatInteractor;
    }

    /**
     * Executes the Individual Stats Use Case.
     * @param displayIndividualStatInputData - player user selects
     */
    public void execute(DisplayIndividualStatInputData displayIndividualStatInputData) {
        displayIndividualStatInteractor.execute(displayIndividualStatInputData);
    }
}
