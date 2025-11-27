package use_case.risk_assessment;
import entity.Team;
import entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import use_case.risk_assessment.risk.MinutesDeclineRule;
import use_case.risk_assessment.risk.ValueRiskRule;
import use_case.risk_assessment.risk.FormDropRule;
import use_case.risk_assessment.risk.PredictedPointsDeclineRule;
import use_case.risk_assessment.risk.PlayerRisk;
import use_case.risk_assessment.risk.RiskRule;


import java.util.ArrayList;
import java.util.List;

/**
 * Interactor for the Risk Assessment use case.
 * Computes risk evaluations for each player using RiskRule strategies.
 */
public class RiskAssessmentInteractor implements RiskAssessmentInputBoundary {

    private final RiskAssessmentOutputBoundary presenter;

    public RiskAssessmentInteractor(RiskAssessmentOutputBoundary presenter) {
        this.presenter = presenter;
    }

    @Override
    public void execute(RiskAssessmentInputData inputData) {

        Team team = inputData.getCurrentTeam();

        // Handle case: no team
        if (team == null || team.getPlayers() == null || team.getPlayers().isEmpty()) {
            presenter.presentFailView("No team data available for risk assessment.");
        }
        else {
            // 1. Construct list of risk rules (Strategy pattern)
            List<RiskRule> rules = List.of(
                    new MinutesDeclineRule(),
                    new FormDropRule(),
                    new ValueRiskRule(),
                    new PredictedPointsDeclineRule()
            );

            // 2. Evaluate each player and create PlayerRisk profiles
            List<PlayerRisk> riskProfiles = new ArrayList<>();

            for (Player player : team.getPlayers()) {
                PlayerRisk profile = new PlayerRisk(player, rules);
                riskProfiles.add(profile);
            }

            // 3. Sort players by total risk count (highest → lowest)
            riskProfiles.sort((a, b) -> b.getRiskCount() - a.getRiskCount());

            // 4. Package into OutputData
            RiskAssessmentOutputData outputData = new RiskAssessmentOutputData(riskProfiles);

            // 5. Send to presenter
            presenter.presentRiskResults(outputData);
        }
    }

}
