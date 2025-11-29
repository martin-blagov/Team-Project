package view;

import entity.Player;
import interface_adapter.ViewManagerModel;
import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.display_individual_stat.DisplayIndividualStatState;
import interface_adapter.display_individual_stat.DisplayIndividualStatViewModel;
import use_case.PlayerDataAccessInterface;
import use_case.display_individual_stat.DisplayIndividualStatInputData;
import view.components.ScrollableListView;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class IndividualStatsPageView extends JPanel implements PropertyChangeListener {
    private final String viewName = "display individual stats";

    private final DisplayIndividualStatViewModel viewModel;
    private final ViewManagerModel viewManagerModel;
    private DisplayIndividualStatController displayIndividualStatController;
    private final ScrollableListView scrollableListView;

    final JLabel nameLabel = new JLabel();
    final JLabel positionLabel = new JLabel();
    final JLabel teamLabel = new JLabel();
    final JLabel costLabel = new JLabel();
    final JLabel goalsScoredLabel = new JLabel();
    final JLabel assistsLabel = new JLabel();
    final JLabel pointsLabel = new JLabel();
    private Player currentPlayer = null;

    final String[] filterOptions = {"Total", "Average", "Last 3", "Last 5"};
    final JComboBox<String> filterComboBox = new JComboBox<>(filterOptions);

    public IndividualStatsPageView(DisplayIndividualStatViewModel viewModel, PlayerDataAccessInterface playerDataAccess,
                                   ViewManagerModel viewManagerModel) {

        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        viewModel.addPropertyChangeListener(this);

        final JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.add(filterComboBox);
        statsPanel.add(nameLabel);
        statsPanel.add(positionLabel);
        statsPanel.add(teamLabel);
        statsPanel.add(costLabel);
        statsPanel.add(goalsScoredLabel);
        statsPanel.add(assistsLabel);
        statsPanel.add(pointsLabel);

        // Total stats is displayed by default
        filterComboBox.setSelectedItem(filterOptions[0]);

        // Update stats field based on filter selected
        filterComboBox.addActionListener(e -> {
            String filterOption = filterComboBox.getSelectedItem().toString();
            DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(currentPlayer.getId(), filterOption);
            displayIndividualStatController.execute(inputData);
        });

        // Scrollable player list
        scrollableListView = new ScrollableListView(playerDataAccess);

        // Set dimensions
        scrollableListView.setDimensions(600, 500);

        // Player stats appear when selected
        scrollableListView.setPlayerSelectionListener(player -> {
            currentPlayer = player;
            String filterOption = filterComboBox.getSelectedItem().toString();
            DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(player.getId(), filterOption);
            displayIndividualStatController.execute(inputData);
        });

        // This is to decide how the panel looks.
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.add(scrollableListView);
        wrapperPanel.add(Box.createHorizontalGlue());
        wrapperPanel.add(statsPanel);

        // Home Button
        JButton homeButton = new JButton("Home");

        homeButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(homeButton);

        JPanel screen = new JPanel();
        screen.setLayout(new BoxLayout(screen, BoxLayout.Y_AXIS));
        screen.add(wrapperPanel);
        screen.add(buttonPanel);

        this.setLayout(new BorderLayout());
        this.add(screen);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                scrollableListView.refresh();
            }
        });


    }

    public String getViewName() { return viewName;}

    public void propertyChange(PropertyChangeEvent evt) {
        final DisplayIndividualStatState state = (DisplayIndividualStatState) evt.getNewValue();
        nameLabel.setText("Name: " + state.getPlayerName());
        positionLabel.setText("Position: " + state.getPlayerPosition());
        teamLabel.setText("Team: " + state.getPlayerTeam());
        costLabel.setText("Cost: €" + state.getPlayerCost());
        goalsScoredLabel.setText("Goals: " + state.getPlayerGoals());
        assistsLabel.setText("Assists: " + state.getPlayerAssists());
        pointsLabel.setText("Points: " + state.getPlayerPoints());
    }

    public void setDisplayIndividualStatController(DisplayIndividualStatController controller) {
        this.displayIndividualStatController = controller;
    }
}
