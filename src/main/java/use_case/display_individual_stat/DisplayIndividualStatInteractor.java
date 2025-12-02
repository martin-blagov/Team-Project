package use_case.display_individual_stat;

import entity.Player;
import use_case.PlayerDataAccessInterface;

/**
 * The Interactor for Display Individual Stats Use Case.
 */
public class DisplayIndividualStatInteractor implements DisplayIndividualStatInputBoundary {
    private final DisplayIndividualStatOutputBoundary presenter;
    private final PlayerDataAccessInterface playerDataAccess;

    public DisplayIndividualStatInteractor(DisplayIndividualStatOutputBoundary presenter,
                                           PlayerDataAccessInterface playerDataAccess) {
        this.presenter = presenter;
        this.playerDataAccess = playerDataAccess;
    }

    public void execute(DisplayIndividualStatInputData inputData) {
        final int id = inputData.getPlayerID();
        final Player p = playerDataAccess.getPlayerById(id);
        final String name = p.getWebName();
        final String position = p.getPosition();
        final String teamName = p.getTeamName();
        final String cost = String.valueOf(p.getNowCost());

        final String filter = inputData.getFilterOption();

        final String goalsString = "goals_scored";
        final String assistsString = "assists";
        final String pointsString = "total_points";

        String goals;
        String assists;
        String points;

        // Set output data
        double goalsDouble;
        double assistsDouble;
        double pointsDouble;

        switch (filter) {
            case "Average":
                goalsDouble = p.getSeasonAvgStat(goalsString);
                assistsDouble = p.getSeasonAvgStat(assistsString);
                pointsDouble = p.getSeasonAvgStat(pointsString);

                goals = String.format("%.2f", goalsDouble);
                assists = String.format("%.2f", assistsDouble);
                points = String.format("%.2f", pointsDouble);

                break;
            case "Last 3":
                goalsDouble = p.getLast3Stat(goalsString);
                assistsDouble = p.getLast3Stat(assistsString);
                pointsDouble = p.getLast3Stat(pointsString);

                goals = String.format("%.2f", goalsDouble);
                assists = String.format("%.2f", assistsDouble);
                points = String.format("%.2f", pointsDouble);

                break;
            case "Last 5":
                goalsDouble = p.getLast5Stat(goalsString);
                assistsDouble = p.getLast5Stat(assistsString);
                pointsDouble = p.getLast5Stat(pointsString);

                goals = String.format("%.2f", goalsDouble);
                assists = String.format("%.2f", assistsDouble);
                points = String.format("%.2f", pointsDouble);

                break;
            default:
                goalsDouble = p.getSeasonTotalStat(goalsString);
                assistsDouble = p.getSeasonTotalStat(assistsString);
                pointsDouble = p.getSeasonTotalStat(pointsString);

                goals = String.format("%.0f", goalsDouble);
                assists = String.format("%.0f", assistsDouble);
                points = String.format("%.2f", pointsDouble);
                break;
        }

        if ("goalkeeper".equals(position)) {
            goals = "N/A";
            assists = "N/A";
        }

        final DisplayIndividualStatOutputData outputData = new DisplayIndividualStatOutputData(
                name,
                position,
                teamName,
                cost,
                goals,
                assists,
                points);
        presenter.presentView(outputData);
    }
}
