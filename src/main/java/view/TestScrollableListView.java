package view;

import entity.Player;
import entity.Team;
import interface_adapter.ViewManagerModel;
import use_case.PlayerDataAccessInterface;
import view.components.ScrollableListView;
import view.components.TeamVisualizationPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test view for the ScrollableListView component and TeamVisualizationPanel.
 * Shows both components side by side.
 */
public class TestScrollableListView extends JPanel {
    private final String viewName = "test scrollable list";
    private final ScrollableListView scrollableListView;
    private final TeamVisualizationPanel teamVisualizationPanel;
    private final ViewManagerModel viewManagerModel;
    private final PlayerDataAccessInterface playerDataAccess;

    public TestScrollableListView(PlayerDataAccessInterface playerDataAccess,
                                  ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;
        this.playerDataAccess = playerDataAccess;

        setLayout(new BorderLayout());

        // Create main content panel with both components
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //HOW TO USE THE SCROLLABLE LIST
        //-------------------------------------------------------------------------------------------------------------
        // Use the scrollable list component
        scrollableListView = new ScrollableListView(playerDataAccess);

        // Set dimensions (optional)
        scrollableListView.setDimensions(410, 600);

        // Define what happens when a player is clicked
        scrollableListView.setPlayerSelectionListener(player -> {
            String message = String.format("%s (%s)\n%s\nPrice: £%.1fm\nPredicted: %.1f pts",
                    player.getWebName(),
                    player.getPosition(),
                    player.getTeamName(),
                    player.getNowCost(),
                    player.getPredictedPoints());
            JOptionPane.showMessageDialog(this, message, "Player Selected", JOptionPane.INFORMATION_MESSAGE);
        });

        // Wrap scrollable list in a panel with title
        JPanel listPanel = new JPanel(new BorderLayout());
        JLabel listTitle = new JLabel("Player List Component", SwingConstants.CENTER);
        listTitle.setFont(new Font("Arial", Font.BOLD, 16));
        listTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        listPanel.add(listTitle, BorderLayout.NORTH);
        listPanel.add(scrollableListView, BorderLayout.CENTER);

        // THIS IS REQUIRED FOR THE LIST TO WORK PROPERLY. COPY AND PASTE THIS.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                scrollableListView.refresh();

                // Create and set the test team, then refresh the visualization
                Team testTeam = TeamVisualizationPanel.createPartialTestTeam(playerDataAccess);
                teamVisualizationPanel.setTeam(testTeam);
                teamVisualizationPanel.refresh();
            }
        });
        //-------------------------------------------------------------------------------------------------------------

        // Add the team visualization component
        teamVisualizationPanel = new TeamVisualizationPanel(playerDataAccess);


        // Set up click listener for placeholders
        // Set up click listener for placeholders
        teamVisualizationPanel.setPlaceholderClickListener(position -> {
            JOptionPane.showMessageDialog(this,
                    "Add Player clicked!\nPosition: " + position + "\n\nThis would open player selection filtered by position.",
                    "Add Player - " + position,
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Set up click listener for actual players
        teamVisualizationPanel.setPlayerClickListener(player -> {
            String message = String.format(
                    "Player: %s\nTeam: %s\nPosition: %s\nPrice: £%.1fm\nPredicted: %.1f pts",
                    player.getWebName(),
                    player.getTeamName(),
                    player.getPosition(),
                    player.getNowCost(),
                    player.getPredictedPoints() != null ? player.getPredictedPoints() : 0.0
            );
            JOptionPane.showMessageDialog(this, message, "Player Info", JOptionPane.INFORMATION_MESSAGE);
        });

        // Wrap team visualization in a panel with title (no scroll pane)
        JPanel vizPanel = new JPanel(new BorderLayout());
        JLabel vizTitle = new JLabel("Team Visualization Component", SwingConstants.CENTER);
        vizTitle.setFont(new Font("Arial", Font.BOLD, 16));
        vizTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        vizPanel.add(vizTitle, BorderLayout.NORTH);

        // Center the pitch panel
        vizPanel.add(teamVisualizationPanel, BorderLayout.CENTER);

        // Add both components to content panel
        contentPanel.add(listPanel);
        contentPanel.add(vizPanel);

        add(contentPanel, BorderLayout.CENTER);

        // Add a back button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getViewName() {
        return viewName;
    }
}