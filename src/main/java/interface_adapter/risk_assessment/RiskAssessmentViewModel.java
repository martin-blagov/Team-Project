package interface_adapter.risk_assessment;

import interface_adapter.ViewModel;
import use_case.risk_assessment.risk.PlayerRisk;

import java.util.List;

public class RiskAssessmentViewModel extends ViewModel<List<PlayerRisk>> {

    public static final String RESULTS_PROPERTY = "risk_results";

    public RiskAssessmentViewModel() {
        super("risk assessment");
    }

    public void setResults(List<PlayerRisk> results) {
        this.setState(results);
        this.firePropertyChange(RESULTS_PROPERTY);
    }
}
