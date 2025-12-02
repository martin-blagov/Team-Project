package use_case.risk_assessment;

import entity.Team;
import entity.Player;
import use_case.risk_assessment.risk.*;
import use_case.PlayerDataAccessInterface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RiskAssessmentInteractor implements RiskAssessmentInputBoundary {

    private final RiskAssessmentTeamAccessInterface teamDataAccess;
    private final PlayerDataAccessInterface playerDataAccess;
    private final RiskAssessmentOutputBoundary presenter;

    public RiskAssessmentInteractor(RiskAssessmentTeamAccessInterface teamDataAccess, PlayerDataAccessInterface playerDataAccess,
                                    RiskAssessmentOutputBoundary presenter) {
        this.teamDataAccess = teamDataAccess;
        this.playerDataAccess = playerDataAccess;
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
            if(player == null) continue;
            // cross-referencing with actual player data
            Player fullPlayer = playerDataAccess.getPlayerById(player.getId());

            if (fullPlayer == null) continue;

            PlayerRisk playerRisk = new PlayerRisk(fullPlayer, rules);
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
