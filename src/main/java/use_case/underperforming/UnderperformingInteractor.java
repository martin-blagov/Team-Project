package use_case.underperforming;

import java.util.ArrayList;
import java.util.List;

public class UnderperformingInteractor implements UnderperformingInputBoundary {

    private final UnderperformingOutputBoundary presenter;

    public UnderperformingInteractor(UnderperformingOutputBoundary presenter) {
        this.presenter = presenter;
    }

    @Override
    public UnderperformingOutputData execute(UnderperformingInputData inputData) {
        // Placeholder logic: collect underperforming players
        List<String> underperformingPlayers = new ArrayList<>();

        // inputData.getCurrentTeam()
        // underperformingPlayers.add("Player X");

        UnderperformingOutputData outputData = new UnderperformingOutputData(underperformingPlayers);
        presenter.presentUnderperformingPlayers(outputData);
        return outputData;
    }
}
