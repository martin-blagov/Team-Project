package view;

import entity.Player;
import interface_adapter.ViewManagerModel;
import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.display_individual_stat.DisplayIndividualStatState;
import interface_adapter.display_individual_stat.DisplayIndividualStatViewModel;
import interface_adapter.test_display_players.TestDisplayPlayersController;
import interface_adapter.test_display_players.TestDisplayPlayersState;
import interface_adapter.test_display_players.TestDisplayPlayersViewModel;
import use_case.PlayerDataAccessInterface;
import use_case.display_individual_stat.DisplayIndividualStatInputData;
import view.components.ScrollableListViewV2;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class IndividualStatsPageView extends JPanel implements PropertyChangeListener {
    private final String viewName = "display individual stats";

    private final ViewManagerModel viewManagerModel;
    private final ScrollableListViewV2 scrollableListView;

    private final DisplayIndividualStatViewModel displayIndividualStatViewModel;
    private DisplayIndividualStatController displayIndividualStatController;

    private final TestDisplayPlayersViewModel playerListViewModel;
    private TestDisplayPlayersController playerListController;

    // Labels
    final JLabel nameLabel = new JLabel();
    final JLabel positionLabel = new JLabel();
    final JLabel teamLabel = new JLabel();
    final JLabel costLabel = new JLabel();
    final JLabel goalsScoredLabel = new JLabel();
    final JLabel assistsLabel = new JLabel();
    final JLabel pointsLabel = new JLabel();
    final JPanel statsPanel = new JPanel();

    // Stats Filtering Options
    final String[] filterOptions = {"Total", "Average", "Last 3", "Last 5"};
    final JComboBox<String> filterComboBox = new JComboBox<>(filterOptions);

    private Player selectedPlayer = null;

    public IndividualStatsPageView(DisplayIndividualStatViewModel displayIndividualStatViewModel, TestDisplayPlayersViewModel playerListViewModel,
                                   PlayerDataAccessInterface playerDataAccess, ViewManagerModel viewManagerModel) {

        this.displayIndividualStatViewModel = displayIndividualStatViewModel;
        this.playerListViewModel = playerListViewModel;
        this.viewManagerModel = viewManagerModel;

        displayIndividualStatViewModel.addPropertyChangeListener(this);
        playerListViewModel.addPropertyChangeListener(this);

        // Stats Display Layout Components
        setStatsPanelLayout(statsPanel);
        statsPanel.setVisible(false);

        // Total stats is displayed by default
        filterComboBox.setSelectedItem(filterOptions[0]);

        // Update stats field based on filter selected
        filterComboBox.addActionListener(e -> {
            //if selected player == null, change filterOption
            String filterOption = filterComboBox.getSelectedItem().toString();
            if (selectedPlayer != null) {
                DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(selectedPlayer.getId(), filterOption);
                displayIndividualStatController.execute(inputData);
            }
        });

        // Scrollable Player List
        scrollableListView = new ScrollableListViewV2();

        scrollableListView.setOnFilterChange(criteria -> {
            if (playerListController != null) {
                playerListController.filterPlayers(
                        criteria.searchText,
                        criteria.positionFilter,
                        criteria.teamFilter,
                        criteria.maxPrice
                );
            }
        });

        // Retrieve player stats data
        scrollableListView.setPlayerSelectionListener(selectedPlayer -> {
            this.selectedPlayer = selectedPlayer;
            statsPanel.setVisible(true);

            String filterOption = filterComboBox.getSelectedItem().toString();
            DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(selectedPlayer.getId(), filterOption);
            displayIndividualStatController.execute(inputData);
        });


        // Load data when component
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (playerListController != null) {
                    playerListController.loadAllPlayers();
                }
            }
        });


        // Scrollable View Display Settings
        scrollableListView.setDimensions(550, 500);
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.add(scrollableListView);
        wrapperPanel.add(Box.createHorizontalGlue());
        wrapperPanel.add(statsPanel);
        add(wrapperPanel, BorderLayout.CENTER);

        // Home Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton homeButton = new JButton("Home");

        homeButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();

            // Fields reset when clicking home
            statsPanel.setVisible(false);
            filterComboBox.setSelectedItem(filterOptions[0]);

            selectedPlayer = null;
        });

        buttonPanel.add(homeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Final Screen Display
        JPanel screen = new JPanel();
        screen.setLayout(new BoxLayout(screen, BoxLayout.Y_AXIS));
        screen.add(wrapperPanel);
        screen.add(buttonPanel);

        this.setLayout(new BorderLayout());
        this.add(screen);
    }


    // View Property Change
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == playerListViewModel) {
            TestDisplayPlayersState playerListState = playerListViewModel.getState();

            if (playerListState.getErrorMessage() != null) {
                JOptionPane.showMessageDialog(this,
                        playerListState.getErrorMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Fetch Data
            scrollableListView.setPlayers(
                    playerListState.getPlayers(),
                    playerListState.getAvailableTeams()
            );
        }
        else if (evt.getSource() == displayIndividualStatViewModel) {
            DisplayIndividualStatState state = displayIndividualStatViewModel.getState();

            // Update Stats
            updateStatsFields(state);
        }
    }

    private void setStatsPanelLayout(JPanel statsPanel) {
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.add(filterComboBox);
        statsPanel.add(nameLabel);
        statsPanel.add(positionLabel);
        statsPanel.add(teamLabel);
        statsPanel.add(costLabel);
        statsPanel.add(goalsScoredLabel);
        statsPanel.add(assistsLabel);
        statsPanel.add(pointsLabel);
    }


    private void updateStatsFields(DisplayIndividualStatState state) {
        nameLabel.setText("Name: " + state.getPlayerName());
        positionLabel.setText("Position: " + state.getPlayerPosition());
        teamLabel.setText("Team: " + state.getPlayerTeam());
        costLabel.setText("Cost: €" + state.getPlayerCost());
        goalsScoredLabel.setText("Goals: " + state.getPlayerGoals());
        assistsLabel.setText("Assists: " + state.getPlayerAssists());
        pointsLabel.setText("Points: " + state.getPlayerPoints());
    }


    public void setPlayerListController(TestDisplayPlayersController controller) {
        this.playerListController = controller;
    }


    public void setDisplayIndividualStatController (DisplayIndividualStatController controller){
        this.displayIndividualStatController = controller;
    }


    public String getViewName () {
        return viewName;
    }
}
