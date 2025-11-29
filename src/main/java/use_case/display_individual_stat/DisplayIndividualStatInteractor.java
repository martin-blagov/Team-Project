package use_case.display_individual_stat;

import entity.Player;
import use_case.PlayerDataAccessInterface;
import use_case.TeamDataAccessInterface;

public class DisplayIndividualStatInteractor implements DisplayIndividualStatInputBoundary {
    private final DisplayIndividualStatOutputBoundary presenter;
    private final PlayerDataAccessInterface playerDataAccess;
    // private final TeamDataAccessInterface teamDataAccess;

    public DisplayIndividualStatInteractor(DisplayIndividualStatOutputBoundary presenter, PlayerDataAccessInterface playerDataAccess,
                                           TeamDataAccessInterface teamDataAccess) {
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
        switch (filter) {
            case "Average":
                goals = String.valueOf(p.getSeasonAvgStat(GOALS_STAT_NAME));
                assists = String.valueOf(p.getSeasonAvgStat(ASSISTANT_STAT_NAME));
                points = String.valueOf(p.getSeasonAvgStat(POINTS_STAT_NAME));
                break;
            case "Last 3":
                goals = String.valueOf(p.getLast3Stat(GOALS_STAT_NAME));
                assists = String.valueOf(p.getLast3Stat(ASSISTANT_STAT_NAME));
                points = String.valueOf(p.getLast3Stat(POINTS_STAT_NAME));
                break;
            case "Last 5":
                goals = String.valueOf(p.getLast5Stat(GOALS_STAT_NAME));
                assists = String.valueOf(p.getLast5Stat(ASSISTANT_STAT_NAME));
                points = String.valueOf(p.getLast5Stat(POINTS_STAT_NAME));
                break;
            default:
                goals = String.valueOf(p.getSeasonTotalStat(GOALS_STAT_NAME));
                assists = String.valueOf(p.getSeasonTotalStat(ASSISTANT_STAT_NAME));
                points = String.valueOf(p.getSeasonTotalStat(POINTS_STAT_NAME));
                break;
        }


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
