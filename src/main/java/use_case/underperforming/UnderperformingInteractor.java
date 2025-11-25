package use_case.underperforming;

import entity.Team;
import entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnderperformingInteractor implements UnderperformingInputBoundary {

    private final UnderperformingOutputBoundary presenter;
    private final Map<Integer, Map<String, Double>> coefficientMap;

    public UnderperformingInteractor(UnderperformingOutputBoundary presenter,
                                     Map<Integer, Map<String, Double>> coefficientMap) {
        this.presenter = presenter;
        this.coefficientMap = coefficientMap;
    }

    @Override
    public UnderperformingOutputData execute(UnderperformingInputData inputData) {

        Team team = inputData.getCurrentTeam();

        // CASE 1: No team selected → return empty list
        if (team == null || team.getPlayers() == null || team.getPlayers().isEmpty()) {
            List<String> underperformingPlayers = new ArrayList<>();
            UnderperformingOutputData outputData = new UnderperformingOutputData(underperformingPlayers);
            presenter.presentUnderperformingPlayers(outputData);
            return outputData;
        }

        List<String> underperformingPlayers = new ArrayList<>();

        // Iterate through each player in the team
        for (Player p : team.getPlayers()) {

            // 1. Predict upcoming points using the player's position
            int pos = p.getElementType();  // 1=GK, 2=DEF, 3=MID, 4=FWD
            Map<String, Double> coeffs = coefficientMap.get(pos);

            if (coeffs == null) {
                continue; // no coefficients for this position, skip
            }

            p.calculatePredictedPoints(coeffs);
            double predicted = p.getPredictedPoints();

            // 2. Get actual recent performance (average points last 3 GWs)
            double actual = p.getLast3Stat("total_points");

            // 3. Underperformance rule
            if (actual < predicted * 0.7) {
                underperformingPlayers.add(p.getWebName());
            }
        }

        // Wrap output + call presenter
        UnderperformingOutputData outputData = new UnderperformingOutputData(underperformingPlayers);
        presenter.presentUnderperformingPlayers(outputData);
        return outputData;
    }
}

