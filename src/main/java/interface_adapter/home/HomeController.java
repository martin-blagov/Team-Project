package interface_adapter.home;

import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.open_team_entry.OpenTeamEntryController;
import interface_adapter.starting_lineup.StartingLineupController;
import use_case.open_team_entry.OpenTeamEntryInputBoundary;

//TODO Check whether to remove:
import interface_adapter.ViewManagerModel;

/**
 * Controller for the Signup Use Case.
 */
public class HomeController {

    private final HomeViewModel homeViewModel;
    private final OpenTeamEntryController openTeamEntryController;
    private final OpenTeamEntryInputBoundary openTeamEntryInputBoundary;
    private final StartingLineupController startingLineupController;
    private final DisplayIndividualStatController displayIndividualStatController;

    //TODO Check whether to remove:
    private final ViewManagerModel viewManagerModel;

    public HomeController(HomeViewModel homeViewModel, OpenTeamEntryController openTeamEntryController,
                          OpenTeamEntryInputBoundary openTeamEntryInputBoundary,
                          StartingLineupController startingLineupController, DisplayIndividualStatController displayIndividualStatController,
                          ViewManagerModel viewManagerModel) {
        this.homeViewModel = homeViewModel;
        this.openTeamEntryInputBoundary = openTeamEntryInputBoundary;
        this.openTeamEntryController = openTeamEntryController;
        this.startingLineupController = startingLineupController;
        this.displayIndividualStatController = displayIndividualStatController;
        
        //TODO Check whether to remove:
        this.viewManagerModel = viewManagerModel;
    }

    // Placeholder print statements to confirm buttons work until we can implement the actual pages
    public void openBestTeamPage() {
        System.out.println("Navigating to Best Team Page...");
    }

    public void openTeamInputPage() {
        openTeamEntryController.execute();
    }

    public void openReplacementPage() {
        System.out.println("Navigating to Replacement Suggestions Page...");
    }

    public void openBestPlayersPage() {
        System.out.println("Navigating to Best Players Page...");
    }

    public void openTransferPage() {
        System.out.println("Navigating to Transfer Page...");
    }

    public void openStatsPage() {displayIndividualStatController.execute();}

    public void openLineupPage() {
        if (startingLineupController != null) {
            startingLineupController.execute();
        } else {
            System.out.println("Navigating to Starting Lineup Page...");
        }
    }

    public void openTestScrollableListPage() {
        viewManagerModel.setState("test scrollable list");
        viewManagerModel.firePropertyChange();
    }
}
