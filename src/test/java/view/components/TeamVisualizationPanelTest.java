package view.components;

import entity.Player;
import entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamVisualizationPanelTest {

    private TeamVisualizationPanel panel;

    @BeforeEach
    void setUp() {
        panel = new TeamVisualizationPanel();
        panel.setSize(500, 700);
    }

    /**
     * on construction, there should be no team and the pitch dimensions should be initialized to some positive values
     */
    @Test
    void testInitialState() {
        assertNull(panel.getTeam(), "Initially, no team should be set.");

        int pitchWidth = panel.getPitchWidth();
        int pitchHeight = panel.getPitchHeight();

        assertTrue(pitchWidth > 0, "Pitch width should be initialized to a positive value.");
        assertTrue(pitchHeight > 0, "Pitch height should be initialized to a positive value.");
    }

    /**
     * setTeam() stores the given team and getTeam() returns the same reference.
     * refresh() with a non-null team creates exactly 15 components on the panel.
     */
    @Test
    void testSetTeamAndRefreshCreatesButtons() {
        Team team = createTeamWithPlayers(15); // 15 non-null players
        panel.setTeam(team);

        assertSame(team, panel.getTeam(), "getTeam() should return the same team instance that was set.");

        // before refresh, panel should have no player components.
        assertEquals(0, panel.getComponentCount(), "Before refresh, panel should have no components.");

        panel.refresh();

        // after refresh, we expect 15 PlayerKitButton components
        assertEquals(15, panel.getComponentCount(),
                "Panel should contain 15 PlayerKitButtons after refresh (one per slot).");

        // all components should be PlayerKitButton instances
        for (int i = 0; i < panel.getComponentCount(); i++) {
            assertTrue(panel.getComponent(i) instanceof PlayerKitButton,
                    "All components should be instances of PlayerKitButton.");
        }
    }

    /**
     * If no team is set (currentTeam == null), calling refresh() should leave the panel with no components
     */
    @Test
    void testRefreshWithNoTeamDoesNotAddComponents() {
        assertNull(panel.getTeam(), "Team should be null before anything is set.");

        // ensuring panel is empty before refresh
        assertEquals(0, panel.getComponentCount(), "Panel should start with no components.");

        panel.refresh();

        assertEquals(0, panel.getComponentCount(),
                "Panel should still have no components after refresh when no team is set.");
    }

    /**
     * Clicking on a non-placeholder player button should invoke the playerClickListener with correct Player instance.
     */
    @Test
    void testPlayerClickListenerIsInvoked() {
        // create a team with exactly 1 real player and 14 null slots
        Player starPlayer = createPlayer(1, 1, "StarGK", "StarTeam");
        List<Player> players = new ArrayList<>();
        players.add(starPlayer);
        while (players.size() < 15) {
            players.add(null);
        }
        Team team = new Team(players, 100.0f, false);

        panel.setTeam(team);

        // capture listener invocations
        class Capture {
            int playerClicks = 0;
            Player lastPlayer = null;
        }
        Capture capture = new Capture();

        panel.setPlayerClickListener(p -> {
            capture.playerClicks++;
            capture.lastPlayer = p;
        });

        panel.refresh();

        // simulate clicking all buttons, only one of them should represent the real player
        int buttonCount = panel.getComponentCount();
        assertEquals(15, buttonCount, "There should be 15 buttons (player + placeholders).");

        for (int i = 0; i < buttonCount; i++) {
            assertTrue(panel.getComponent(i) instanceof PlayerKitButton,
                    "All components should be PlayerKitButton.");
            PlayerKitButton button = (PlayerKitButton) panel.getComponent(i);
            // simulate a click
            button.doClick();
        }

        assertEquals(1, capture.playerClicks,
                "Exactly one playerClick should have been recorded (for the single non-null player).");
        assertNotNull(capture.lastPlayer, "Last clicked player should not be null.");
        assertEquals(starPlayer.getId(), capture.lastPlayer.getId(),
                "Listener should have received the correct Player instance.");
    }

    /**
     * Clicking on placeholder buttons should invoke the placeholderClickListener with appropriate position labels
     **/
    @Test
    void testPlaceholderClickListenerIsInvoked() {
        // one real GK player, all other slots null, so 14 placeholders.
        Player starPlayer = createPlayer(1, 1, "StarGK", "StarTeam");
        List<Player> players = new ArrayList<>();
        players.add(starPlayer);
        while (players.size() < 15) {
            players.add(null);
        }
        Team team = new Team(players, 100.0f, false);

        panel.setTeam(team);

        class Capture {
            int placeholderClicks = 0;
            final List<String> positions = new ArrayList<>();
        }
        Capture capture = new Capture();

        panel.setPlaceholderClickListener(pos -> {
            capture.placeholderClicks++;
            capture.positions.add(pos);
        });

        panel.refresh();

        int buttonCount = panel.getComponentCount();
        assertEquals(15, buttonCount, "There should be 15 buttons (1 real + 14 placeholders).");

        // set a dummy playerClickListener so clicks on the real player don't cause NullPointException.
        panel.setPlayerClickListener(p -> { /* ignore */ });

        for (int i = 0; i < buttonCount; i++) {
            assertTrue(panel.getComponent(i) instanceof PlayerKitButton);
            PlayerKitButton btn = (PlayerKitButton) panel.getComponent(i);
            btn.doClick();
        }

        assertEquals(14, capture.placeholderClicks,
                "There should be placeholder clicks for the 14 empty slots.");

        // all reported positions should be valid labels
        assertFalse(capture.positions.isEmpty(), "At least one placeholder position should be recorded.");
        for (String pos : capture.positions) {
            assertTrue(pos.equals("GK") || pos.equals("DEF") || pos.equals("MID") || pos.equals("FWD"),
                    "Placeholder position should be one of GK/DEF/MID/FWD.");
        }
    }

    /**
     * setDimensions() should update the panel's preferred and minimum size
     */
    @Test
    void testSetDimensions() {
        panel.setDimensions(600, 900);

        assertEquals(600, panel.getPreferredSize().width);
        assertEquals(900, panel.getPreferredSize().height);

        assertEquals(600, panel.getMinimumSize().width);
        assertEquals(900, panel.getMinimumSize().height);
    }

    /**
     * Create a Team with the given number of non-null players.
     * Remaining slots up to 15 are filled with nulls.
     */
    private Team createTeamWithPlayers(int nonNullCount) {
        List<Player> players = new ArrayList<>();
        int idCounter = 1;

        // cycle through positions 1-4 for variety
        for (int i = 0; i < nonNullCount; i++) {
            int elementType = (i % 4) + 1; // 1=GK, 2=DEF, 3=MID, 4=FWD
            Player p = createPlayer(idCounter++, elementType,
                    "Player_" + i, "Team_" + (i % 3));
            players.add(p);
        }

        while (players.size() < 15) {
            players.add(null);
        }

        return new Team(players, 100.0f, false);
    }

    /**
     * Helper to build a dummy Player.
     */
    private Player createPlayer(int id, int elementType, String webName, String teamName) {
        Player p = new Player(
                id,
                webName,
                elementType,
                "a",          // position name or code
                5.0,                // cost
                elementType,
                teamName,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        );
        p.calculatePredictedPoints(java.util.Map.of("intercept", 10.0));
        return p;
    }
}
