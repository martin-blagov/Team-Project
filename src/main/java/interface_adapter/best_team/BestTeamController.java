package interface_adapter.best_team;

import use_case.best_team.BestTeamInputBoundary;
import use_case.best_team.BestTeamRequestModel;
import use_case.best_team.BestTeamResponseModel;

public class BestTeamController {
    private final BestTeamInputBoundary interactor;

    public BestTeamController(BestTeamInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void showBestTeam() {
        BestTeamRequestModel requestModel = new BestTeamRequestModel(100.0);
        interactor.execute(requestModel);
    }
}
