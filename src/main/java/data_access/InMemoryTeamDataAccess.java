package data_access;

import entity.Team;
import use_case.display_team.DisplayTeamDataAccessInterface;
import use_case.team_entry.TeamDataAccessInterface;
import use_case.transfer_suggestions.TransferSuggestionsTeamDataAccessInterface;


public class InMemoryTeamDataAccess implements TeamDataAccessInterface, DisplayTeamDataAccessInterface,
TransferSuggestionsTeamDataAccessInterface{

    private Team storedTeam = null;

    @Override
    public void saveTeam(Team team) {
        this.storedTeam = team;
    }

    @Override
    public Team getTeam() {
        return storedTeam;
    }

    @Override
    public void clearTeam() {
        this.storedTeam = null;
    }
}
