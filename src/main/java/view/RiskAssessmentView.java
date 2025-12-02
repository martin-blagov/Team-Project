package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.risk_assessment.RiskAssessmentViewModel;
import use_case.risk_assessment.risk.PlayerRisk;

import entity.Player;
import entity.Team;
import view.components.TeamVisualizationPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RiskAssessmentView extends JPanel implements PropertyChangeListener {

    private final String viewName = "risk assessment";
    private final RiskAssessmentViewModel viewModel;
    private ViewManagerModel viewManagerModel;
    private JTable table;
    private JButton backButton;
    private JPanel tablePanel;
    // New: Tabbed layout
    private JTabbedPane tabbedPane;

    // New: Pitch-based UI elements
    private TeamVisualizationPanel pitchPanel;
    private JTextArea riskDetailsArea;

    // Store latest results so click listener can access
    private List<PlayerRisk> currentResults;


    public RiskAssessmentView(RiskAssessmentViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        viewModel.addPropertyChangeListener(this);

        this.viewModel.addPropertyChangeListener(this);

        // Build UI
        this.tablePanel = buildTablePanel();
        JPanel pitchTab = buildPitchTab();
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());

        // Back button on the RIGHT
        backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });
        topBar.add(backButton, BorderLayout.WEST);


        JLabel title = new JLabel("Risk Assessment Results");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Switch buttons
        JPanel switchPanel = new JPanel();
        JButton tableBtn = new JButton("Table");
        JButton pitchBtn = new JButton("Pitch");
        switchPanel.add(tableBtn);
        switchPanel.add(pitchBtn);

        // Combine top bar + switch panel
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.add(topBar, BorderLayout.NORTH);
        topArea.add(title, BorderLayout.CENTER);
        topArea.add(switchPanel, BorderLayout.SOUTH);


        // ---------------- CONTENT PANEL ----------------
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.add(tablePanel, "TABLE");
        contentPanel.add(pitchTab, "PITCH");

        CardLayout layout = (CardLayout) contentPanel.getLayout();
        tableBtn.addActionListener(e -> layout.show(contentPanel, "TABLE"));
        pitchBtn.addActionListener(e -> layout.show(contentPanel, "PITCH"));

        // ---------------- FINAL LAYOUT ----------------
        setLayout(new BorderLayout());
        add(topArea, BorderLayout.NORTH);   // top = back + table/pitch buttons
        add(contentPanel, BorderLayout.CENTER); // content fills the screen
    }

    public void setViewManagerModel(ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;
        backButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });
    }

    //Table View
        private JPanel buildTablePanel() {
        JPanel root = new JPanel(new BorderLayout());

        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        root.add(scrollPane, BorderLayout.CENTER);

        return root;
    }

    //Pitch View
    private JPanel buildPitchTab() {
        JPanel root = new JPanel(new BorderLayout());

        // Center: pitch visualization
        pitchPanel = new TeamVisualizationPanel();
        pitchPanel.setDimensions(800, 600);
        pitchPanel.setShowBudgetBox(false);

        // Click listener: show risk details
        pitchPanel.setPlayerClickListener(player -> {
            if (player == null || currentResults == null) {
                riskDetailsArea.setText("No player selected.");
                return;
            }

            PlayerRisk match = null;
            for (PlayerRisk pr : currentResults) {
                Player p = pr.getPlayer();
                if (p != null && p.getId() == player.getId()) {
                    match = pr;
                    break;
                }
            }

            if (match == null) {
                riskDetailsArea.setText("No risk data found for this player.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(match.getPlayer().getWebName())
                    .append(" (")
                    .append(match.getPlayer().getTeamName())
                    .append(")\n")
                    .append("Risk Count: ")
                    .append(match.getRiskCount())
                    .append("\n\n");

            sb.append("Risks:\n");
            sb.append(match.getTriggeredRiskNames()).append("\n");

            riskDetailsArea.setText(sb.toString());
        });

        JPanel pitchContainer = new JPanel(new BorderLayout());
        pitchContainer.add(pitchPanel, BorderLayout.CENTER);

        // Right side: instructions + risk details
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));

        JTextArea instructions = new JTextArea(
                "Click on a highlighted player to see why they are flagged as risky.\n\n" +
                        "Greyed-out kits are empty or non-risky slots.\n\n" +
                        "The pitch view shows only the ordered risky players, placed left-to-right."
        );
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);

        riskDetailsArea = new JTextArea("Select a risky player on the pitch.");
        riskDetailsArea.setEditable(false);
        riskDetailsArea.setLineWrap(true);
        riskDetailsArea.setWrapStyleWord(true);

        rightPanel.add(new JScrollPane(instructions), BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(riskDetailsArea), BorderLayout.CENTER);

        root.add(pitchContainer, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.EAST);

        return root;
    }

    public String getViewName() {
        return viewName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RiskAssessmentViewModel.RESULTS_PROPERTY)) {
            List<PlayerRisk> results = viewModel.getState();
            currentResults = results;
            updateTable(results);
            updatePitch(results);
        }
    }
    private void updateTable(List<PlayerRisk> risks) {
        if (risks == null || risks.isEmpty()) {
            table.setModel(new javax.swing.table.DefaultTableModel(
                    new Object[][]{},
                    new String[]{"No data"}
            ));
            return;
        }

        String[] columns = {"Player", "Position", "Club", "Risk Count", "Risks"};
        Object[][] data = new Object[risks.size()][5];

        for (int i = 0; i < risks.size(); i++) {
            PlayerRisk r = risks.get(i);

            data[i][0] = r.getPlayer().getWebName();
            data[i][1] = r.getPlayer().getPosition();
            data[i][2] = r.getPlayer().getTeamName();
            data[i][3] = r.getRiskCount();
            data[i][4] = String.join(", ", r.getTriggeredRiskNames());
        }

        table.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    private void updatePitch(List<PlayerRisk> risks) {
        if (pitchPanel == null) return;

        List<Player> slots = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            slots.add(null);
        }

        if (risks != null) {
            int idx = 0;
            for (PlayerRisk pr : risks) {
                if (idx >= 15) break;
                slots.set(idx, pr.getPlayer());
                idx++;
            }
        }

        Team dummyTeam = new Team(slots, 0.0f, false);
        pitchPanel.setTeam(dummyTeam);
        pitchPanel.refresh();

        riskDetailsArea.setText("Select a risky player on the pitch.");
    }

}

