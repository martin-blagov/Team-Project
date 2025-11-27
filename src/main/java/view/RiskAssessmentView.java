package view;

import interface_adapter.risk_assessment.RiskAssessmentViewModel;
import use_case.risk_assessment.risk.PlayerRisk;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class RiskAssessmentView extends JPanel implements PropertyChangeListener {

    private final RiskAssessmentViewModel viewModel;
    private JTable table;

    public RiskAssessmentView(RiskAssessmentViewModel viewModel) {
        this.viewModel = viewModel;
        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Risk Assessment", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RiskAssessmentViewModel.RESULTS_PROPERTY)) {
            List<PlayerRisk> results = viewModel.getState();
            updateTable(results);
        }
    }

    private void updateTable(List<PlayerRisk> risks) {
        String[] columns = {"Player", "Total Risks", "Details"};
        String[][] data = new String[risks.size()][3];

        for (int i = 0; i < risks.size(); i++) {
            PlayerRisk pr = risks.get(i);

            data[i][0] = pr.getPlayer().getWebName();
            data[i][1] = String.valueOf(pr.getRiskCount());
            data[i][2] = pr.getTriggeredRiskNames(); // implement to join names
        }

        table.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
}
