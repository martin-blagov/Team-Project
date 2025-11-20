package view;

import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.display_individual_stat.DisplayIndividualStatState;
import interface_adapter.display_individual_stat.DisplayIndividualStatViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class IndividualStatsPageView extends JPanel implements PropertyChangeListener {
    private final String viewName = "display individual stats";
    private final DisplayIndividualStatViewModel viewModel;
    private DisplayIndividualStatController displayIndividualStatController;

    // Display Basic Player Info
    private final JLabel playerNameLabel = new JLabel("Name: ");
    private final JLabel playerNameField = new JLabel();

    private final JLabel playerTeamLabel = new JLabel("Team: ");
    private final JLabel playerTeamField = new JLabel();

    private final JLabel playerPositionLabel = new JLabel("Position: ");
    private final JLabel playerPositionField = new JLabel();;

    private final JLabel playerCurrentCostLabel = new JLabel("Current Cost: ");
    private final JLabel playerCurrentCostField = new JLabel();;

    // Display Current Season Stat
    private final JLabel currentSeasonStatsLabel = new JLabel("Current Season Stats: ");

    private final JLabel goalsScoredLabel = new JLabel("Goals Scored: ");
    private final JLabel goalsScoredField = new JLabel();;

    private final JLabel goalsAssistedLabel = new JLabel("Goals Assisted: ");
    private final JLabel goalsAssistedField = new JLabel();;




    public IndividualStatsPageView(DisplayIndividualStatViewModel viewModel) {
        this.viewModel = viewModel;
        viewModel.addPropertyChangeListener(this);

        this.setLayout(new BorderLayout());

        this.add(playerNameLabel);
    }

    public String getViewName() { return viewName;}


    public void propertyChange(PropertyChangeEvent evt) {
        final DisplayIndividualStatState state = (DisplayIndividualStatState) evt.getNewValue();
    }


    public void setDisplayIndividualStatController(DisplayIndividualStatController controller) {
        this.displayIndividualStatController = controller;
    }

}
