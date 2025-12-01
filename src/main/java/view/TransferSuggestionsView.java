package view;

import entity.Player;
import entity.Team;
import interface_adapter.ViewManagerModel;
import interface_adapter.transfer_suggestions.TransferSuggestionsController;
import interface_adapter.transfer_suggestions.TransferSuggestionsState;
import interface_adapter.transfer_suggestions.TransferSuggestionsViewModel;
import use_case.transfer_suggestions.TransferSuggestionsOutputData;
import view.components.TeamVisualizationPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * View for Transfer Suggestions use case.
 * Displays original team, suggested team, and transfer details side by side.
 */
public class TransferSuggestionsView extends JPanel implements PropertyChangeListener {

    private final String viewName = "transfer suggestions";

    // Clean Architecture components
    private final TransferSuggestionsViewModel viewModel;
    private final ViewManagerModel viewManagerModel;
    private TransferSuggestionsController controller;

    // UI Components
    private final JLabel statusLabel;
    private final JSpinner numberOfTransfersSpinner;
    private final JButton suggestButton;
    private final JButton backButton;

    // Team visualization panels
    private final TeamVisualizationPanel originalTeamPanel;
    private final TeamVisualizationPanel suggestedTeamPanel;

    // Transfer details components
    private final JPanel transfersListPanel;
    private final JLabel totalImprovementLabel;
    private final JLabel newBudgetLabel;
    private final JScrollPane transfersScrollPane;

    // Constants for sizing
    private static final int TEAM_PANEL_WIDTH = 500;
    private static final int TEAM_PANEL_HEIGHT = 700;
    private static final int DETAILS_PANEL_WIDTH = 350;

