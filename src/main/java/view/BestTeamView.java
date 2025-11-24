package view;

import entity.Player;
import interface_adapter.best_team.BestTeamViewModel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class BestTeamView extends JPanel implements PropertyChangeListener {
    private final BestTeamViewModel viewModel;
    private final DefaultTableModel tableModel;
    private final JLabel totalCostLabel;
    private final JLabel totalPointsLabel;
    private final JButton backButton = new JButton("Back");
    private Runnable backAction;

    public BestTeamView(BestTeamViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        // table in the center
        String[] columns = {"Name", "Position", "Cost", "Predicted Points"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // summary label for totals
        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        totalCostLabel = new JLabel("Total cost: 0");
        totalPointsLabel = new JLabel("Predicted points: 0");
        infoPanel.add(totalCostLabel);
        infoPanel.add(totalPointsLabel);

        // bottom panel (back on left, info on right)
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(backButton);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(infoPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });
    }

    private void refreshTable() {
        List<Player> players = viewModel.getPlayers();
        tableModel.setRowCount(0);

        for (Player p : players) {
            tableModel.addRow(new Object[] {p.getWebName(),  p.getPosition(), p.getNowCost(), p.getPredictedPoints()});
        }

        totalCostLabel.setText(String.format("Total cost: %.1f", viewModel.getTotalCost()));
        totalPointsLabel.setText(String.format("Predicted points: %.1f", viewModel.getTotalPredictedPoints()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refreshTable();
    }

    public String getViewTitle() {
        return BestTeamViewModel.VIEW_TITLE;
    }

    public void setBackAction(String label, Runnable backAction) {
        backButton.setText(label);
        this.backAction = backAction;
    }
}
