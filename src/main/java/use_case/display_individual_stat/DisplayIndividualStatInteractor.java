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
        final double cost = p.getNowCost();
        // final String goals = p.getSeasonTotalStat("goals_scored");
        // final String assists = p.getSeasonAvgStat("goals_scored");
        // final String p.getLast3Stat("goals_scored");
        // final String p.getLast5Stat("goals_scored");

        DisplayIndividualStatOutputData outputData = new DisplayIndividualStatOutputData(
                name,
                position,
                teamName,
                cost);
        presenter.presentView(outputData);
    }
}
