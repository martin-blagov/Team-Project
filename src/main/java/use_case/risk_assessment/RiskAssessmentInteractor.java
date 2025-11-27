package use_case.risk_assessment;

import entity.Team;
import entity.Player;
import use_case.TeamDataAccessInterface;
import use_case.risk_assessment.risk.*;

import java.util.ArrayList;
import java.util.List;

public class RiskAssessmentInteractor implements RiskAssessmentInputBoundary {

    private final TeamDataAccessInterface teamDataAccess;
    private final RiskAssessmentOutputBoundary presenter;

    public RiskAssessmentInteractor(TeamDataAccessInterface teamDataAccess,
                                    RiskAssessmentOutputBoundary presenter) {
        this.teamDataAccess = teamDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(RiskAssessmentInputData data) {

        Team team = teamDataAccess.getTeam();

        if (team == null || team.getPlayers() == null || team.getPlayers().isEmpty()) {
            presenter.presentFailView("No team data available.");
            return;
        }

        List<RiskRule> rules = List.of(
                new MinutesDeclineRule(),
                new FormDropRule(),
                new ValueRiskRule(),
                new PredictedPointsDeclineRule()
        );

        List<PlayerRisk> profiles = new ArrayList<>();

        for (Player player : team.getPlayers()) {
            profiles.add(new PlayerRisk(player, rules));
        }

        profiles.sort((a, b) -> b.getRiskCount() - a.getRiskCount());

        presenter.presentRiskResults(new RiskAssessmentOutputData(profiles));
    }
}
