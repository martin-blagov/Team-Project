package view;

import interface_adapter.initialise_predictions.InitialisePredictionsController;
import interface_adapter.initialise_predictions.InitialisePredictionsViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Loading screen shown during initialization.
 * Automatically triggers initialization when controller is set.
 */
public class InitialisePredictionsView extends JPanel implements PropertyChangeListener {
    private final String viewName = "initialization";
    private final InitialisePredictionsViewModel viewModel;
    private InitialisePredictionsController controller;

    private final JLabel statusLabel;
    private final JLabel statsLabel;
    private final JProgressBar progressBar;

    public InitialisePredictionsView(InitialisePredictionsViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("FPL Predictions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel(viewModel.getStatusMessage());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsLabel = new JLabel("");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(300, 30));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.add(Box.createVerticalGlue());
        this.add(titleLabel);
        this.add(Box.createRigidArea(new Dimension(0, 20)));
        this.add(statusLabel);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(statsLabel);
        this.add(Box.createRigidArea(new Dimension(0, 20)));
        this.add(progressBar);
        this.add(Box.createVerticalGlue());

        updateUIFromViewModel();
    }

    /**
     * Set the controller and automatically trigger initialization.
     * This is called by AppBuilder after wiring everything together.
     */
    public void setInitialisePredictionsController(InitialisePredictionsController controller) {
        this.controller = controller;

        // Automatically start initialization when controller is set
        viewModel.setLoading(true);
        viewModel.setStatusMessage("Loading player data...");

        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            controller.execute();
        }).start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateUIFromViewModel();
    }

    private void updateUIFromViewModel() {
        statusLabel.setText(viewModel.getStatusMessage());

        if (viewModel.getTotalPlayers() > 0) {
            statsLabel.setText(String.format("%d players • %d gameweeks",
                    viewModel.getTotalPlayers(),
                    viewModel.getGameweeksProcessed()));
        }

        progressBar.setVisible(viewModel.isLoading());

        this.revalidate();
        this.repaint();
    }

    public String getViewName() {
        return viewName;
    }
}