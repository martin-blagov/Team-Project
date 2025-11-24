package interface_adapter.underperforming;

import use_case.underperforming.UnderperformingOutputBoundary;
import use_case.underperforming.UnderperformingOutputData;

public class UnderperformingPresenter implements UnderperformingOutputBoundary {

    @Override
    public void presentUnderperformingPlayers(UnderperformingOutputData outputData) {
        // Minimal placeholder: print to console
        System.out.println("Underperforming players: " + outputData.getUnderperformingPlayers());
    }
}