    public TransferSuggestionsView(TransferSuggestionsViewModel viewModel,
                                   ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        // Listen to ViewModel changes
        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== INITIALIZE FIELDS FIRST (BEFORE CREATING PANELS) =====
        numberOfTransfersSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
        suggestButton = new JButton(TransferSuggestionsViewModel.SUGGEST_BUTTON_LABEL);
        transfersListPanel = new JPanel();
        transfersListPanel.setLayout(new BoxLayout(transfersListPanel, BoxLayout.Y_AXIS));
        transfersScrollPane = new JScrollPane(transfersListPanel);
        totalImprovementLabel = new JLabel("Total Improvement: --");
        newBudgetLabel = new JLabel("New Budget: --");

        // ===== NORTH: Title Bar =====
        JPanel titlePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel(TransferSuggestionsViewModel.TITLE_LABEL);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Enter number of transfers and click 'Suggest Transfers'");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titlePanel.add(statusLabel, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // ===== CENTER: Main Content =====
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.X_AXIS));

        // LEFT: Original Team Panel (with aspect ratio wrapper)
        JPanel originalTeamWrapper = createTeamPanelWrapper("Your Current Team");
        originalTeamPanel = new TeamVisualizationPanel();
        originalTeamPanel.setPreferredSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        originalTeamPanel.setMinimumSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        originalTeamPanel.setMaximumSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));

        // Add click listener for original team players
        originalTeamPanel.setPlayerClickListener(this::showPlayerDetails);

        JPanel originalTeamCentering = new JPanel();
        originalTeamCentering.setLayout(new BoxLayout(originalTeamCentering, BoxLayout.X_AXIS));
        originalTeamCentering.add(Box.createHorizontalGlue());
        originalTeamCentering.add(originalTeamPanel);
        originalTeamCentering.add(Box.createHorizontalGlue());

        originalTeamWrapper.add(originalTeamCentering, BorderLayout.CENTER);
        mainContentPanel.add(originalTeamWrapper);

        mainContentPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // MIDDLE: Suggested Team Panel (with aspect ratio wrapper)
        JPanel suggestedTeamWrapper = createTeamPanelWrapper("Suggested Team");
        suggestedTeamPanel = new TeamVisualizationPanel();
        suggestedTeamPanel.setPreferredSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        suggestedTeamPanel.setMinimumSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        suggestedTeamPanel.setMaximumSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));

        // Add click listener for suggested team players
        suggestedTeamPanel.setPlayerClickListener(this::showPlayerDetails);

        JPanel suggestedTeamCentering = new JPanel();
        suggestedTeamCentering.setLayout(new BoxLayout(suggestedTeamCentering, BoxLayout.X_AXIS));
        suggestedTeamCentering.add(Box.createHorizontalGlue());
        suggestedTeamCentering.add(suggestedTeamPanel);
        suggestedTeamCentering.add(Box.createHorizontalGlue());

        suggestedTeamWrapper.add(suggestedTeamCentering, BorderLayout.CENTER);
        mainContentPanel.add(suggestedTeamWrapper);

        mainContentPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // RIGHT: Transfer Details Panel
        JPanel detailsPanel = createDetailsPanel();
        detailsPanel.setPreferredSize(new Dimension(DETAILS_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        detailsPanel.setMaximumSize(new Dimension(DETAILS_PANEL_WIDTH, Integer.MAX_VALUE));
        mainContentPanel.add(detailsPanel);

        // Wrap mainContentPanel in a JScrollPane with both scrollbars
        JScrollPane mainScrollPane = new JScrollPane(mainContentPanel);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane. VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setBorder(null); // Remove scroll pane border if you want clean look

        add(mainScrollPane, BorderLayout.CENTER);
        // ===== SOUTH: Navigation Buttons =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        backButton = new JButton(TransferSuggestionsViewModel.BACK_BUTTON_LABEL);
        backButton.addActionListener(e -> {
            if (controller != null) {
                controller.switchToHomePage();
            }
        });

        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Auto-load when view becomes visible
        addComponentListener(new java.awt.event. ComponentAdapter() {
            @Override
            public void componentShown(java.awt. event.ComponentEvent e) {
                if (controller != null) {
                    numberOfTransfersSpinner.setValue(0);
                    TransferSuggestionsState state = viewModel.getState();
                    state.setNumberOfTransfers(0);
                    viewModel.setState(state);
                    controller.execute();
                }
            }
        });
    }

    /**
     * Create a wrapper panel for team visualization with titled border.
     */
    private JPanel createTeamPanelWrapper(String title) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                title,
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));
        return wrapper;
    }

    /**
     * Create the transfer details panel on the right side.
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                "Transfer Details",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));

        // Input Section
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel spinnerLabel = new JLabel(TransferSuggestionsViewModel.NUMBER_OF_TRANSFERS_LABEL);
        spinnerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(spinnerLabel);

        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        numberOfTransfersSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        numberOfTransfersSpinner.setMaximumSize(new Dimension(100, 30));
        inputPanel.add(numberOfTransfersSpinner);

        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        suggestButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        suggestButton.addActionListener(e -> {
            if (controller != null) {
                // Update the state with selected number of transfers
                TransferSuggestionsState state = viewModel.getState();
                state.setNumberOfTransfers((Integer) numberOfTransfersSpinner.getValue());
                viewModel.setState(state);

                // Execute the use case
                controller.execute();
            }
        });
        inputPanel.add(suggestButton);

        panel.add(inputPanel);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Transfers List Section
        JPanel transfersSection = new JPanel(new BorderLayout());
        transfersSection.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        transfersSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel transfersLabel = new JLabel("Suggested Transfers:");
        transfersLabel.setFont(new Font("Arial", Font.BOLD, 12));
        transfersSection.add(transfersLabel, BorderLayout.NORTH);

        transfersScrollPane.setPreferredSize(new Dimension(300, 400));
        transfersScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        transfersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        transfersSection.add(transfersScrollPane, BorderLayout.CENTER);

        panel.add(transfersSection);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Summary Section
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalImprovementLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalImprovementLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.add(totalImprovementLabel);

        summaryPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        newBudgetLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        newBudgetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.add(newBudgetLabel);

        panel.add(summaryPanel);

        // Add glue to push everything to the top
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Create a panel for a single transfer swap.
     */
    private JPanel createSwapPanel(TransferSuggestionsOutputData.PlayerSwap swap) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        Player playerOut = swap.getPlayerOut();
        Player playerIn = swap.getPlayerIn();

        // OUT player
        JLabel outLabel = new JLabel("⬇ OUT: " + playerOut.getWebName());
        outLabel.setFont(new Font("Arial", Font.BOLD, 12));
        outLabel.setForeground(new Color(200, 0, 0)); // Red
        outLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(outLabel);

        JLabel outDetails = new JLabel(String.format("    %s • £%.1fm",
                playerOut.getTeamName(), playerOut.getNowCost()));
        outDetails.setFont(new Font("Arial", Font.PLAIN, 10));
        outDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(outDetails);

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // IN player
        JLabel inLabel = new JLabel("⬆ IN:  " + playerIn.getWebName());
        inLabel.setFont(new Font("Arial", Font.BOLD, 12));
        inLabel.setForeground(new Color(0, 150, 0)); // Green
        inLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inLabel);

        JLabel inDetails = new JLabel(String.format("    %s • £%.1fm",
                playerIn.getTeamName(), playerIn.getNowCost()));
        inDetails.setFont(new Font("Arial", Font.PLAIN, 10));
        inDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inDetails);

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Improvement
        double improvement = swap.getPointsImprovement();
        String improvementText = improvement >= 0
                ? String.format("📈 Improvement: +%.1f pts", improvement)
                : String.format("📉 Change: %.1f pts", improvement);

        JLabel improvementLabel = new JLabel(improvementText);
        improvementLabel.setFont(new Font("Arial", Font.BOLD, 11));
        improvementLabel.setForeground(improvement >= 0 ? new Color(0, 100, 0) : Color.RED);
        improvementLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(improvementLabel);

        return panel;
    }

    /**
     * Show player details in a dialog when clicked.
     */
    private void showPlayerDetails(Player player) {
        String message = String.format(
                "Player: %s\nTeam: %s\nPosition: %s\nPrice: £%.1fm\nPredicted Points: %.1f",
                player.getWebName(),
                player.getTeamName(),
                getPositionName(player.getElementType()),
                player.getNowCost(),
                player.getPredictedPoints() != null ? player.getPredictedPoints() : 0.0
        );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Player Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Convert element type to position name.
     */
    private String getPositionName(int elementType) {
        switch (elementType) {
            case 1: return "Goalkeeper";
            case 2: return "Defender";
            case 3: return "Midfielder";
            case 4: return "Forward";
            default: return "Unknown";
        }
    }

    /**
     * Set the Controller.
     * Called by AppBuilder after wiring everything together.
     */
    public void setController(TransferSuggestionsController controller) {
        this.controller = controller;
    }

    /**
     * Property change listener - called when ViewModel changes.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        TransferSuggestionsState state = viewModel.getState();

        // Handle loading state
        if (state.isLoading()) {
            statusLabel.setText("Searching for optimal transfers...");
            suggestButton.setEnabled(false);
            numberOfTransfersSpinner.setEnabled(false);
            return;
        }

        // Re-enable controls
        suggestButton.setEnabled(true);
        numberOfTransfersSpinner.setEnabled(true);

        // Handle errors
        if (state.getErrorMessage() != null) {
            statusLabel.setText("Error: " + state.getErrorMessage());
            JOptionPane.showMessageDialog(
                    this,
                    state.getErrorMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Handle success
        if (state.getSuccessMessage() != null) {
            statusLabel.setText(state.getSuccessMessage());

            // Update original team (if changed)
            if (state.getOriginalTeam() != null) {
                originalTeamPanel.setTeam(state.getOriginalTeam());
                originalTeamPanel.refresh();
            }

            // Update suggested team
            if (state.getSuggestedTeam() != null) {
                suggestedTeamPanel.setTeam(state.getSuggestedTeam());
                suggestedTeamPanel.refresh();
            }

            // Update transfers list
            updateTransfersList(state.getSwaps());

            // Update summary
            updateSummary(state);
        }
    }

    /**
     * Update the transfers list panel with swap details.
     */
    private void updateTransfersList(List<TransferSuggestionsOutputData.PlayerSwap> swaps) {
        transfersListPanel.removeAll();

        if (swaps == null || swaps.isEmpty()) {
            JLabel emptyLabel = new JLabel("No transfers suggested");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            emptyLabel.setForeground(Color.GRAY);
            transfersListPanel.add(emptyLabel);
        } else {
            for (TransferSuggestionsOutputData.PlayerSwap swap : swaps) {
                transfersListPanel.add(createSwapPanel(swap));
                transfersListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        transfersListPanel.revalidate();
        transfersListPanel.repaint();
    }

    /**
     * Update the summary section with total improvement and new budget.
     */
    private void updateSummary(TransferSuggestionsState state) {
        double totalImprovement = state.getTotalPointsImprovement();

        if (totalImprovement >= 0) {
            totalImprovementLabel.setText(String.format("Total Improvement: +%.1f pts", totalImprovement));
            totalImprovementLabel.setForeground(new Color(0, 100, 0));
        } else {
            totalImprovementLabel.setText(String.format("Total Change: %.1f pts", totalImprovement));
            totalImprovementLabel.setForeground(Color.RED);
        }

        if (state.getSuggestedTeam() != null) {
            float newBudget = state.getSuggestedTeam().getBudget();
            newBudgetLabel.setText(String.format("New Budget: £%.1fm", newBudget));
        }
    }

    public String getViewName() {
        return viewName;
    }
}