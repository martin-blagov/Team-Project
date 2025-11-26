package interface_adapter.risk_assessment;

import use_case.risk_assessment.RiskAssessmentInputBoundary;
import use_case.risk_assessment.RiskAssessmentInputData;
import interface_adapter.team_view.TeamViewModel;
import entity.Team;


public class RiskAssessmentController {

    private final RiskAssessmentInputBoundary interactor;
    private final TeamViewModel teamViewModel;

    public RiskAssessmentController(RiskAssessmentInputBoundary interactor,
                                    TeamViewModel teamViewModel) {
        this.interactor = interactor;
        this.teamViewModel = teamViewModel;
    }

    public void handle() {
        Team team = teamViewModel.getTeam(); // may be null
        RiskAssessmentInputData inputData = new RiskAssessmentInputData(team);

        interactor.execute(inputData);
    }
}
