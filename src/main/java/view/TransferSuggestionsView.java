package view;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import entity.Player;
import interface_adapter.ViewManagerModel;
import interface_adapter.transfer_suggestions.TransferSuggestionsController;
import interface_adapter.transfer_suggestions.TransferSuggestionsState;
import interface_adapter.transfer_suggestions.TransferSuggestionsViewModel;
import use_case.transfer_suggestions.TransferSuggestionsOutputData;
import view.components.TeamVisualizationPanel;

/**
 * View for Transfer Suggestions use case.
 * Displays original team, suggested team, and transfer details side by side.
 */
public class TransferSuggestionsView extends JPanel implements PropertyChangeListener {

    // Constants for sizing
    private static final int TEAM_PANEL_WIDTH = 350;
    private static final int TEAM_PANEL_HEIGHT = 450;
    private static final int DETAILS_PANEL_WIDTH = 250;

    private final String viewName = "transfer suggestions";
    private final String font = "Arial";

    // Clean Architecture components
    private final TransferSuggestionsViewModel viewModel;
    private final ViewManagerModel viewManagerModel;
    private TransferSuggestionsController controller;

    // UI Components
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

    public TransferSuggestionsView(TransferSuggestionsViewModel viewModel,
                                   ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        // Listen to ViewModel changes
        viewModel.addPropertyChangeListener(this);

        final int gaps = 15;
        setLayout(new BorderLayout(gaps, gaps));
        setBorder(BorderFactory.createEmptyBorder(gaps, gaps, gaps, gaps));

        // ===== INITIALIZE FIELDS FIRST (BEFORE CREATING PANELS) =====
        final int maximumplayers = 15;
        numberOfTransfersSpinner = new JSpinner(new SpinnerNumberModel(0, 0, maximumplayers, 1));
        suggestButton = new JButton(TransferSuggestionsViewModel.SUGGEST_BUTTON_LABEL);
        transfersListPanel = new JPanel();
        transfersListPanel.setLayout(new BoxLayout(transfersListPanel, BoxLayout.Y_AXIS));
        transfersScrollPane = new JScrollPane(transfersListPanel);
        totalImprovementLabel = new JLabel("Total Improvement: --");
        newBudgetLabel = new JLabel("New Budget: --");

        // ===== NORTH: Title Bar =====
        final JPanel titlePanel = new JPanel(new BorderLayout());

        // Left side: Back button
        backButton = new JButton(TransferSuggestionsViewModel.BACK_BUTTON_LABEL);
        backButton.addActionListener(event -> {
            if (controller != null) {
                controller.switchToHomePage();
            }
        });
        titlePanel.add(backButton, BorderLayout.WEST);

        // Center: Title
        final JLabel titleLabel = new JLabel(TransferSuggestionsViewModel.TITLE_LABEL);
        titleLabel.setFont(new Font(font, Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        // ===== CENTER: Main Content =====
        final JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.X_AXIS));

        // LEFT: Original Team Panel (with aspect ratio wrapper)

        originalTeamPanel = new TeamVisualizationPanel();
        originalTeamPanel.setPreferredSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        originalTeamPanel.setMinimumSize(new Dimension(300, 450));
        originalTeamPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Add click listener for original team players
        originalTeamPanel.setPlayerClickListener(this::showPlayerDetails);

        final JPanel originalTeamCentering = new JPanel();
        originalTeamCentering.setLayout(new BoxLayout(originalTeamCentering, BoxLayout.X_AXIS));
        originalTeamCentering.add(Box.createHorizontalGlue());
        originalTeamCentering.add(originalTeamPanel);
        originalTeamCentering.add(Box.createHorizontalGlue());

        final JPanel originalTeamWrapper = createTeamPanelWrapper("Your Current Team");
        originalTeamWrapper.add(originalTeamCentering, BorderLayout.CENTER);
        mainContentPanel.add(originalTeamWrapper);

        mainContentPanel.add(Box.createRigidArea(new Dimension(gaps, 0)));

        // MIDDLE: Suggested Team Panel (with aspect ratio wrapper)
        suggestedTeamPanel = new TeamVisualizationPanel();
        suggestedTeamPanel.setPreferredSize(new Dimension(TEAM_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        suggestedTeamPanel.setMinimumSize(new Dimension(300, 450));
        suggestedTeamPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Add click listener for suggested team players
        suggestedTeamPanel.setPlayerClickListener(this::showPlayerDetails);

        final JPanel suggestedTeamCentering = new JPanel();
        suggestedTeamCentering.setLayout(new BoxLayout(suggestedTeamCentering, BoxLayout.X_AXIS));
        suggestedTeamCentering.add(Box.createHorizontalGlue());
        suggestedTeamCentering.add(suggestedTeamPanel);
        suggestedTeamCentering.add(Box.createHorizontalGlue());

        final JPanel suggestedTeamWrapper = createTeamPanelWrapper("Suggested Team");
        suggestedTeamWrapper.add(suggestedTeamCentering, BorderLayout.CENTER);
        mainContentPanel.add(suggestedTeamWrapper);

        mainContentPanel.add(Box.createRigidArea(new Dimension(gaps, 0)));

        // RIGHT: Transfer Details Panel
        final JPanel detailsPanel = createDetailsPanel();
        detailsPanel.setPreferredSize(new Dimension(DETAILS_PANEL_WIDTH, TEAM_PANEL_HEIGHT));
        detailsPanel.setMinimumSize(new Dimension(220, 450));
        detailsPanel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));
        mainContentPanel.add(detailsPanel);

        // Wrap mainContentPanel in a JScrollPane with both scrollbars
        final JScrollPane mainScrollPane = new JScrollPane(mainContentPanel);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setBorder(null);

        add(mainScrollPane, BorderLayout.CENTER);
        // Auto-load when view becomes visible
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (controller != null) {
                    numberOfTransfersSpinner.setValue(0);
                    final TransferSuggestionsState state = viewModel.getState();
                    state.setNumberOfTransfers(0);
                    viewModel.setState(state);
                }
            }
        });
    }

    /**
     * Create a wrapper panel for team visualization with titled border.
     * @param title title for our team panel
     * @return wrapper class for our team panel
     */
    private JPanel createTeamPanelWrapper(String title) {
        final JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                title,
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font(font, Font.BOLD, 14)
        ));
        return wrapper;
    }

    /**
     * Create the transfer details panel on the right side.
     */
    private JPanel createDetailsPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                "Transfer Details",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font(font, Font.BOLD, 14)
        ));

        // Input Section
        final JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JLabel spinnerLabel = new JLabel(TransferSuggestionsViewModel.NUMBER_OF_TRANSFERS_LABEL);
        spinnerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(spinnerLabel);

        inputPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        numberOfTransfersSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        numberOfTransfersSpinner.setMaximumSize(new Dimension(100, 30));
        inputPanel.add(numberOfTransfersSpinner);

        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        suggestButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        suggestButton.addActionListener(event -> {
            if (controller != null) {
                // Update the state with selected number of transfers
                final TransferSuggestionsState state = viewModel.getState();
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
        final JPanel transfersSection = new JPanel(new BorderLayout());
        transfersSection.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        transfersSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JLabel transfersLabel = new JLabel("Suggested Transfers:");
        transfersLabel.setFont(new Font(font, Font.BOLD, 12));
        transfersSection.add(transfersLabel, BorderLayout.NORTH);

        transfersScrollPane.setPreferredSize(new Dimension(300, 400));
        transfersScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        transfersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        transfersSection.add(transfersScrollPane, BorderLayout.CENTER);

        panel.add(transfersSection);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Summary Section
        final JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalImprovementLabel.setFont(new Font(font, Font.BOLD, 14));
        totalImprovementLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.add(totalImprovementLabel);

        summaryPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        newBudgetLabel.setFont(new Font(font, Font.PLAIN, 12));
        newBudgetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.add(newBudgetLabel);

        panel.add(summaryPanel);

        // Add glue to push everything to the top
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Create a panel for a single transfer swap.
     * @param swap transfer to be made
     * @return display for swaps
     */
    private JPanel createSwapPanel(TransferSuggestionsOutputData.PlayerSwap swap) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        final Player playerOut = swap.getPlayerOut();
        // OUT player
        final JLabel outLabel = new JLabel("⬇ OUT: " + playerOut.getWebName());
        outLabel.setFont(new Font(font, Font.BOLD, 12));
        // Red
        outLabel.setForeground(new Color(200, 0, 0));
        outLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(outLabel);

        final JLabel outDetails = new JLabel(String.format("    %s • £%.1fm",
                playerOut.getTeamName(), playerOut.getNowCost()));
        outDetails.setFont(new Font(font, Font.PLAIN, 10));
        outDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(outDetails);

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        final Player playerIn = swap.getPlayerIn();
        // IN player
        final JLabel inLabel = new JLabel("⬆ IN:  " + playerIn.getWebName());
        inLabel.setFont(new Font(font, Font.BOLD, 12));
        // Green
        inLabel.setForeground(new Color(0, 150, 0));
        inLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inLabel);

        final JLabel inDetails = new JLabel(String.format("    %s • £%.1fm",
                playerIn.getTeamName(), playerIn.getNowCost()));
        inDetails.setFont(new Font(font, Font.PLAIN, 10));
        inDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(inDetails);

        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Improvement
        final double improvement = swap.getPointsImprovement();
        final String improvementText;
        if (improvement >= 0) {
            improvementText = String.format("📈 Improvement: +%.1f pts", improvement);
        }
        else {
            improvementText = String.format("📉 Change: %.1f pts", improvement);
        }

        final JLabel improvementLabel = new JLabel(improvementText);
        improvementLabel.setFont(new Font(font, Font.BOLD, 11));
        if (improvement >= 0) {
            improvementLabel.setForeground(new Color(0, 100, 0));
        }
        else {
            improvementLabel.setForeground(Color.RED);
        }
        improvementLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(improvementLabel);

        return panel;
    }

    /**
     * Show player details in a dialog when clicked.
     * @param player player whose details we want
     */
    private void showPlayerDetails(Player player) {
        final String message = String.format(
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
     * @param elementType element type describes position as int.
     * @return return string giving position of player
     */
    private String getPositionName(int elementType) {
        String position = "Unknown";
        switch (elementType) {
            case 1:
                position = "Goalkeeper";
                break;
            case 2:
                position = "Defender";
                break;
            case 3:
                position = "Midfielder";
                break;
            case 4:
                position = "Forward";
                break;
            default:
                position = "Unknown";
        }
        return position;
    }

    /**
     * Set the Controller.
     * Called by AppBuilder after wiring everything together.
     * @param controller controller that we want to set for trasnfersuggestions
     */
    public void setController(TransferSuggestionsController controller) {
        this.controller = controller;
    }

    /**
     * Property change listener - called when ViewModel changes.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final TransferSuggestionsState state = viewModel.getState();

        // Handle loading state
        if (state.isLoading()) {
            suggestButton.setEnabled(false);
            numberOfTransfersSpinner.setEnabled(false);
        }
        // Handle errors
        else if (state.getErrorMessage() != null) {
            // Re-enable controls
            suggestButton.setEnabled(true);
            numberOfTransfersSpinner.setEnabled(true);

            JOptionPane.showMessageDialog(
                    this,
                    state.getErrorMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        // Handle normal state
        else {
            // Re-enable controls
            suggestButton.setEnabled(true);
            numberOfTransfersSpinner.setEnabled(true);

            // ALWAYS update teams if they exist (not just on success message)
            if (state.getOriginalTeam() != null) {
                originalTeamPanel.setTeam(state.getOriginalTeam());
                originalTeamPanel.refresh();
            }

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
     * @param swaps the transfers to be made
     */
    private void updateTransfersList(List<TransferSuggestionsOutputData.PlayerSwap> swaps) {
        transfersListPanel.removeAll();

        if (swaps == null || swaps.isEmpty()) {
            final JLabel emptyLabel = new JLabel("No transfers suggested");
            emptyLabel.setFont(new Font(font, Font.ITALIC, 12));
            emptyLabel.setForeground(Color.GRAY);
            transfersListPanel.add(emptyLabel);
        }
        else {
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
     * @param state state for our transfersuggestions
     */
    private void updateSummary(TransferSuggestionsState state) {
        final double totalImprovement = state.getTotalPointsImprovement();

        if (totalImprovement >= 0) {
            totalImprovementLabel.setText(String.format("Total Improvement: +%.1f pts", totalImprovement));
            totalImprovementLabel.setForeground(new Color(0, 100, 0));
        }
        else {
            totalImprovementLabel.setText(String.format("Total Change: %.1f pts", totalImprovement));
            totalImprovementLabel.setForeground(Color.RED);
        }

        if (state.getSuggestedTeam() != null) {
            final float newBudget = state.getSuggestedTeam().getBudget();
            newBudgetLabel.setText(String.format("New Budget: £%.1fm", newBudget));
        }
    }

    public String getViewName() {
        return viewName;
    }
}
