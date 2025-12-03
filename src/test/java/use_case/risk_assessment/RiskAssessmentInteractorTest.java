package use_case.risk_assessment;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import use_case.PlayerDataAccessInterface;
import use_case.risk_assessment.risk.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RiskAssessmentInteractorTest {

    private RiskAssessmentTeamAccessInterface teamAccess;
    private PlayerDataAccessInterface playerDataAccess;
    private RiskAssessmentOutputBoundary presenter;

    private RiskAssessmentInteractor interactor;

    private Player teamPlayer;
    private Player fullPlayer;

    @BeforeEach
    void setUp() {

        teamAccess = mock(RiskAssessmentTeamAccessInterface.class);
        playerDataAccess = mock(PlayerDataAccessInterface.class);
        presenter = mock(RiskAssessmentOutputBoundary.class);

        interactor = new RiskAssessmentInteractor(teamAccess, playerDataAccess, presenter);

        // Minimal player inside team
        teamPlayer = new Player(
                1, "ShortInfo", 3, "a", 5.5, 3, "TeamX",
                Map.of(), Map.of(), Map.of(), Map.of()
        );

        // Full player data with stats needed for risk rules
        fullPlayer = new Player(
                1, "FullInfo", 3, "a", 6.5, 3, "TeamX",
                Map.of("total_points", 80.0),
                Map.of("season_avg_goals_scored", 0.5, "season_avg_minutes", 90.0),
                Map.of("goals_scored_last3", 0.0, "minutes_last3", 30.0),
                Map.of("goals_scored_last5", 0.1, "minutes_last5", 40.0)
        );
    }

    // ------------------------------------------------------
    // TEST 1: Normal successful execution
    // ------------------------------------------------------
    @Test
    void testExecuteSuccess() {

        Team team = new Team(List.of(teamPlayer), 100, false);
        when(teamAccess.getTeam()).thenReturn(team);

        when(playerDataAccess.getPlayerById(1)).thenReturn(fullPlayer);

        interactor.execute(new RiskAssessmentInputData());

        ArgumentCaptor<RiskAssessmentOutputData> captor =
                ArgumentCaptor.forClass(RiskAssessmentOutputData.class);

        verify(presenter).presentRiskResults(captor.capture());

        RiskAssessmentOutputData output = captor.getValue();

        assertNotNull(output);
        assertEquals(1, output.getPlayerRisks().size());

        PlayerRisk risk = output.getPlayerRisks().get(0);

        assertEquals(1, risk.getPlayer().getId());   // Correct getter
        assertTrue(risk.getRiskCount() > 0);         // At least one rule must trigger
    }

    // ------------------------------------------------------
    // TEST 2: Null team
    // ------------------------------------------------------
    @Test
    void testExecuteNoTeam() {

        when(teamAccess.getTeam()).thenReturn(null);

        interactor.execute(new RiskAssessmentInputData());

        verify(presenter).presentFailView("No team data available.");
        verify(presenter, never()).presentRiskResults(any());
    }

    // ------------------------------------------------------
    // TEST 3: Empty team list
    // ------------------------------------------------------
    @Test
    void testExecuteEmptyTeam() {

        when(teamAccess.getTeam()).thenReturn(new Team(List.of(), 100, false));

        interactor.execute(new RiskAssessmentInputData());

        verify(presenter).presentFailView("No team data available.");
        verify(presenter, never()).presentRiskResults(any());
    }

    // ------------------------------------------------------
    // TEST 4: Team contains a null player → skip safely
    // ------------------------------------------------------
    @Test
    void testExecuteNullPlayerInTeam() {

        List<Player> players = new ArrayList<>();
        players.add(null); // allowed in ArrayList

        Team team = new Team(players, 100f, false);
        when(teamAccess.getTeam()).thenReturn(team);

        interactor.execute(new RiskAssessmentInputData());

        ArgumentCaptor<RiskAssessmentOutputData> captor =
                ArgumentCaptor.forClass(RiskAssessmentOutputData.class);

        verify(presenter).presentRiskResults(captor.capture());

        assertTrue(captor.getValue().getPlayerRisks().isEmpty());
    }


    // ------------------------------------------------------
    // TEST 5: playerDataAccess returns null → skip safely
    // ------------------------------------------------------
    @Test
    void testExecuteMissingFullPlayerData() {

        when(teamAccess.getTeam()).thenReturn(new Team(List.of(teamPlayer), 100, false));
        when(playerDataAccess.getPlayerById(1)).thenReturn(null);

        interactor.execute(new RiskAssessmentInputData());

        ArgumentCaptor<RiskAssessmentOutputData> captor =
                ArgumentCaptor.forClass(RiskAssessmentOutputData.class);

        verify(presenter).presentRiskResults(captor.capture());

        assertTrue(captor.getValue().getPlayerRisks().isEmpty());
    }
}
