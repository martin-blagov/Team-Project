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
    private final JLabel statusLabel;
    final JLabel nameLabel = new JLabel();
    final JLabel positionLabel = new JLabel();
    final JLabel teamLabel = new JLabel();
    final JLabel costLabel = new JLabel();
    final JLabel goalsScoredLabel = new JLabel();
    final JLabel assistsLabel = new JLabel();
    final JLabel pointsLabel = new JLabel();
    private Player currentPlayer = null;

    // Displaly Info Filtering Options
    final String[] filterOptions = {"Total", "Average", "Last 3", "Last 5"};
    final JComboBox<String> filterComboBox = new JComboBox<>(filterOptions);

    public IndividualStatsPageView(DisplayIndividualStatViewModel displayIndividualStatViewModel, TestDisplayPlayersViewModel playerListViewModel,
                                   PlayerDataAccessInterface playerDataAccess, ViewManagerModel viewManagerModel) {

        this.displayIndividualStatViewModel = displayIndividualStatViewModel;
        this.playerListViewModel = playerListViewModel;
        this.viewManagerModel = viewManagerModel;

        displayIndividualStatViewModel.addPropertyChangeListener(this);
        playerListViewModel.addPropertyChangeListener(this);

        // Stats Display Layout Components
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
            //if selected player == null, change filterOption
            String filterOption = filterComboBox.getSelectedItem().toString();
            DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(currentPlayer.getId(), filterOption);
            displayIndividualStatController.execute(inputData);
        });


        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title panel at top
        JPanel titlePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Test Display Players - Clean Architecture Demo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titlePanel.add(statusLabel, BorderLayout.EAST);



        // Create the dumb ScrollableListView component (NO data access!)
        scrollableListView = new ScrollableListViewV2();

        // Wire up the callback - when filters change, call Controller
        scrollableListView.setOnFilterChange(criteria -> {
            System.out.println("Controller: " + playerListController);
            if (playerListController != null) {
                // THIS is Clean Architecture!
                // View doesn't fetch data - it asks Controller to do it
                playerListController.filterPlayers(
                        criteria.searchText,
                        criteria.positionFilter,
                        criteria.teamFilter,
                        criteria.maxPrice
                );
                statusLabel.setText("Filtering...");
            }
        });

        // NEW: Add player selection listener for testing
        scrollableListView.setPlayerSelectionListener(selectedPlayer -> {
            currentPlayer = selectedPlayer;
            String filterOption = filterComboBox.getSelectedItem().toString();
            DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(selectedPlayer.getId(), filterOption);
            displayIndividualStatController.execute(inputData);

            // Update status label
            statusLabel.setText("Selected: " + selectedPlayer.getWebName());
        });



        // Setting up how the list is displayed
        scrollableListView.setDimensions(550, 500);
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.add(scrollableListView);
        wrapperPanel.add(Box.createHorizontalGlue());
        wrapperPanel.add(statsPanel);
        add(wrapperPanel, BorderLayout.CENTER);

        // Button panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });

        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        JPanel screen = new JPanel();
        screen.setLayout(new BoxLayout(screen, BoxLayout.Y_AXIS));
        screen.add(titlePanel);
        screen.add(wrapperPanel);
        screen.add(buttonPanel);

        this.setLayout(new BorderLayout());
        this.add(screen);

        // Load data when component is actually shown (not when constructed)
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (playerListController != null) {
                    playerListController.loadAllPlayers();
                    statusLabel.setText("Loading players...");
                }
            }
        });
    }



    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("property firing");

        if (evt.getSource() == playerListViewModel) {
            System.out.println("loading players");
            TestDisplayPlayersState playerListState = playerListViewModel.getState();

            if (playerListState.getErrorMessage() != null) {
                statusLabel.setText("Error: " + playerListState.getErrorMessage());
                JOptionPane.showMessageDialog(this,
                        playerListState.getErrorMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update the ScrollableListView with new data
            // (Passing clean data - NOT fetching it ourselves!)
            scrollableListView.setPlayers(
                    playerListState.getPlayers(),
                    playerListState.getAvailableTeams()
            );

            // Update status label
            int playerCount = playerListState.getPlayers().size();
            statusLabel.setText(playerCount + " players loaded");
        }
        else if (evt.getSource() == displayIndividualStatViewModel) {
            DisplayIndividualStatState state = displayIndividualStatViewModel.getState();

            // Player Update
            nameLabel.setText("Name: " + state.getPlayerName());
            positionLabel.setText("Position: " + state.getPlayerPosition());
            teamLabel.setText("Team: " + state.getPlayerTeam());
            costLabel.setText("Cost: €" + state.getPlayerCost());
            goalsScoredLabel.setText("Goals: " + state.getPlayerGoals());
            assistsLabel.setText("Assists: " + state.getPlayerAssists());
            pointsLabel.setText("Points: " + state.getPlayerPoints());
        }
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
