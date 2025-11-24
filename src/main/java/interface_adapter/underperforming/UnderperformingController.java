package interface_adapter.underperforming;

import use_case.underperforming.UnderperformingInputBoundary;
import use_case.underperforming.UnderperformingInputData;
import use_case.underperforming.UnderperformingOutputData;

public class UnderperformingController {

    private final UnderperformingInputBoundary interactor;

    public UnderperformingController(UnderperformingInputBoundary interactor) {
        this.interactor = interactor;
    }

    public UnderperformingOutputData handle(UnderperformingInputData inputData) {
        return interactor.execute(inputData);
    }
}
