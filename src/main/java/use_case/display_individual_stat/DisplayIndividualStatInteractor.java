package use_case.display_individual_stat;

import entity.Player;
import use_case.PlayerDataAccessInterface;
// import use_case.TeamDataAccessInterface;

public class DisplayIndividualStatInteractor implements DisplayIndividualStatInputBoundary {
    private final DisplayIndividualStatOutputBoundary presenter;
    private final PlayerDataAccessInterface playerDataAccess;
    // private final TeamDataAccessInterface teamDataAccess;

    public DisplayIndividualStatInteractor(DisplayIndividualStatOutputBoundary presenter, PlayerDataAccessInterface playerDataAccess) {
        this.presenter = presenter;
        this.playerDataAccess = playerDataAccess;
        // this.teamDataAccess = teamDataAccess;
    }

    public void execute(DisplayIndividualStatInputData inputData) {
        int id = inputData.getPlayerID();
        Player p = playerDataAccess.getPlayerById(id);
        final String name = p.getWebName();
        final String position = p.getPosition();
        final String teamName = p.getTeamName();
        final String cost = String.valueOf(p.getNowCost());

        final String filter = inputData.getFilterOption();

        final String GOALS_STAT_NAME = "goals_scored";
        final String ASSISTANT_STAT_NAME = "assists";
        final String POINTS_STAT_NAME = "total_points";

        String goals;
        String assists;
        String points;

        // Set output data
        double goalsDouble;
        double assistsDouble;
        double pointsDouble;

        switch (filter) {
            case "Average":
                goalsDouble = p.getSeasonAvgStat(GOALS_STAT_NAME);
                assistsDouble = p.getSeasonAvgStat(ASSISTANT_STAT_NAME);
                pointsDouble = p.getSeasonAvgStat(POINTS_STAT_NAME);
                break;
            case "Last 3":
                goalsDouble = p.getLast3Stat(GOALS_STAT_NAME);
                assistsDouble = p.getLast3Stat(ASSISTANT_STAT_NAME);
                pointsDouble = p.getLast3Stat(POINTS_STAT_NAME);
                break;
            case "Last 5":
                goalsDouble = p.getLast5Stat(GOALS_STAT_NAME);
                assistsDouble = p.getLast5Stat(ASSISTANT_STAT_NAME);
                pointsDouble = p.getLast5Stat(POINTS_STAT_NAME);
                break;
            default:
                goalsDouble = p.getSeasonTotalStat(GOALS_STAT_NAME);
                assistsDouble = p.getSeasonTotalStat(ASSISTANT_STAT_NAME);
                pointsDouble = p.getSeasonTotalStat(POINTS_STAT_NAME);
                break;
        }

        goals = String.format("%.2f", goalsDouble);
        assists = String.format("%.2f", assistsDouble);
        points = String.format("%.2f", pointsDouble);


        DisplayIndividualStatOutputData outputData = new DisplayIndividualStatOutputData(
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
