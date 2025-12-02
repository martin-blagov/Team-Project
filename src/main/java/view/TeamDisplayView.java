package view;

import entity.Player;
import entity.Team;
import interface_adapter.starting_lineup.StartingLineupState.ViewMode;
import interface_adapter.starting_lineup.StartingLineupViewModel;
import interface_adapter.team_view.TeamViewModel;
import view.components.TeamVisualizationPanel;

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

    /**
     * Fixed size for the team visualization graphic layout.
     */
    private enum GraphicLayoutSize {
        DESKTOP(new Dimension(640, 900));

        private final Dimension dimension;

        GraphicLayoutSize(Dimension dimension) {
            this.dimension = dimension;
        }

        Dimension getDimension() {
            return dimension;
        }
    }

    private static final String TABLE_CARD = "table";
    private static final String GRAPHIC_CARD = "graphic";
    private static final String DEFAULT_FONT_FAMILY = "Arial";

    private final TeamViewModel teamViewModel;
    private final StartingLineupViewModel startingLineupViewModel;
    private final TeamViewModel.DisplayConfig displayConfig;
    private final String viewName;

    private final JLabel headerLabel;
    private final JLabel teamStatusLabel;
    private final JLabel budgetLabel;
    private final JTable playersTable;
    private final JTable benchTable;
    private final DefaultTableModel tableModel;
    private final DefaultTableModel benchTableModel;
    private final JButton backButton;
    private final JPanel contentCardPanel;
    private final CardLayout contentCardLayout;
    private final TeamVisualizationPanel visualizationPanel;
    private ViewMode currentViewMode;

    public TeamDisplayView(TeamViewModel teamViewModel) {
        this(teamViewModel, null);
    }

    public TeamDisplayView(TeamViewModel teamViewModel, StartingLineupViewModel startingLineupViewModel) {
        this.teamViewModel = teamViewModel;
        this.startingLineupViewModel = startingLineupViewModel;
        this.displayConfig = teamViewModel.getDisplayConfig();
        this.viewName = teamViewModel.getViewName();

        teamViewModel.addPropertyChangeListener(this);
        if (startingLineupViewModel != null) {
            startingLineupViewModel.addPropertyChangeListener(this);
        }

        this.headerLabel = new JLabel(displayConfig.getTitle());
        this.teamStatusLabel = new JLabel(displayConfig.getEmptyStateMessage());
        this.budgetLabel = new JLabel("Remaining Budget: N/A");
        this.tableModel = buildReadOnlyTableModel(displayConfig.getColumnHeaders());
        this.playersTable = buildTable(tableModel, 25, Font.BOLD, 11);
        this.benchTableModel = isStartingLineupView()
                ? buildReadOnlyTableModel(displayConfig.getColumnHeaders())
                : null;
        if (benchTableModel == null) {
            this.benchTable = null;
        } else {
            this.benchTable = buildTable(benchTableModel, 23, Font.PLAIN, 11);
        }
        this.backButton = new JButton("Back");
        this.contentCardLayout = new CardLayout();
        this.contentCardPanel = new JPanel(contentCardLayout);
        this.visualizationPanel = new TeamVisualizationPanel();

        buildRootLayout();
        displayTeam(teamViewModel.getTeam());
    }

    private boolean isStartingLineupView() {
        return startingLineupViewModel != null && "starting lineup".equalsIgnoreCase(viewName);
    }

    private DefaultTableModel buildReadOnlyTableModel(String[] headers) {
        return new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable buildTable(DefaultTableModel model, int rowHeight, int fontStyle, int fontSize) {
        JTable table = new JTable(model);
        table.setRowHeight(rowHeight);
        table.setFont(new Font(DEFAULT_FONT_FAMILY, fontStyle, fontSize));
        table.getTableHeader().setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 11));
        return table;
    }

    private void buildRootLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(buildInfoPanel(), BorderLayout.NORTH);
        add(buildContentPanel(), BorderLayout.CENTER);
    }

    private JPanel buildInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerRow.setOpaque(false);
        backButton.setVisible(false);
        headerRow.add(backButton);
        headerLabel.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 16));
        headerRow.add(Box.createRigidArea(new Dimension(10, 0)));
        headerRow.add(headerLabel);

        teamStatusLabel.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 13));
        headerRow.add(Box.createRigidArea(new Dimension(11, 0)));
        headerRow.add(teamStatusLabel);

        budgetLabel.setFont(new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, 13));
        if (displayConfig.shouldShowBudget()) {
            headerRow.add(Box.createRigidArea(new Dimension(11, 0)));
            headerRow.add(budgetLabel);
        }

        if (isStartingLineupView()) {
            headerRow.add(Box.createRigidArea(new Dimension(0, 10)));
            headerRow.add(buildViewTogglePanel());
        }

        infoPanel.add(headerRow);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        return infoPanel;
    }

    private JPanel buildViewTogglePanel() {
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        togglePanel.setBackground(Color.WHITE);

        JButton tableButton = new JButton("Table");
        JButton graphicButton = new JButton("Visualization");

        tableButton.addActionListener(e -> switchView(ViewMode.TABLE));
        graphicButton.addActionListener(e -> switchView(ViewMode.GRAPHIC));

        togglePanel.add(tableButton);
        togglePanel.add(graphicButton);
        return togglePanel;
    }

    private void switchView(ViewMode mode) {
        if (!isStartingLineupView()) {
            return;
        }
        startingLineupViewModel.setViewMode(mode);
        showCard(mode);
    }

    private JPanel buildContentPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);

        JPanel tablePanel = buildTablePanel();

        if (isStartingLineupView()) {
            centerPanel.add(buildCardPanel(tablePanel));
            showCard(startingLineupViewModel.getViewMode());
        } else {
            centerPanel.add(tablePanel);
        }

        return centerPanel;
    }

    private JPanel buildTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel contentStack = new JPanel();
        contentStack.setLayout(new BoxLayout(contentStack, BoxLayout.Y_AXIS));
        contentStack.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Players");
        tableTitle.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 12));
        tableTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentStack.add(tableTitle);

        JScrollPane scrollPane = new JScrollPane(playersTable);
        scrollPane.setPreferredSize(new Dimension(0, 360));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentStack.add(scrollPane);

        if (benchTable != null) {
            JPanel benchContainer = new JPanel();
            benchContainer.setLayout(new BoxLayout(benchContainer, BoxLayout.Y_AXIS));
            benchContainer.setBackground(Color.WHITE);
            benchContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            benchContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
            benchContainer.add(buildBenchPanel());
            contentStack.add(benchContainer);
        }

        tablePanel.add(contentStack, BorderLayout.NORTH);

        return tablePanel;
    }

    private JPanel buildBenchPanel() {
        JPanel benchPanel = new JPanel();
        benchPanel.setLayout(new BoxLayout(benchPanel, BoxLayout.Y_AXIS));
        benchPanel.setOpaque(false);

        JLabel benchTitle = new JLabel("Bench");
        benchTitle.setFont(new Font(DEFAULT_FONT_FAMILY, Font.BOLD, 12));
        benchTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        benchPanel.add(benchTitle);
        benchPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JScrollPane benchScroll = new JScrollPane(benchTable);
        benchScroll.setPreferredSize(new Dimension(0, 160));
        benchScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        benchScroll.getViewport().setBackground(Color.WHITE);
        benchPanel.add(benchScroll);

        return benchPanel;
    }

    private JPanel buildCardPanel(JPanel tablePanel) {
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.add(tablePanel, BorderLayout.CENTER);

        JPanel graphicWrapper = new JPanel(new GridBagLayout());
        graphicWrapper.setBackground(Color.WHITE);

        JPanel fixedSizePanel = new JPanel(new BorderLayout());
        Dimension size = new Dimension(400, 620);
        fixedSizePanel.setPreferredSize(size);
        fixedSizePanel.setMinimumSize(size);
        fixedSizePanel.setMaximumSize(size);
        fixedSizePanel.add(visualizationPanel, BorderLayout.CENTER);

        graphicWrapper.add(fixedSizePanel);

        contentCardPanel.add(tableWrapper, TABLE_CARD);
        contentCardPanel.add(graphicWrapper, GRAPHIC_CARD);

        return contentCardPanel;
    }


    public void setBackAction(String label, Runnable action) {
        String buttonLabel = (label == null || label.isBlank()) ? "Back" : label;
        backButton.setText(buttonLabel);
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

    public void displayTeam(Team team) {
        displayTeam(team, null);
    }

    public void displayTeam(Team team, List<Player> benchPlayers) {
        if (team == null) {
            teamStatusLabel.setText(displayConfig.getEmptyStateMessage());
            if (displayConfig.shouldShowBudget()) {
                budgetLabel.setText("Remaining Budget: N/A");
            }
            tableModel.setRowCount(0);
            if (benchTableModel != null) {
                benchTableModel.setRowCount(0);
            }
            return;
        }

        String statusText;
        if (startingLineupViewModel != null) {
            statusText = team.getFilledSlots() >= 11 ? "Starting Lineup: Complete" : "Starting Lineup: Incomplete";
        } else {
            statusText = team.isConfirmed() ? "Team Status: Confirmed" : "Team Status: Not Confirmed";
        }
        this.teamStatusLabel.setText(statusText);
        this.teamStatusLabel.setForeground(Color.BLACK);

        if (displayConfig.shouldShowBudget()) {
            budgetLabel.setText("Remaining budget: " + team.getBudget());
        }

        tableModel.setRowCount(0);
        for (Player player : team.getPlayers()) {
            tableModel.addRow(createRow(player));
        }

        if (benchTableModel != null) {
            benchTableModel.setRowCount(0);
            if (benchPlayers != null) {
                for (Player player : benchPlayers) {
                    benchTableModel.addRow(createRow(player));
                }
            }
        }

        resizeColumns();
    }

    private Object[] createRow(Player player) {
        String[] headers = displayConfig.getColumnHeaders();
        Object[] row = new Object[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = mapHeaderValue(headers[i], player);
        }
        return row;
    }

    private Object mapHeaderValue(String header, Player player) {
        switch (header.toLowerCase()) {
            case "name":
                return player.getWebName();
            case "position":
                return player.getPosition();
            case "club":
                return player.getTeamName();
            case "price":
                return String.format("£%.1fM", player.getNowCost());
            case "points":
                return player.getPredictedPoints();
            default:
                return "";
        }
    }

    private void resizeColumns() {
        for (int column = 0; column < playersTable.getColumnCount(); column++) {
            int width = 50;
            for (int row = 0; row < playersTable.getRowCount(); row++) {
                Object value = playersTable.getValueAt(row, column);
                Component comp = playersTable.getDefaultRenderer(playersTable.getColumnClass(column))
                        .getTableCellRendererComponent(playersTable, value, false, false, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            playersTable.getColumnModel().getColumn(column).setPreferredWidth(width + 10);
        }
    }

    public int getPlayerCount() {
        return tableModel.getRowCount();
    }

    public boolean hasTeamDisplayed() {
        return tableModel.getRowCount() > 0;
    }

    private void showCard(ViewMode mode) {
        if (!isStartingLineupView()) {
            return;
        }
        if (mode == ViewMode.GRAPHIC) {
            Team team = teamViewModel.getTeam();
            visualizationPanel.setTeam(team);
            visualizationPanel.refresh();
            contentCardLayout.show(contentCardPanel, GRAPHIC_CARD);
        } else {
            contentCardLayout.show(contentCardPanel, TABLE_CARD);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (TeamViewModel.TEAM_STATE_LABEL.equals(property)) {
            Team team = teamViewModel.getTeam();
            if (startingLineupViewModel != null) {
                displayTeam(team, startingLineupViewModel.getBenchPlayers());
            } else {
                displayTeam(team);
            }
        } else if ("startingLineup".equals(property) && startingLineupViewModel != null) {
            Team team = teamViewModel.getTeam();
            displayTeam(team, startingLineupViewModel.getBenchPlayers());
        } else if ("viewMode".equals(property) && startingLineupViewModel != null) {
            showCard(startingLineupViewModel.getViewMode());
        }
    }

    public String getViewName() {
        return viewName;
    }
}
