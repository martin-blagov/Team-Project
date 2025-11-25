package view;

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
    final JLabel priceLabel = new JLabel();
    final JLabel goalsScoredLabel = new JLabel();
    final JLabel assistsLabel = new JLabel();
    final JLabel pointsLabel = new JLabel();

    public IndividualStatsPageView(DisplayIndividualStatViewModel viewModel, PlayerDataAccessInterface playerDataAccess,
                                   ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        viewModel.addPropertyChangeListener(this);

        final JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.add(nameLabel);
        statsPanel.add(positionLabel);
        statsPanel.add(teamLabel);
        statsPanel.add(priceLabel);
        statsPanel.add(goalsScoredLabel);
        statsPanel.add(assistsLabel);
        statsPanel.add(pointsLabel);


        // Use the scrollable list component
        scrollableListView = new ScrollableListView(playerDataAccess);

        // Set dimensions (optional)
        scrollableListView.setDimensions(600, 500);

        // Define what happens when a player is clicked
        scrollableListView.setPlayerSelectionListener(player -> {
            DisplayIndividualStatInputData inputData = new DisplayIndividualStatInputData(player.getId());
            displayIndividualStatController.execute(inputData);
        });

        // This is to decide how the panel looks.
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.add(scrollableListView);
        wrapperPanel.add(statsPanel);

        JButton homeButton = new JButton("Home");
        homeButton.addActionListener(e -> {viewManagerModel.setState("home"); viewManagerModel.firePropertyChange();});
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(homeButton);

        JPanel screen = new JPanel();
        screen.setLayout(new BoxLayout(screen, BoxLayout.Y_AXIS));
        screen.add(wrapperPanel);
        screen.add(buttonPanel);

        this.setLayout(new BorderLayout());
        this.add(screen);




        // THIS IS REQUIRED FOR THE LIST TO WORK PROPERLY. COPY AND PASTE THIS.
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
        // priceLabel.setText("Price: " + state.getPlayerCost());
    }

    public void setDisplayIndividualStatController(DisplayIndividualStatController controller) {
        this.displayIndividualStatController = controller;
    }
}
