package use_case.best_team;

public class BestTeamRequestModel {
    private final double budget;

    public BestTeamRequestModel(double budget) {
        this.budget = budget;
    }

    public double getBudget() {
        return budget;
    }
}
