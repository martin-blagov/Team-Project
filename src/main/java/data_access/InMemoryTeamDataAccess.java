package data_access;

import entity.Team;
import use_case.TeamDataAccessInterface;

public class InMemoryTeamDataAccess implements TeamDataAccessInterface {

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
