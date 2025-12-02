package view;

import entity.Player;
import entity.Team;
import interface_adapter.best_team.BestTeamViewModel;
import view.components.TeamVisualizationPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class BestTeamView extends JPanel implements PropertyChangeListener {

    private enum ViewMode {
        TABLE, GRAPHIC
    }

    private final BestTeamViewModel viewModel;

    // UI state
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel totalCostLabel;
    private final JLabel totalPointsLabel;
    private final JButton backButton = new JButton("Back");
    private Runnable backAction;

    // Card layout for center content
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final TeamVisualizationPanel visualizationPanel;

    private ViewMode currentMode = ViewMode.TABLE;

    public BestTeamView(BestTeamViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(Color.WHITE);

        // Left side: Back + title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);

        JLabel titleLabel = new JLabel("Optimal Team");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        leftHeader.add(backButton);
        leftHeader.add(Box.createRigidArea(new Dimension(10, 0)));
        leftHeader.add(titleLabel);

        headerPanel.add(leftHeader, BorderLayout.WEST);

        // Right side: totals + view toggle
        JPanel rightHeader = new JPanel();
        rightHeader.setOpaque(false);
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));

        // Totals row
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        infoPanel.setOpaque(false);
        totalCostLabel = new JLabel("Total cost: 0.0");
        totalPointsLabel = new JLabel("Predicted points: 0.0");
        infoPanel.add(totalCostLabel);
        infoPanel.add(totalPointsLabel);

        // Toggle buttons row
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        togglePanel.setOpaque(false);
        JButton tableButton = new JButton("Table");
        JButton graphicButton = new JButton("Visualization");

        tableButton.addActionListener(e -> switchView(ViewMode.TABLE));
        graphicButton.addActionListener(e -> switchView(ViewMode.GRAPHIC));

        togglePanel.add(tableButton);
        togglePanel.add(graphicButton);

        rightHeader.add(infoPanel);
        rightHeader.add(Box.createRigidArea(new Dimension(0, 5)));
        rightHeader.add(togglePanel);

        headerPanel.add(rightHeader, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // CENTER: CARD PANEL (TABLE / GRAPHIC)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        // TABLE CARD
        String[] columns = {"Name", "Position", "Cost", "Predicted Points"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tableWrapper.add(tableScroll, BorderLayout.CENTER);

        cardPanel.add(tableWrapper, ViewMode.TABLE.name());

        // GRAPHIC CARD
        visualizationPanel = new TeamVisualizationPanel();
        visualizationPanel.setShowBudgetBox(false); // optional for best team

        JPanel visInner = new JPanel();
        visInner.setBackground(Color.WHITE);
        visInner.setLayout(new BoxLayout(visInner, BoxLayout.X_AXIS));
        visInner.add(Box.createHorizontalGlue());
        visInner.add(visualizationPanel);
        visInner.add(Box.createHorizontalGlue());

        JPanel visOuter = new JPanel(new BorderLayout());
        visOuter.setBackground(Color.WHITE);
        visOuter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        visOuter.add(visInner, BorderLayout.CENTER);

        cardPanel.add(visOuter, ViewMode.GRAPHIC.name());

        add(cardPanel, BorderLayout.CENTER);

        // BACK BUTTON ACTION
        backButton.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });

        // Initial data
        refreshFromViewModel();
        showCurrentCard();
    }

    private void refreshFromViewModel() {
        // 1) Fill table
        List<Player> players = viewModel.getPlayers();
        tableModel.setRowCount(0);

        for (Player p : players) {
            tableModel.addRow(new Object[]{
                    p.getWebName(),
                    p.getPosition(),
                    p.getNowCost(),
                    p.getPredictedPoints()
            });
        }

        // 2) Update totals
        totalCostLabel.setText(String.format("Total cost: %.1f", viewModel.getTotalCost()));
        totalPointsLabel.setText(String.format("Predicted points: %.1f", viewModel.getTotalPredictedPoints()));

        // 3) Update visualization panel
        final Team team = viewModel.getTeam();
        if (team != null) {
            visualizationPanel.setTeam(team);
            visualizationPanel.refresh();
        }
    }

    private void switchView(ViewMode mode) {
        this.currentMode = mode;
        showCurrentCard();
    }

    private void showCurrentCard() {
        cardLayout.show(cardPanel, currentMode.name());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Whenever the ViewModel changes, refresh everything from it
        refreshFromViewModel();
    }

    public String getViewTitle() {
        return BestTeamViewModel.VIEW_TITLE;
    }

    public void setBackAction(String label, Runnable backAction) {
        backButton.setText(label);
        this.backAction = backAction;
    }
}
