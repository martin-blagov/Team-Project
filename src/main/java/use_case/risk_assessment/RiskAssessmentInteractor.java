package use_case.risk_assessment;

import entity.Team;
import entity.Player;
import use_case.risk_assessment.risk.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RiskAssessmentInteractor implements RiskAssessmentInputBoundary {

    private final RiskAssessmentTeamAccessInterface teamDataAccess;
    private final RiskAssessmentOutputBoundary presenter;

    public RiskAssessmentInteractor(RiskAssessmentTeamAccessInterface teamDataAccess,
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
            PlayerRisk playerRisk = new PlayerRisk(player, rules);
            if (playerRisk.getRiskCount() > 0) {
                profiles.add(playerRisk);
            }
        }

        //sort player ricks in descending order
        profiles.sort(new Comparator<PlayerRisk>() {
            @Override
            public int compare(PlayerRisk pr1, PlayerRisk pr2) {
                return pr1.getRiskCount() - pr2.getRiskCount();
            }
        });

        presenter.presentRiskResults(new RiskAssessmentOutputData(profiles));
    }
}
