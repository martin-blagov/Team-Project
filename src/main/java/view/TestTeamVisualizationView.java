package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.display_team. DisplayTeamController;
import interface_adapter.display_team.DisplayTeamState;
import interface_adapter.display_team.DisplayTeamViewModel;
import view.components.TeamVisualizationPanel;

import javax.swing.*;
import java.awt.*;
import java. beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Test Team Visualization View
 * This view demonstrates how to use the TeamVisualizationPanel component
 */
public class TestTeamVisualizationView extends JPanel implements PropertyChangeListener {

    private final String viewName = "test team visualization";

    // Clean Architecture components
    private final DisplayTeamViewModel viewModel;
    private DisplayTeamController controller;

    // UI components
    private final TeamVisualizationPanel teamVisualizationPanel;
    private final JLabel statusLabel;
    private final ViewManagerModel viewManagerModel;

    /**
     * Constructor
     *
     * @param viewModel The ViewModel for this use case
     * @param viewManagerModel For navigation
     */
    public TestTeamVisualizationView(DisplayTeamViewModel viewModel,
                                     ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        // Listen to ViewModel changes
        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== NORTH: Title panel =====
        JPanel titlePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Team Visualization - Clean Architecture Demo");
        titleLabel.setFont(new Font("Arial", Font. BOLD, 24));
        titlePanel. add(titleLabel, BorderLayout.WEST);


        statusLabel = new JLabel("No team loaded");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titlePanel.add(statusLabel, BorderLayout. EAST);

        add(titlePanel, BorderLayout.NORTH);

        // ===== CENTER: Team Visualization Panel =====
        teamVisualizationPanel = new TeamVisualizationPanel();

        // Wire up player click callback
        teamVisualizationPanel.setPlayerClickListener(player -> {
            // When user clicks on a player, show their details
            String message = String.format(
                    "Player: %s\nTeam: %s\nPosition: %s\nPrice: £%.1fm",
                    player.getWebName(),
                    player.getTeamName(),
                    getPositionName(player.getElementType()),
                    player.getNowCost()
            );

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Player Details",
                    JOptionPane. INFORMATION_MESSAGE
            );

            statusLabel.setText("Selected: " + player.getWebName());
        });

        // Wire up placeholder click callback
        teamVisualizationPanel.setPlaceholderClickListener(position -> {
            // When user clicks on empty slot, prompt to add player
            String message = String.format(
                    "Add a %s player?\n\nIn a full implementation, this would open the player selection screen.",
                    getFullPositionName(position)
            );

            int result = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Add Player",
                    JOptionPane. YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                statusLabel.setText("Would navigate to add " + position + " player.. .");
                // In real app: controller.openPlayerSelection(position);
                // Or: viewManagerModel. setState("player selection");
            }
        });

        // Setting up how the panel is displayed, use this format since it allows it to resize.

        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.X_AXIS));
        // NOTE: If you add the following lines in the opposite order( add panel first then glue),
        // panel is pushed to the left. This order pushes the panel to the right.
        wrapperPanel.add(Box.createHorizontalGlue());
        wrapperPanel.add(teamVisualizationPanel);

        add(wrapperPanel, BorderLayout.CENTER);



//        // ===== EAST: Instructions panel =====
//        JPanel instructionsPanel = createInstructionsPanel();
//        add(instructionsPanel, BorderLayout.EAST);

        // ===== SOUTH: Button panel =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh Team");
        refreshButton. addActionListener(e -> {
            if (controller != null) {
                controller. loadTeam();
            }
        });

        JButton backButton = new JButton("Back to Home");
        backButton. addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });

        buttonPanel. add(refreshButton);
        buttonPanel. add(backButton);
        add(buttonPanel, BorderLayout. SOUTH);

        // Load data when component is actually shown (not when constructed)
        addComponentListener(new java. awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (controller != null) {
                    controller. loadTeam();
                }
            }
        });

    }

    /**
     * Create the instructions panel on the right side.
     *
     * @return JPanel with usage instructions
     */
    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("How to Use"));
        panel.setPreferredSize(new Dimension(200, 0));

        String[] instructions = {
                "Click on a player to",
                "view their details.",
                "",
                "Click on a '+' placeholder",
                "to add a new player.",
                "",
                "Positions:",
                "• GK: 2 Goalkeepers",
                "• DEF: 5 Defenders",
                "• MID: 5 Midfielders",
                "• FWD: 3 Forwards",
                "",
                "Total: 15 players"
        };

        for (String line : instructions) {
            JLabel label = new JLabel(line);
            label. setAlignmentX(Component.LEFT_ALIGNMENT);
            panel. add(label);
            panel.add(Box.createVerticalStrut(3));
        }

        return panel;
    }

    /**
     * Convert element type to position name.
     *
     * @param elementType 1=GK, 2=DEF, 3=MID, 4=FWD
     * @return Full position name
     */
    private String getPositionName(int elementType) {
        switch (elementType) {
            case 1: return "Goalkeeper";
            case 2: return "Defender";
            case 3: return "Midfielder";
            case 4: return "Forward";
            default: return "Unknown";
        }
    }

    /**
     * Convert position abbreviation to full name.
     *
     * @param position "GK", "DEF", "MID", or "FWD"
     * @return Full position name
     */
    private String getFullPositionName(String position) {
        switch (position) {
            case "GK": return "Goalkeeper";
            case "DEF": return "Defender";
            case "MID": return "Midfielder";
            case "FWD": return "Forward";
            default: return position;
        }
    }

    /**
     * Set the Controller.
     * Called by AppBuilder after wiring everything together.
     *
     * @param controller The Controller for this use case
     */
    public void setController(DisplayTeamController controller) {
        this. controller = controller;
        // Don't load here - let componentShown() do it when view is actually displayed
    }

    /**
     * Property change listener - called when ViewModel changes.
     *
     * This is the OBSERVER PATTERN in action.
     * When the Presenter updates the ViewModel, we get notified here.
     *
     * @param evt Property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Get the updated state from ViewModel
        DisplayTeamState state = viewModel. getState();

        // Check for errors
        if (state. getErrorMessage() != null) {
            statusLabel.setText("Error: " + state.getErrorMessage());
            JOptionPane.showMessageDialog(
                    this,
                    state.getErrorMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Update the TeamVisualizationPanel with new team data
        // (Passing clean data from ViewModel - NOT fetching it ourselves!)
        if (state.getTeam() != null) {
            teamVisualizationPanel.setTeam(state.getTeam());
            teamVisualizationPanel.refresh();

            // Update status label with team info
            int filledSlots = state.getTeam().getFilledSlots();
            float budget = state.getTeam().getBudget();
            statusLabel.setText(String.format(
                    "%d/15 players | Budget: £%.1fm",
                    filledSlots,
                    budget
            ));
        }
    }

    public String getViewName() {
        return viewName;
    }
}