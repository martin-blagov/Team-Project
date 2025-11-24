package view.components;

import entity.Player;
import entity.Team;
import use_case.PlayerDataAccessInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A visual component that displays a football team on a pitch.
 * Players are shown as kit images positioned according to formation.
 */
public class TeamVisualizationPanel extends JPanel {

    private BufferedImage pitchImage;
    private PlayerDataAccessInterface playerDataAccess;
    private Team currentTeam;

    // Click listeners for when players/placeholders are clicked
    private Consumer<Player> playerClickListener;
    private Consumer<String> placeholderClickListener;

    // Pitch dimensions (will be set based on image)
    private int pitchWidth;
    private int pitchHeight;

    // Display dimensions (scaled down)
    private static final int DISPLAY_WIDTH = 500;
    private static final int DISPLAY_HEIGHT = 700;

    // File paths as constants
    private static final String PITCH_IMAGE_PATH = "src/main/resources/images/pitch/Football_Pitch.jpg";

    // Base path for kit images - we'll build the full path dynamically
    private static final String BASE_KIT_PATH = "src/main/resources/images/kits/";

    /**
     * Create a new team visualization panel.
     * NOTE: Does NOT load players immediately - call setTeam() and refresh() when ready.
     */
    public TeamVisualizationPanel(PlayerDataAccessInterface playerDataAccess) {
        this.playerDataAccess = playerDataAccess;
        this.currentTeam = null;
        this.playerClickListener = null;
        this.placeholderClickListener = null;
        loadPitchImage();
        setupPanel();
    }

    /**
     * Load the football pitch background image.
     */
    private void loadPitchImage() {
        try {
            File pitchFile = new File(PITCH_IMAGE_PATH);

            if (!pitchFile.exists()) {
                throw new IOException("Pitch image file not found at: " + pitchFile.getAbsolutePath());
            }

            pitchImage = ImageIO.read(pitchFile);

            if (pitchImage == null) {
                throw new IOException("ImageIO.read returned null - file might not be a valid image");
            }

            pitchWidth = pitchImage.getWidth();
            pitchHeight = pitchImage.getHeight();

        } catch (IOException e) {
            System.err.println("Error loading pitch image: " + e.getMessage());

            // Set default dimensions if image fails to load
            pitchWidth = 800;
            pitchHeight = 1200;
        }
    }

