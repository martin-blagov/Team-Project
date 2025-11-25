package interface_adapter.underperforming;

import use_case.underperforming.UnderperformingInputBoundary;
import use_case.underperforming.UnderperformingInputData;
import interface_adapter.team_view.TeamViewModel;
import entity.Team;


public class UnderperformingController {

    private final UnderperformingInputBoundary interactor;
    private final TeamViewModel teamViewModel;

    public UnderperformingController(UnderperformingInputBoundary interactor,
                                     TeamViewModel teamViewModel) {
        this.interactor = interactor;
        this.teamViewModel = teamViewModel;
    }

    public void handle() {
        Team team = teamViewModel.getTeam(); // may be null
        UnderperformingInputData inputData = new UnderperformingInputData(team);

        interactor.execute(inputData);
    }
}
