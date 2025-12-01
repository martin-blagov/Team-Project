package view.components;

import entity.Player;
import entity.Team;

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
 * Team Visualization Panel - DUMB UI Component
 *
 * Displays a football team on a pitch with player kit images.
 * Receives team data from parent view via setTeam().
 * Notifies parent of user interactions via callbacks.
 */
public class TeamVisualizationPanel extends JPanel {

    private BufferedImage pitchImage;
    private Team currentTeam;

    private Consumer<Player> playerClickListener;
    private Consumer<String> placeholderClickListener;

    private int pitchWidth;
    private int pitchHeight;

    private static final int DISPLAY_WIDTH = 500;
    private static final int DISPLAY_HEIGHT = 700;

    private static final String PITCH_IMAGE_PATH = "src/main/resources/images/pitch/Football_Pitch.jpg";
    private static final String BASE_KIT_PATH = "src/main/resources/images/kits/";

    private boolean showBudgetBox = true;

    /**
     * Create a new team visualization panel.
     * Data is provided later via setTeam() method.
     */
    public TeamVisualizationPanel() {
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
            pitchWidth = 800;
            pitchHeight = 1200;
        }
    }

    /**
     * Setup panel properties and listeners.
     */
    private void setupPanel() {
        setLayout(null);
        setOpaque(true);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (currentTeam != null) {
                    // Use SwingUtilities.invokeLater to ensure new dimensions are available
                    SwingUtilities.invokeLater(() -> refresh());
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (pitchImage != null) {
            g.drawImage(pitchImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback: green background
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);

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

        if (currentTeam != null && showBudgetBox) {
            drawBudgetBox(g);
        }
    }

    public void setShowBudgetBox(boolean show) {
        this.showBudgetBox = show;
    }


    /**
     * Draw budget information box at top right.
     */
    // todo - kind of ugly, find a better alternative
    private void drawBudgetBox(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float budget = currentTeam.getBudget();
        int filledSlots = currentTeam.getFilledSlots();
        int totalSlots = 15;

        int boxWidth = (int)(getWidth() * 0.25);
        int boxHeight = (int)(getHeight() * 0.12);
        int boxX = getWidth() - boxWidth - 10;
        int boxY = 10;

        // Draw background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

        // Draw border
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new java.awt.BasicStroke(2));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

        // Draw text
        g2d.setColor(Color.WHITE);
        int fontSize = Math.max(12, boxHeight / 5);
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));

        String budgetText = String.format("Budget: £%.1fm", budget);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = boxX + (boxWidth - fm.stringWidth(budgetText)) / 2;
        int textY = boxY + boxHeight / 3 + fm.getAscent() / 2;
        g2d.drawString(budgetText, textX, textY);

        String playersText = String.format("Players: %d/%d", filledSlots, totalSlots);
        int playersTextX = boxX + (boxWidth - fm.stringWidth(playersText)) / 2;
        int playersTextY = boxY + (2 * boxHeight / 3) + fm.getAscent() / 2;
        g2d.drawString(playersText, playersTextX, playersTextY);
    }

    public int getPitchWidth() {
        return pitchWidth;
    }

    public int getPitchHeight() {
        return pitchHeight;
    }

    /**
     * Set the team to be displayed.
     * Must call refresh() after this to update the display.
     *
     * @param team The team to display (must have 15 player slots, can contain nulls)
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
     * Refresh the team visualization.
     * Call this after setTeam() or when the display needs to be updated.
     */
    public void refresh() {
        System.out.println("TeamVisualizationPanel refresh() called");
        System.out.println("Panel dimensions: " + getWidth() + "x" + getHeight()); // DEBUG

        removeAll();

        if (currentTeam != null) {
            displayTeam(currentTeam);
        } else {
            System.err.println("⚠️ No team set - nothing to display");
        }

        // Force immediate layout and repaint
        invalidate();
        validate();
        repaint();
    }

    /**
     * Set listener for player button clicks.
     *
     * @param listener Consumer that receives the clicked player (guaranteed non-null)
     */
    public void setPlayerClickListener(Consumer<Player> listener) {
        this.playerClickListener = listener;
    }

    /**
     * Set listener for placeholder button clicks.
     *
     * @param listener Consumer that receives the position type ("GK", "DEF", "MID", "FWD")
     */
    public void setPlaceholderClickListener(Consumer<String> listener) {
        this.placeholderClickListener = listener;
    }

    /**
     * Display a team on the pitch.
     * Creates PlayerKitButtons for each position with proper spacing.
     *
     * @param team The team to display
     */
    private void displayTeam(Team team) {
        List<Player> players = team.getPlayers();

        System.out.println("Displaying team with " + players.size() + " total slots (" +
                team.getFilledSlots() + " filled, " + team.getEmptySlots() + " empty)");

        // Calculate Y-coordinates for each position row
        int currentHeight = getHeight() > 0 ? getHeight() : DISPLAY_HEIGHT;
        int goalKeeperY = (int)(currentHeight * 0.07);
        int defenderY = (int)(currentHeight * 0.30);
        int midfielderY = (int)(currentHeight * 0.53);
        int forwardY = (int)(currentHeight * 0.76);

        // Separate players by position
        List<Player> goalkeepers = new ArrayList<>();
        List<Player> defenders = new ArrayList<>();
        List<Player> midfielders = new ArrayList<>();
        List<Player> forwards = new ArrayList<>();

        for (Player player : players) {
            if (player == null) {
                continue;
            }

            switch (player.getElementType()) {
                case 1: goalkeepers.add(player); break;
                case 2: defenders.add(player); break;
                case 3: midfielders.add(player); break;
                case 4: forwards.add(player); break;
            }
        }

        System.out.println("Positioned players: " + goalkeepers.size() + " GK, " +
                defenders.size() + " DEF, " + midfielders.size() + " MID, " +
                forwards.size() + " FWD");

        // Add players to pitch with placeholders for empty slots
        addPlayersWithPlaceholders(goalkeepers, 2, goalKeeperY, "GK");
        addPlayersWithPlaceholders(defenders, 5, defenderY, "DEF");
        addPlayersWithPlaceholders(midfielders, 5, midfielderY, "MID");
        addPlayersWithPlaceholders(forwards, 3, forwardY, "FWD");
    }

    /**
     * Add players to a position row, filling remaining slots with placeholders.
     *
     * @param players List of actual players for this position
     * @param totalSlots Total number of slots for this position
     * @param yPosition Y-coordinate for this row
     * @param position Position label ("GK", "DEF", "MID", "FWD")
     */
    private void addPlayersWithPlaceholders(List<Player> players, int totalSlots,
                                            int yPosition, String position) {
        int[] xPositions = calculateHorizontalPositions(totalSlots);

        for (int i = 0; i < totalSlots; i++) {
            if (i < players.size()) {
                Player player = players.get(i);
                String kitPath = getKitPathForPlayer(player);
                addPlayerButton(player, kitPath, xPositions[i], yPosition, position);
            } else {
                addPlayerButton(null, "", xPositions[i], yPosition, position);
            }
        }
    }

    /**
     * Calculate horizontal positions for players in a row.
     * Players are evenly spaced with margins based on row size.
     *
     * @param numPlayers Number of players in this row
     * @return Array of X-coordinates for each player
     */
    private int[] calculateHorizontalPositions(int numPlayers) {
        int[] positions = new int[numPlayers];

        int currentWidth = getWidth() > 0 ? getWidth() : DISPLAY_WIDTH;
        System.out.println("calculateHorizontalPositions: currentWidth = " + currentWidth); // DEBUG

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
        System.out.println("Calculated positions: " + java.util.Arrays.toString(positions)); // DEBUG
        return positions;
    }

    /**
     * Build kit image path for a player.
     * Outfield players use "Home" kits, goalkeepers use "GK" kits.
     *
     * @param player The player
     * @return Full path to the kit image
     */
    private String getKitPathForPlayer(Player player) {
        String teamName = player.getTeamName();
        boolean isGoalkeeper = player.getElementType() == 1;

        String kitType = isGoalkeeper ? "GK" : "Home";
        String kitPath = BASE_KIT_PATH + "Kit=" + teamName + " (" + kitType + ").png";

        System.out.println("Kit path for " + player.getWebName() + ": " + kitPath);

        return kitPath;
    }

    /**
     * Create and add a player button to the pitch.
     *
     * @param player The player to display (null for placeholder)
     * @param kitPath Path to the kit image
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param position Position label for callbacks
     */
    private void addPlayerButton(Player player, String kitPath, int x, int y, String position) {
        PlayerKitButton button = new PlayerKitButton(player, kitPath);

        int currentWidth = getWidth() > 0 ? getWidth() : DISPLAY_WIDTH;
        int currentHeight = getHeight() > 0 ? getHeight() : DISPLAY_HEIGHT;
        int buttonWidth = (int)(currentWidth * 0.15);
        int buttonHeight = (int)(currentHeight * 0.18);
        button.setBounds(x, y, buttonWidth, buttonHeight);

        button.addActionListener(e -> {
            if (button.isPlaceholder()) {
                System.out.println("Clicked placeholder - Add Player at position: " + position);
                if (placeholderClickListener != null) {
                    placeholderClickListener.accept(position);
                }
            } else {
                System.out.println("Clicked: " + player.getWebName() + " (" + position + ")");
                if (playerClickListener != null) {
                    playerClickListener.accept(player);
                }
            }
        });

        this.add(button);
    }

    /**
     * Set the preferred dimensions for this panel.
     * @param width Preferred width
     * @param height Preferred height
     */
    public void setDimensions(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
    }
}