    /**
     * Setup the panel properties.
     */
    private void setupPanel() {
        setLayout(null);
        setOpaque(true);

        // Add resize listener to recalculate positions
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (currentTeam != null) {
                    refresh();
                }
            }
        });
    }

    /**
     * Paint the component - draw the pitch background.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (pitchImage != null) {
            // Draw the pitch image scaled to panel size
            g.drawImage(pitchImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback: draw a green background if image failed to load
            g.setColor(new Color(34, 139, 34)); // Forest green
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw center line
            g.setColor(Color.WHITE);
            g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);

            // Draw error message
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            String errorMsg = "Image Failed to Load";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(errorMsg)) / 2;
            int y = getHeight() / 2 - 50;
            g.drawString(errorMsg, x, y);

            g.setFont(new Font("Arial", Font.PLAIN, 14));
            String helpMsg = "Check console for details";
            x = (getWidth() - g.getFontMetrics().stringWidth(helpMsg)) / 2;
            g.drawString(helpMsg, x, y + 30);
        }

        // Draw budget box (if team is set)
        if (currentTeam != null) {
            drawBudgetBox(g);
        }
    }

    /**
     * Draw a budget information box at the top right corner of the pitch.
     * Shows remaining budget and number of players selected.
     *
     * @param g Graphics context to draw on
     */
    private void drawBudgetBox(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Get budget info from team
        float budget = currentTeam.getBudget();
        int filledSlots = currentTeam.getFilledSlots();
        int totalSlots = 15;

        // Calculate box dimensions (scale with panel size)
        int boxWidth = (int)(getWidth() * 0.25);  // 25% of panel width
        int boxHeight = (int)(getHeight() * 0.12); // 12% of panel height
        int boxX = getWidth() - boxWidth - 10;     // 10px from right edge
        int boxY = 10;                              // 10px from top

        // Draw semi-transparent background
        g2d.setColor(new Color(0, 0, 0, 180)); // Black with 70% opacity
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15); // Rounded corners

        // Draw border
        g2d.setColor(new Color(255, 255, 255, 200)); // White with 80% opacity
        g2d.setStroke(new java.awt.BasicStroke(2));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

        // Draw text
        g2d.setColor(Color.WHITE);

        // Scale font size based on box height
        int fontSize = Math.max(12, boxHeight / 5);
        g2d.setFont(new Font("Ariel", Font.BOLD, fontSize));

        // Draw budget text
        String budgetText = String.format("Budget: £%.1fm", budget);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = boxX + (boxWidth - fm.stringWidth(budgetText)) / 2;
        int textY = boxY + boxHeight / 3 + fm.getAscent() / 2;
        g2d.drawString(budgetText, textX, textY);

        // Draw players count text
        String playersText = String.format("Players: %d/%d", filledSlots, totalSlots);
        int playersTextX = boxX + (boxWidth - fm.stringWidth(playersText)) / 2;
        int playersTextY = boxY + (2 * boxHeight / 3) + fm.getAscent() / 2;
        g2d.drawString(playersText, playersTextX, playersTextY);
    }

    /**
     * Get the pitch width.
     */
    public int getPitchWidth() {
        return pitchWidth;
    }

    /**
     * Get the pitch height.
     */
    public int getPitchHeight() {
        return pitchHeight;
    }

    /**
     * Calculate horizontal positions for N players in a row.
     * Players are evenly spaced, with more centering for rows with fewer players.
     *
     * @param numPlayers Number of players in this row
     * @return Array of X-coordinates for each player
     */
    private int[] calculateHorizontalPositions(int numPlayers) {
        int[] positions = new int[numPlayers];

        int currentWidth = getWidth() > 0 ? getWidth() : DISPLAY_WIDTH;

        int margin;
        if (numPlayers <= 2) {
            margin = (int)(currentWidth * 0.40);
        } else if (numPlayers == 3) {
            margin = (int)(currentWidth * 0.24);
        } else {
            margin = (int)(currentWidth * 0.12);
        }

        int availableWidth = currentWidth - (2 * margin);

        if (numPlayers == 1) {
            positions[0] = currentWidth / 2 - 40;
        } else {
            int spacing = availableWidth / (numPlayers - 1);
            for (int i = 0; i < numPlayers; i++) {
                positions[i] = margin + (i * spacing) - 40;
            }
        }

        return positions;
    }

    /**
     * Set a listener to be called when an actual player button is clicked.
     *
     * @param listener Consumer that receives the clicked player (guaranteed non-null)
     */
    public void setPlayerClickListener(Consumer<Player> listener) {
        this.playerClickListener = listener;
    }

    /**
     * Set a listener to be called when a placeholder "Add Player" button is clicked.
     *
     * @param listener Consumer that receives the position type ("GK", "DEF", "MID", "FWD")
     */
    public void setPlaceholderClickListener(Consumer<String> listener) {
        this.placeholderClickListener = listener;
    }

    // ========== TEAM MANAGEMENT ==========

    /**
     * Set the team to be displayed on the pitch.
     * Call refresh() after this to update the visualization.
     *
     * @param team The team to display (must have 15 players)
     */
    public void setTeam(Team team) {
        this.currentTeam = team;
        System.out.println("Team set with " + (team != null ? team.getPlayers().size() : 0) + " total slots");
    }

    /**
     * Get the currently displayed team.
     *
     * @return The current team, or null if none is set
     */
    public Team getTeam() {
        return currentTeam;
    }

    /**
     * Refresh the team visualization with current data.
     * Should be called when the component becomes visible or after setTeam().
     */
    public void refresh() {
        System.out.println("TeamVisualizationPanel refresh() called");

        // Clear any existing player buttons
        removeAll();

        // Display the current team
        if (currentTeam != null) {
            displayTeam(currentTeam);
        } else {
            System.err.println("⚠️ No team set - nothing to display");
        }

        // Repaint the panel
        revalidate();
        repaint();
    }

    // ========== DISPLAY TEAM ==========

    /**
     * Display a team on the pitch.
     * Positions players according to their position type (GK, DEF, MID, FWD).
     *
     * @param team The team to display
     */
    private void displayTeam(Team team) {
        List<Player> players = team.getPlayers();

        System.out.println("Displaying team with " + players.size() + " total slots (" +
                team.getFilledSlots() + " filled, " + team.getEmptySlots() + " empty)");

// Y-coordinates for each position row
        int currentHeight = getHeight() > 0 ? getHeight() : DISPLAY_HEIGHT;
        int goalKeeperY = (int)(currentHeight * 0.07);
        int defenderY = (int)(currentHeight * 0.30);
        int midfielderY = (int)(currentHeight * 0.53);
        int forwardY = (int)(currentHeight * 0.76);

// Separate players by position (skip nulls)
        List<Player> goalkeepers = new ArrayList<>();
        List<Player> defenders = new ArrayList<>();
        List<Player> midfielders = new ArrayList<>();
        List<Player> forwards = new ArrayList<>();

        for (Player player : players) {
            if (player == null) {
                // Skip null players - we'll add placeholders later
                continue;
            }

            switch (player.getElementType()) {
                case 1: goalkeepers.add(player); break;
                case 2: defenders.add(player); break;
                case 3: midfielders.add(player); break;
                case 4: forwards.add(player); break;
            }
        }

        System.out.println("Positioning players: " + goalkeepers.size() + " GK, " +
                defenders.size() + " DEF, " + midfielders.size() + " MID, " +
                forwards.size() + " FWD");

        // Add goalkeepers (with placeholders for empty slots)
        // Add goalkeepers (with placeholders for empty slots)
        addPlayersWithPlaceholders(goalkeepers, 2, goalKeeperY, "GK");

        // Add defenders (with placeholders for empty slots)
        addPlayersWithPlaceholders(defenders, 5, defenderY, "DEF");

        // Add midfielders (with placeholders for empty slots)
        addPlayersWithPlaceholders(midfielders, 5, midfielderY, "MID");

        // Add forwards (with placeholders for empty slots)
        addPlayersWithPlaceholders(forwards, 3, forwardY, "FWD");
    }

    /**
     * Add players to a row, filling remaining slots with placeholders.
     *
     * @param players List of actual players (non-null)
     * @param totalSlots Total number of positions in this row (e.g., 2 for GK, 5 for DEF)
     * @param yPosition Y-coordinate for this row
     */
    private void addPlayersWithPlaceholders(List<Player> players, int totalSlots, int yPosition, String position) {
        int[] xPositions = calculateHorizontalPositions(totalSlots);

        for (int i = 0; i < totalSlots; i++) {
            if (i < players.size()) {
                // Add actual player
                Player player = players.get(i);
                String kitPath = getKitPathForPlayer(player);
                addPlayerButton(player, kitPath, xPositions[i], yPosition, position);
            } else {
                // Add placeholder for empty slot
                addPlayerButton(null, "", xPositions[i], yPosition, position);
            }
        }
    }



    /**
     * Dynamically build the kit image path for a player based on their team and position.
     *
     * This constructs paths like:
     * - "src/main/resources/images/kits/Kit=Arsenal (Home).png" for outfield players
     * - "src/main/resources/images/kits/Kit=Arsenal (GK).png" for goalkeepers
     *
     * @param player The player
     * @return Full path to the kit image
     */
    private String getKitPathForPlayer(Player player) {
        String teamName = player.getTeamName();
        boolean isGoalkeeper = player.getElementType() == 1;

        // Determine kit type based on position
        String kitType = isGoalkeeper ? "GK" : "Home";

        // Build the path dynamically
        String kitPath = BASE_KIT_PATH + "Kit=" + teamName + " (" + kitType + ").png";

        System.out.println("Kit path for " + player.getWebName() + ": " + kitPath);

        return kitPath;
    }

    /**
     * Add a player button to the pitch at the specified coordinates.
     */
    private void addPlayerButton(Player player, String kitPath, int x, int y, String position) {
        PlayerKitButton button = new PlayerKitButton(player, kitPath);

        int currentWidth = getWidth() > 0 ? getWidth() : DISPLAY_WIDTH;
        int currentHeight = getHeight() > 0 ? getHeight() : DISPLAY_HEIGHT;
        int buttonWidth = (int)(currentWidth * 0.16);
        int buttonHeight = (int)(currentHeight * 0.17);
        button.setBounds(x, y, buttonWidth, buttonHeight);

        // Add click listener that calls appropriate listener based on placeholder status
        button.addActionListener(e -> {
            if (button.isPlaceholder()) {
                System.out.println("Clicked placeholder - Add Player at position: " + position);
                // Call placeholder listener if set, passing the position
                if (placeholderClickListener != null) {
                    placeholderClickListener.accept(position);
                }
            } else {
                System.out.println("Clicked: " + player.getWebName() + " (" + position + ")");
                // Call player listener if set
                if (playerClickListener != null) {
                    playerClickListener.accept(player);
                }
            }
        });

        this.add(button);
    }

    /**
     * Create an empty team with all slots as null (placeholders).
     * Useful for team creation flow where user will fill slots one by one.
     *
     * @return A Team with 15 null players (isConfirmed will be false)
     */
    public static Team createEmptyTeam() {
        List<Player> emptyPlayers = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            emptyPlayers.add(null);
        }
        return new Team(emptyPlayers, 100.0f, false);
    }

    /**
     * Find a player by their web name (partial match).
     * Returns the first matching player found.
     *
     * @param playerDataAccess The data access to search in
     * @param partialName Part of the player's name to search for
     * @return The player if found, null otherwise
     */
    private static Player findPlayerByName(PlayerDataAccessInterface playerDataAccess, String partialName) {
        if (playerDataAccess == null) {
            System.err.println("PlayerDataAccess is null - cannot find players");
            return null;
        }

        List<Player> allPlayers = playerDataAccess.getAllPlayers();

        System.out.println("Searching for '" + partialName + "' among " + allPlayers.size() + " players");

        for (Player player : allPlayers) {
            if (player.getWebName().equals(partialName)) {
                System.out.println("✓ Found player: " + player.getWebName() + " (" + player.getTeamName() + ", " + player.getPosition() + ")");
                return player;
            }
        }

        System.err.println("✗ Player not found: " + partialName);
        return null;
    }

    /**
     * Create a partially filled test team (mix of real players and nulls).
     * Useful for testing team-building in progress.
     *
     * @param playerDataAccess The data access to search for players
     * @return A Team with some positions filled, rest are null
     */
    public static Team createPartialTestTeam(PlayerDataAccessInterface playerDataAccess) {
        System.out.println("Creating partial test team...");

        List<Player> partialPlayers = new ArrayList<>();

        // 2 Goalkeepers - 1 filled, 1 empty
        partialPlayers.add(findPlayerByName(playerDataAccess, "Vicario"));  // GK 1 - filled
        partialPlayers.add(null);                                            // GK 2 - empty

        // 5 Defenders - 3 filled, 2 empty
        partialPlayers.add(findPlayerByName(playerDataAccess, "Tarkowski")); // DEF 1 - filled
        partialPlayers.add(findPlayerByName(playerDataAccess, "Van de Ven")); // DEF 2 - filled
        partialPlayers.add(null);                                             // DEF 3 - empty
        partialPlayers.add(findPlayerByName(playerDataAccess, "Saliba"));     // DEF 4 - filled
        partialPlayers.add(null);                                             // DEF 5 - empty

        // 5 Midfielders - 2 filled, 3 empty
        partialPlayers.add(findPlayerByName(playerDataAccess, "M.Salah"));   // MID 1 - filled
        partialPlayers.add(null);                                             // MID 2 - empty
        partialPlayers.add(findPlayerByName(playerDataAccess, "Palmer"));     // MID 3 - filled
        partialPlayers.add(null);                                             // MID 4 - empty
        partialPlayers.add(null);                                             // MID 5 - empty

        // 3 Forwards - 1 filled, 2 empty
        partialPlayers.add(findPlayerByName(playerDataAccess, "Watkins"));   // FWD 1 - filled
        partialPlayers.add(null);                                             // FWD 2 - empty
        partialPlayers.add(null);                                             // FWD 3 - empty

        System.out.println("Partial team created with " + partialPlayers.size() + " total slots");

        return new Team(partialPlayers, 100.0f, false);
    }
}