package interface_adapter.replacement;

import use_case.replacement.ReplacementInputBoundary;
import use_case.replacement.ReplacementInputData;
import entity.Team;

public class ReplacementController {

    private final ReplacementInputBoundary interactor;

    public ReplacementController(ReplacementInputBoundary interactor) {
        this.interactor = interactor;
    }

    // For future: team comes from the view
    public void recommendReplacement(Team team) {
        ReplacementInputData inputData = new ReplacementInputData(team);
        interactor.execute(inputData);
    }
}
