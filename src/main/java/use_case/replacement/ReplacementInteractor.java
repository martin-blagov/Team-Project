package use_case.replacement;

import java.util.Collections;

public class ReplacementInteractor implements ReplacementInputBoundary {

    private final ReplacementOutputBoundary presenter;

    public ReplacementInteractor(ReplacementOutputBoundary presenter) {
        this.presenter = presenter;
    }

    @Override
    public void execute(ReplacementInputData inputData) {
        ReplacementOutputData outputData = new ReplacementOutputData(Collections.emptyList());
        presenter.prepareSuccessView(outputData);
    }
}
