package view.components;

import entity.Player;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A button component that displays a player's kit image and name.
 * Can be positioned on the pitch and clicked to trigger actions.
 *
 * NEW: Now supports placeholder mode for empty positions (player = null).
 */
public class PlayerKitButton extends JButton {

    private final Player player;  // Can be null for placeholders
    private BufferedImage kitImage;

    private static final int KIT_WIDTH = 80;
    private static final int KIT_HEIGHT = 100;

    // NEW: Path to placeholder image for empty positions
    private static final String PLACEHOLDER_IMAGE_PATH = "src/main/resources/images/kits/Kit=Placeholder.png";

    /**
     * Create a player kit button.
     *
     * @param player The player to display (null for "Add Player" placeholder)
     * @param kitImagePath Path to the kit image (ignored if player is null)
     */
    public PlayerKitButton(Player player, String kitImagePath) {
        this.player = player;

        if (player == null) {
            // Load placeholder image for empty positions
            loadKitImage(PLACEHOLDER_IMAGE_PATH);
        } else {
            // Load actual player's kit
            loadKitImage(kitImagePath);
        }

        setupButton();
    }

    /**
     * Load the kit image from file.
     */
    private void loadKitImage(String kitImagePath) {
        try {
            File kitFile = new File(kitImagePath);
            if (kitFile.exists()) {
                kitImage = ImageIO.read(kitFile);
            } else {
                System.err.println("Kit image not found: " + kitImagePath);
            }
        } catch (IOException e) {
            System.err.println("Error loading kit image: " + e.getMessage());
        }
    }

    /**
     * Setup button properties.
     */
    private void setupButton() {
        // Make button transparent so only the kit shows
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        // Set size
        setPreferredSize(new Dimension(KIT_WIDTH, KIT_HEIGHT + 20)); // Extra space for name
        setSize(KIT_WIDTH, KIT_HEIGHT + 20);

        // Set cursor to hand when hovering
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Paint the kit image and player name.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Use button's actual dimensions instead of fixed constants
        int kitWidth = getWidth();
        int kitHeight = (int)(getHeight() * 0.83); // Leave 17% for name

        if (kitImage != null) {
            // Draw kit image centered - use actual button dimensions
            int x = 0;
            g2d.drawImage(kitImage, x, 0, kitWidth, kitHeight, this);
        } else {
            // Fallback: draw a colored circle if image not loaded
            Color fallbackColor = (player == null) ? Color.LIGHT_GRAY : Color.RED;
            g2d.setColor(fallbackColor);
            g2d.fillOval(0, 0, kitWidth, kitHeight);
        }

        // Draw player name or "Add Player" below kit
        String displayText = (player != null) ? player.getWebName() : "Add Player";
        Color textColor = (player != null) ? Color.WHITE : Color.DARK_GRAY;

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(displayText);
        int nameX = (getWidth() - nameWidth) / 2;
        int nameY = kitHeight + 15;

        // Draw shadow for better readability
        g2d.setColor(Color.BLACK);
        g2d.drawString(displayText, nameX + 1, nameY + 1);

        // Draw name
        g2d.setColor(textColor);
        g2d.drawString(displayText, nameX, nameY);
    }

    /**
     * Get the player associated with this button.
     *
     * @return The player, or null if this is a placeholder
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Check if this button represents a placeholder (empty position).
     *
     * @return true if this is an "Add Player" placeholder, false if it has a real player
     */
    public boolean isPlaceholder() {
        return player == null;
    }

    /**
     * Add an action listener for when this player is clicked.
     * Parent views can use this to define what happens on click.
     */
    @Override
    public void addActionListener(ActionListener listener) {
        super.addActionListener(listener);
    }
}