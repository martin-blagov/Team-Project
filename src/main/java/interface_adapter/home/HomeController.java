package interface_adapter.home;

import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.open_team_entry.OpenTeamEntryController;
import interface_adapter.starting_lineup.StartingLineupController;
import use_case.open_team_entry.OpenTeamEntryInputBoundary;

/**
 * Controller for the Signup Use Case.
 */
public class HomeController {

    private final HomeViewModel homeViewModel;
    private final OpenTeamEntryController openTeamEntryController;
    private final OpenTeamEntryInputBoundary openTeamEntryInputBoundary;
    private final StartingLineupController startingLineupController;
    private final DisplayIndividualStatController displayIndividualStatController;

    public HomeController(HomeViewModel homeViewModel,
                          OpenTeamEntryController openTeamEntryController,
                          OpenTeamEntryInputBoundary openTeamEntryInputBoundary,
                          StartingLineupController startingLineupController, DisplayIndividualStatController displayIndividualStatController) {
        this.homeViewModel = homeViewModel;
        this.openTeamEntryInputBoundary = openTeamEntryInputBoundary;
        this.openTeamEntryController = openTeamEntryController;
        this.startingLineupController = startingLineupController;
        this.displayIndividualStatController = displayIndividualStatController;
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
}
