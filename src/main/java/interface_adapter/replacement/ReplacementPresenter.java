package interface_adapter.replacement;

import use_case.replacement.ReplacementOutputBoundary;
import use_case.replacement.ReplacementOutputData;

public class ReplacementPresenter implements ReplacementOutputBoundary {

    @Override
    public void prepareSuccessView(ReplacementOutputData outputData) {
        System.out.println("[Demo] Suggested Players:" + outputData.getSuggestedPlayers());
    }
}
