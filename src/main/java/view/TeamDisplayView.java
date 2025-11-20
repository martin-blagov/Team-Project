package view;

import entity.Player;
import entity.Team;
import interface_adapter.team_view.TeamViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;



public class TeamDisplayView extends JPanel implements PropertyChangeListener{

    private final TeamViewModel teamViewModel;
    private final JLabel headerLabel;
    private final JLabel teamStatusLabel;
    private final JLabel budgetLabel;
    private final JTable playersTable;
    private final DefaultTableModel tableModel;
    private final TeamViewModel.DisplayConfig displayConfig;
    private final String viewName;
    private final JButton backButton;

    /**
     * Creates a reusable team display and renders the current team state.
     */
    public TeamDisplayView(TeamViewModel teamViewModel) {
        this.teamViewModel = teamViewModel;
        this.displayConfig = teamViewModel.getDisplayConfig();
        this.viewName = teamViewModel.getViewName();
        teamViewModel.addPropertyChangeListener(this);

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setBackground(Color.WHITE);

        this.headerLabel = new JLabel(displayConfig.getTitle());
        this.headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(this.headerLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        this.teamStatusLabel = new JLabel(displayConfig.getEmptyStateMessage());
        this.teamStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoPanel.add(this.teamStatusLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        this.budgetLabel = new JLabel("Remaining Budget: N/A");
        this.budgetLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        if (displayConfig.shouldShowBudget()) {
            infoPanel.add(this.budgetLabel);
        }

        this.add(infoPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Players");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 12));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        this.tableModel = new DefaultTableModel(displayConfig.getColumnHeaders(), 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };

        this.playersTable = new JTable(this.tableModel);
        this.playersTable.setRowHeight(25);
        this.playersTable.setFont(new Font("Arial", Font.BOLD, 11));
        this.playersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        this.playersTable.getSelectionBackground();

        JScrollPane scrollPane = new JScrollPane(this.playersTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        this.add(tablePanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionPanel.setBackground(Color.WHITE);
        this.backButton = new JButton("Back");
        this.backButton.setVisible(false);
        actionPanel.add(this.backButton);
        this.add(actionPanel, BorderLayout.SOUTH);

        displayTeam(teamViewModel.getTeam());
    }


    public void displayTeam(Team team) {
        if (team == null) {
            this.teamStatusLabel.setText(displayConfig.getEmptyStateMessage());
            if (displayConfig.shouldShowBudget()) {
                this.budgetLabel.setText("Remaining Budget: N/A");
            }
            this.tableModel.setRowCount(0);
            return;
        }

        String statusText;
        if (team.isConfirmed()) {
            statusText = "Team Status: Confirmed";
        } else {
            statusText = "Team Status: Not Confirmed";
        }
        this.teamStatusLabel.setText(statusText);
        this.teamStatusLabel.setForeground(Color.BLACK);

        if (displayConfig.shouldShowBudget()) {
            String budgetText = "Remaining budget: " + team.getBudget();
            this.budgetLabel.setText(budgetText);
        }


        this.tableModel.setRowCount(0); // Clear existing rows

        for (Player player : team.getPlayers()) {
            Object[] rowData = createRow(player);
            this.tableModel.addRow(rowData);
        }

        resizeColumns();
    }

    /**
     * Builds a single row for the configured table headers using properties from {@link Player}.
     */
    private Object[] createRow(Player player) {
        String[] headers = displayConfig.getColumnHeaders();
        Object[] row = new Object[headers.length];
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            switch (header.toLowerCase()) {
                case "name":
                    row[i] = player.getWebName();
                    break;
                case "position":
                    row[i] = player.getPosition();
                    break;
                case "club":
                    row[i] = player.getStatus();
                    break;
                case "price":
                    row[i] = String.format("£%.1fM", player.getNowCost());
                    break;
                case "points":
                    row[i] = player.getPredictedPoints();
                    break;
                default:
                    row[i] = "";
            }
        }
        return row;
    }


    public void clearTeamDisplay() {
        this.teamStatusLabel.setText("Team Status: Null");
        this.teamStatusLabel.setForeground(Color.BLACK);
        this.budgetLabel.setText("Remaining Budget: Null");
        this.tableModel.setRowCount(0);
    }


    private void resizeColumns() {
        for (int column = 0; column < playersTable.getColumnCount(); column++) {
            int width = 50; // Min width
            for (int row = 0; row < playersTable.getRowCount(); row++) {
                Object value = this.playersTable.getValueAt(row, column);
                Component comp = playersTable.getDefaultRenderer(playersTable.getColumnClass(column))
                        .getTableCellRendererComponent(playersTable, value, false, false, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            this.playersTable.getColumnModel().getColumn(column).setPreferredWidth(width + 10);
        }
    }


    public int getPlayerCount() {
        return this.tableModel.getRowCount();
    }


    public boolean hasTeamDisplayed() {
        return this.tableModel.getRowCount() > 0;
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TeamViewModel.TEAM_STATE_LABEL.equals(evt.getPropertyName())) {
            Team team = this.teamViewModel.getTeam();
            displayTeam(team);
        }
    }


    public String getViewName() {
        return viewName;
    }

    /**
     * Enables the optional back button.
     */
    public void setBackAction(String label, Runnable action) {
        String buttonLabel = (label == null || label.isBlank()) ? "Back" : label;
        this.backButton.setText(buttonLabel);
        for (ActionListener listener : backButton.getActionListeners()) {
            backButton.removeActionListener(listener);
        }
        if (action != null) {
            backButton.addActionListener(evt -> action.run());
            backButton.setVisible(true);
        } else {
            backButton.setVisible(false);
        }
    }
}
