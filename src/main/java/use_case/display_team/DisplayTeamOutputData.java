package use_case.display_team;

import entity.Team;

/**
 * Output Data for Display Team use case.
 * Contains the team to display (may have null players for empty slots).
 */
public class DisplayTeamOutputData {
    private final Team team;

    public DisplayTeamOutputData(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
}