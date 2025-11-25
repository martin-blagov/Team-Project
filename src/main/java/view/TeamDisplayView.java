package view;

import entity.Player;
import entity.Team;
import interface_adapter.team_view.TeamViewModel;
import interface_adapter.starting_lineup.StartingLineupViewModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Reusable view for displaying players in the team.
 */
public class TeamDisplayView extends JPanel implements PropertyChangeListener {

    private static final String DEFAULT_FONT_FAMILY = "Arial";

    private final TeamViewModel teamViewModel;
    private final JLabel headerLabel;
    private final JLabel teamStatusLabel;
    private final JLabel budgetLabel;
    private final JTable playersTable;
    private final DefaultTableModel tableModel;
    private final TeamViewModel.DisplayConfig displayConfig;
    private final String viewName;
    private final JButton backButton;
    private JTable benchTable;
    private DefaultTableModel benchTableModel;
    private final StartingLineupViewModel startingLineupViewModel; // may be null for non-lineup views

    public TeamDisplayView(TeamViewModel teamViewModel) {
        this(teamViewModel, null);
    }

    public TeamDisplayView(TeamViewModel teamViewModel, StartingLineupViewModel startingLineupViewModel) {
        this.teamViewModel = teamViewModel;
        this.startingLineupViewModel = startingLineupViewModel;
        this.displayConfig = teamViewModel.getDisplayConfig();
        this.viewName = teamViewModel.getViewName();
        teamViewModel.addPropertyChangeListener(this);
        if (this.startingLineupViewModel != null) {
            this.startingLineupViewModel.addPropertyChangeListener(this);
        }

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setBackground(Color.WHITE);

        this.headerLabel = new JLabel(displayConfig.getTitle());
        this.headerLabel.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 16));
        infoPanel.add(this.headerLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        this.teamStatusLabel = new JLabel(displayConfig.getEmptyStateMessage());
        this.teamStatusLabel.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 12));
        infoPanel.add(this.teamStatusLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        this.budgetLabel = new JLabel("Remaining Budget: N/A");
        this.budgetLabel.setFont(new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, 12));
        if (displayConfig.shouldShowBudget()) {
            infoPanel.add(this.budgetLabel);
        }

        this.add(infoPanel, BorderLayout.NORTH);

        // Center panel will contain both the main table and optional bench table stacked vertically.
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        // Main players table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Players");
        tableTitle.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 12));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        this.tableModel = new DefaultTableModel(displayConfig.getColumnHeaders(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };

        this.playersTable = new JTable(this.tableModel);
        this.playersTable.setRowHeight(25);
        this.playersTable.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 11));
        this.playersTable.getTableHeader().setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane(this.playersTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Wrap tablePanel in a container with a preferred height to emphasize it.
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        centerPanel.add(tablePanel);

        // Optional bench table (for starting lineup view only).
        this.benchTableModel = new DefaultTableModel(displayConfig.getColumnHeaders(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.benchTable = new JTable(benchTableModel);
        this.benchTable.setRowHeight(22);
        this.benchTable.setFont(new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, 11));
        this.benchTable.getTableHeader().setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 10));

        if ("starting lineup".equalsIgnoreCase(this.viewName)) {
            JPanel benchPanel = new JPanel(new BorderLayout());
            benchPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
            benchPanel.setBackground(Color.WHITE);
            JLabel benchTitle = new JLabel("Bench");
            benchTitle.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 12));
            benchPanel.add(benchTitle, BorderLayout.NORTH);
            JScrollPane benchScroll = new JScrollPane(benchTable);
            benchScroll.setPreferredSize(new Dimension(0, 120)); // smaller height than main table
            benchPanel.add(benchScroll, BorderLayout.CENTER);
            benchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
            centerPanel.add(benchPanel);
        }

        this.add(centerPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionPanel.setBackground(Color.WHITE);
        this.backButton = new JButton("Back");
        this.backButton.setVisible(false);
        actionPanel.add(this.backButton);
        this.add(actionPanel, BorderLayout.SOUTH);

        displayTeam(teamViewModel.getTeam());
    }

    /**
     * Renders the given team into the table.
     */
    public void displayTeam(Team team) {
        displayTeam(team, null);
    }

    public void displayTeam(Team team, List<Player> benchPlayers) {
        if (team == null) {
            this.teamStatusLabel.setText(displayConfig.getEmptyStateMessage());
            if (displayConfig.shouldShowBudget()) {
                this.budgetLabel.setText("Remaining Budget: N/A");
            }
            this.tableModel.setRowCount(0);
            return;
        }

        String statusText = team.isConfirmed() ? "Team Status: Confirmed" : "Team Status: Not Confirmed";
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

        // Populate bench table if present
        if (benchTableModel != null) {
            benchTableModel.setRowCount(0);
            if (benchPlayers != null) {
                for (Player player : benchPlayers) {
                    Object[] rowData = createRow(player);
                    benchTableModel.addRow(rowData);
                }
            }
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
                    row[i] = player.getTeamName();
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
        String property = evt.getPropertyName();
        if (TeamViewModel.TEAM_STATE_LABEL.equals(property)) {
            // Team has changed (starting lineup or other team view)
            Team team = this.teamViewModel.getTeam();
            // If we have a startingLineupViewModel, also pass its bench players
            if (startingLineupViewModel != null) {
                displayTeam(team, startingLineupViewModel.getBenchPlayers());
            } else {
                displayTeam(team);
            }
        } else if ("startingLineup".equals(property) && startingLineupViewModel != null) {
            // Starting lineup state changed (bench or starting team updated)
            Team team = this.teamViewModel.getTeam();
            displayTeam(team, startingLineupViewModel.getBenchPlayers());
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
