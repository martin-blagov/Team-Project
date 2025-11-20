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

    private final JLabel playerNameLabel = new JLabel("Name: ");
    private JLabel playerNameField;

    private final JLabel playerTeamLabel = new JLabel("Team: ");
    private JLabel playerTeamField;

    private final JLabel playerPositionLabel = new JLabel("Position: ");
    private JLabel playerPositionField;;

    private final JLabel playerCurrentCostLabel = new JLabel("Current Cost: ");
    private JLabel playerCurrentCostField;

    private final JLabel currentSeasonStatsLabel = new JLabel("Current Season Stats: ");

    private final JLabel goalsScoredLabel1 = new JLabel("Goals Scored: ");
    private JLabel goalsScoredField1;

    private final JLabel goalsAssistedLabel = new JLabel("Goals Assisted: ");
    private JLabel goalsAssistedField1;


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
