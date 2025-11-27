package view;

import interface_adapter.ViewManagerModel;
import use_case.PlayerDataAccessInterface;
import view.components.ScrollableListView;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test view for the ScrollableListView component.
 */
public class TestScrollableListView extends JPanel {
    private final String viewName = "test scrollable list";
    private final ScrollableListView scrollableListView;
    private final ViewManagerModel viewManagerModel;

    public TestScrollableListView(PlayerDataAccessInterface playerDataAccess,
                                  ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;

        setLayout(new BorderLayout());

        //HOW TO USE THE SCROLLABLE LIST
        //-------------------------------------------------------------------------------------------------------------
        // Use the scrollable list component
        scrollableListView = new ScrollableListView(playerDataAccess);

        // Set dimensions (optional)
        scrollableListView.setDimensions(600, 500);


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

        // This is to decide how the panel looks.
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.add(scrollableListView);
        add(wrapperPanel, BorderLayout.CENTER);

        // THIS IS REQUIRED FOR THE LIST TO WORK PROPERLY. COPY AND PASTE THIS.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                scrollableListView.refresh();
            }
        });

        //-------------------------------------------------------------------------------------------------------------

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