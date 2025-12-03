package interface_adapter.home;

import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.risk_assessment.RiskAssessmentController;
import interface_adapter.team_entry.TeamEntryController;
import interface_adapter.starting_lineup.StartingLineupController;
import interface_adapter.transfer_suggestions.TransferSuggestionsController;
import use_case.team_entry.TeamEntryInputBoundary;

import interface_adapter.ViewManagerModel;

/**
 * Controller for the Signup Use Case.
 */
public class HomeController {

    private final HomeViewModel homeViewModel;
    private final TeamEntryController teamEntryController;
    private final TeamEntryInputBoundary teamEntryInputBoundary;
    private final StartingLineupController startingLineupController;
    private final DisplayIndividualStatController displayIndividualStatController;
    private final RiskAssessmentController riskAssessmentController;
    private final TransferSuggestionsController transferSuggestionsController;

    private final ViewManagerModel viewManagerModel;

    public HomeController(HomeViewModel homeViewModel, TeamEntryController teamEntryController,
                          TeamEntryInputBoundary teamEntryInputBoundary,
                          StartingLineupController startingLineupController,
                          DisplayIndividualStatController displayIndividualStatController,
                          RiskAssessmentController riskAssessmentController,
                          TransferSuggestionsController transferSuggestionsController,
                          ViewManagerModel viewManagerModel) {
        this.homeViewModel = homeViewModel;
        this.teamEntryInputBoundary = teamEntryInputBoundary;
        this.teamEntryController = teamEntryController;
        this.startingLineupController = startingLineupController;
        this.displayIndividualStatController = displayIndividualStatController;
        this.riskAssessmentController = riskAssessmentController;
        this.transferSuggestionsController = transferSuggestionsController;

        this.viewManagerModel = viewManagerModel;
    }

    // Placeholder print statements to confirm buttons work until we can implement the actual pages
    public void openBestTeamPage() {
        System.out.println("Navigating to Best Team Page...");
    }

    public void openTeamInputPage() {
        teamEntryController.openPage();
    }

    public void openRiskAssessmentPage() {
        riskAssessmentController.execute();
    }


    public void openBestPlayersPage() {
        System.out.println("Navigating to Best Players Page...");
    }

    public void openTransferPage() {
        System.out.println("Navigating to Transfer Page...");
    }

    public void openStatsPage() {
        viewManagerModel.setState("display individual stats");
        viewManagerModel.firePropertyChange();
    }

    public void openLineupPage() {
            startingLineupController.execute();
    }

    public void openTransferSuggestionsPage() {transferSuggestionsController.openPage();}

}
