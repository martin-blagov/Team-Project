package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.risk_assessment.RiskAssessmentViewModel;
import use_case.risk_assessment.risk.PlayerRisk;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class RiskAssessmentView extends JPanel implements PropertyChangeListener {

    private final String viewName = "risk assessment";
    private final RiskAssessmentViewModel viewModel;
    private ViewManagerModel viewManagerModel;
    private JTable table;
    private JButton backButton;

    public RiskAssessmentView(RiskAssessmentViewModel viewModel) {
        this.viewModel = viewModel;
        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Color.white);

        //Title
        JLabel title = new JLabel("Risk Assessment", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        //Table
        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        //Back Button
        backButton = new JButton("Back");

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(backButton);

        add(southPanel, BorderLayout.SOUTH);

    }

    public void setViewManagerModel(ViewManagerModel viewManagerModel) {
        this.viewManagerModel = viewManagerModel;
        backButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });
    }

    public String getViewName() {
        return viewName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RiskAssessmentViewModel.RESULTS_PROPERTY)) {
            List<PlayerRisk> results = viewModel.getState();
            updateTable(results);
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
}

