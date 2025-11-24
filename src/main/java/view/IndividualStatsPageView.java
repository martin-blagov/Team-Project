package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.display_individual_stat.DisplayIndividualStatState;
import interface_adapter.display_individual_stat.DisplayIndividualStatViewModel;
import use_case.PlayerDataAccessInterface;
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

    public IndividualStatsPageView(DisplayIndividualStatViewModel viewModel, PlayerDataAccessInterface playerDataAccess,
                                   ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        viewModel.addPropertyChangeListener(this);

        final JLabel nameLabel = new JLabel(viewModel.NAME_LABEL);
        final JLabel nameField = new JLabel();

        final JLabel ageLabel = new JLabel(viewModel.AGE_LABEL);
        final JLabel ageField = new JLabel();

        final JLabel positionLabel = new JLabel(viewModel.POSITION_LABEL);
        final JLabel positionField = new JLabel();

        final JLabel teamLabel = new JLabel(viewModel.TEAM_LABEL);
        final JLabel teamField = new JLabel();

        final JLabel priceLabel = new JLabel(viewModel.PRICE_LABEL);
        final JLabel priceField = new JLabel();

        final JLabel goalsScoredLabel = new JLabel(viewModel.GOALS_SCORED_LABEL);
        final JLabel goalsScoredField = new JLabel();

        final JLabel assistsLabel = new JLabel(viewModel.ASSISTS_LABEL);
        final JLabel assistsField = new JLabel();

        final JLabel pointsLabel = new JLabel(viewModel.POINTS_LABEL);
        final JLabel pointsField = new JLabel();

        final JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.add(nameLabel);
        statsPanel.add(nameField);
        statsPanel.add(ageLabel);
        statsPanel.add(ageField);
        statsPanel.add(positionLabel);
        statsPanel.add(positionField);
        statsPanel.add(teamLabel);
        statsPanel.add(teamField);
        statsPanel.add(priceLabel);
        statsPanel.add(priceField);
        statsPanel.add(goalsScoredLabel);
        statsPanel.add(goalsScoredField);
        statsPanel.add(assistsLabel);
        statsPanel.add(assistsField);
        statsPanel.add(pointsLabel);
        statsPanel.add(pointsField);


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
    }

    public void setDisplayIndividualStatController(DisplayIndividualStatController controller) {
        this.displayIndividualStatController = controller;
    }
}
