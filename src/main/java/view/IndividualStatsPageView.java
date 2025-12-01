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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

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
    JPanel imageBox = new JPanel();
    JLabel imageLabel = new JLabel();

    private static final String BASE_PATH = "src/main/resources/images/kits/";

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
        filterComboBox.setMaximumSize(new Dimension(100, 50));

        // Update stats field based on filter selected
        filterComboBox.addActionListener(e -> {
            // If selected player equals null, change filterOption
            String filterOption = filterComboBox.getSelectedItem().toString();
            if (selectedPlayer != null) {
                DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(selectedPlayer.getId(), filterOption);
                displayIndividualStatController.execute(inputData);
            }
        });

        // Stats Panel Display
        JPanel outerStats = new JPanel();
        outerStats.setLayout(new BoxLayout(outerStats, BoxLayout.Y_AXIS));
        outerStats.add(Box.createHorizontalStrut(200));
        outerStats.add(Box.createVerticalStrut(50));
        outerStats.add(statsPanel);

        JPanel wrapperStatsPanel = new JPanel(new BorderLayout());
        wrapperStatsPanel.add(outerStats, BorderLayout.NORTH);
        wrapperStatsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Player Stats - Current Season"));

        wrapperStatsPanel.setPreferredSize(new Dimension(500, 500));
        wrapperStatsPanel.setMaximumSize(new Dimension(500, 500));
        wrapperStatsPanel.setMinimumSize(new Dimension(500, 500));


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


        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (playerListController != null) {
                    playerListController.loadAllPlayers();
                }
            }
        });

        // Scrollable View Display Settings
        scrollableListView.setDimensions(600, 600);
        JPanel wrapperPlayerListPanel = new JPanel();
        wrapperPlayerListPanel.add(scrollableListView);

        // Home Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton homeButton = new JButton("Back");

        homeButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();

            // Fields reset when clicking home
            statsPanel.setVisible(false);
            filterComboBox.setSelectedItem(filterOptions[0]);

            scrollableListView.resetScrollableViewList();

            selectedPlayer = null;
        });

        buttonPanel.add(homeButton);


        // Final Screen Display
        JPanel screen = new JPanel(new BorderLayout());
        screen.add(wrapperPlayerListPanel, BorderLayout.WEST);
        screen.add(wrapperStatsPanel, BorderLayout.CENTER);
        screen.add(buttonPanel, BorderLayout.PAGE_START);

        setLayout(new BorderLayout());
        add(screen);
    }

    private String retrievePlayerImagePath() {
        if (selectedPlayer != null) {
            String teamName = selectedPlayer.getTeamName();
            boolean isGoalkeeper = selectedPlayer.getElementType() == 1;

            String playerType = isGoalkeeper ? "GK" : "Home";
            String playerPath = BASE_PATH + "Kit=" + teamName + " (" + playerType + ").png";

            System.out.println("Image path for " + selectedPlayer.getWebName() + ": " + playerPath);
            return playerPath;
        }
        return "";
    }

    private void displayImage() {
        // Create Player Image

        final String imagePath = retrievePlayerImagePath();

        BufferedImage image = null;

        try {
            image = ImageIO.read(new File(imagePath));
        }
        catch (IOException exception) {
            System.out.println("Error in loading image...");
        }

        if (image != null) {
            Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            ImageIcon imageIcon = new ImageIcon(scaledImage);
            imageLabel.setText("");
            imageLabel.setIcon(imageIcon);
            imageBox.add(imageLabel);
            imageBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
        else {
            imageLabel.setIcon(null);
            imageLabel.setText("Error");
        }
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
            updateStatsField(state);
        }
    }

    private void setStatsPanelLayout(JPanel statsPanel) {
        setLabelSize();


        imageBox.setPreferredSize(new Dimension(100, 100));
        imageBox.setMinimumSize(new Dimension(100, 100));
        imageBox.setMaximumSize(new Dimension(100, 100));
        imageBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        filterComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        positionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        teamLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        costLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        goalsScoredLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        assistsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.add(filterComboBox);
        statsPanel.add(Box.createVerticalStrut(100));
        statsPanel.add(imageBox);
        statsPanel.add(nameLabel);
        statsPanel.add(imageBox);
        statsPanel.add(nameLabel);
        statsPanel.add(positionLabel);
        statsPanel.add(teamLabel);
        statsPanel.add(costLabel);
        statsPanel.add(goalsScoredLabel);
        statsPanel.add(assistsLabel);
        statsPanel.add(pointsLabel);
    }


    private void setLabelSize() {
        final JLabel[] labels = {nameLabel, positionLabel, teamLabel, costLabel, goalsScoredLabel, assistsLabel, pointsLabel};
        final int WIDTH = 500;
        final int SIZE = 50;

        for (JLabel label : labels) {
            label.setFont(new Font("Arial", Font.BOLD, 20));
            label.setPreferredSize(new Dimension(WIDTH, SIZE));
            label.setMaximumSize(new Dimension(WIDTH, SIZE));
            label.setMinimumSize(new Dimension(WIDTH, SIZE));
        }
    }


    private void updateStatsField(DisplayIndividualStatState state) {
        nameLabel.setText("Name: " + state.getPlayerName());
        positionLabel.setText("Position: " + state.getPlayerPosition());
        teamLabel.setText("Team: " + state.getPlayerTeam());
        costLabel.setText("Cost: €" + state.getPlayerCost());
        goalsScoredLabel.setText("Goals: " + state.getPlayerGoals());
        assistsLabel.setText("Assists: " + state.getPlayerAssists());
        pointsLabel.setText("Points: " + state.getPlayerPoints());
        displayImage();
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
