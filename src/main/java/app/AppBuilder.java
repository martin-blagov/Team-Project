package app;

import data_access.InMemoryPlayerDataAccess;
import data_access.*;
import interface_adapter.ViewManagerModel;
import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.display_individual_stat.DisplayIndividualStatPresenter;
import interface_adapter.display_individual_stat.DisplayIndividualStatViewModel;
import interface_adapter.home.HomeController;
import interface_adapter.home.HomeViewModel;
import interface_adapter.team_entry.TeamEntryController;
import interface_adapter.team_entry.TeamEntryPresenter;
import interface_adapter.team_entry.TeamEntryViewModel;
import interface_adapter.starting_lineup.StartingLineupController;
import interface_adapter.starting_lineup.StartingLineupPresenter;
import interface_adapter.starting_lineup.StartingLineupViewModel;
import interface_adapter.team_view.TeamViewModel;
import interface_adapter.best_team.BestTeamController;
import interface_adapter.best_team.BestTeamPresenter;
import interface_adapter.best_team.BestTeamViewModel;
import use_case.starting_lineup.StartingLineupTeamDataAccessInterface;
import interface_adapter.transfer_suggestions.TransferSuggestionsController;
import interface_adapter.transfer_suggestions.TransferSuggestionsPresenter;
import interface_adapter.transfer_suggestions.TransferSuggestionsViewModel;
import use_case.team_entry.TeamDataAccessInterface;
import use_case.display_individual_stat.DisplayIndividualStatInputBoundary;
import use_case.display_individual_stat.DisplayIndividualStatInteractor;
import use_case.display_individual_stat.DisplayIndividualStatOutputBoundary;
import use_case.team_entry.TeamEntryInputBoundary;
import use_case.team_entry.TeamEntryInteractor;
import use_case.starting_lineup.StartingLineupInputBoundary;
import use_case.starting_lineup.StartingLineupInteractor;
import use_case.starting_lineup.StartingLineupOutputBoundary;
import use_case.best_team.BestTeamInteractor;
import use_case.transfer_suggestions.TransferSuggestionsInteractor;
import view.*;

import interface_adapter.initialise_predictions.InitialisePredictionsController;
import interface_adapter.initialise_predictions.InitialisePredictionsPresenter;
import interface_adapter.initialise_predictions.InitialisePredictionsViewModel;
import use_case.initialise_predictions.BootstrapDataAccessInterface;
import use_case.initialise_predictions.GameWeekDataAccessInterface;
import use_case.initialise_predictions.InitialisePredictionsInputBoundary;
import use_case.initialise_predictions.InitialisePredictionsInteractor;
import use_case.initialise_predictions.ModelCoefficientDataAccessInterface;
import view.InitialisePredictionsView;

import use_case.display_team.DisplayTeamDataAccessInterface;
import use_case.transfer_suggestions.TransferSuggestionsTeamDataAccessInterface;

//TODO REMOVE
import interface_adapter.test_display_players.TestDisplayPlayersController;
import interface_adapter.test_display_players.TestDisplayPlayersPresenter;
import interface_adapter.test_display_players.TestDisplayPlayersViewModel;
import use_case.test_display_players.TestDisplayPlayersInteractor;

import view.TestScrollableListViewV2;

import interface_adapter.display_team. DisplayTeamController;
import interface_adapter.display_team.DisplayTeamPresenter;
import interface_adapter.display_team.DisplayTeamViewModel;
import use_case.display_team.DisplayTeamInputBoundary;
import use_case.display_team.DisplayTeamInteractor;
import view.TestTeamVisualizationView;



import javax.swing.*;
import java.awt.*;

public class AppBuilder {
    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    private HomePageView homePageView;
    private HomeViewModel homeViewModel;
    private TeamEntryView teamEntryView;
    private TeamEntryViewModel teamEntryViewModel;
    private TeamEntryController teamEntryController;
    private TeamEntryInputBoundary teamEntryInputBoundary;
    private TeamViewModel startingLineupViewModel;
    private TeamDisplayView startingLineupView;
    private IndividualStatsPageView displayIndividualStatsView;
    private DisplayIndividualStatViewModel displayIndividualStatViewModel;
    private DisplayIndividualStatController displayIndividualStatController;
    private StartingLineupController startingLineupController;
    private StartingLineupViewModel startingLineupViewModelAdapter;
    private StartingLineupPresenter startingLineupPresenter;
    private StartingLineupInputBoundary startingLineupInputBoundary;
    private BestTeamViewModel bestTeamViewModel;
    private BestTeamView bestTeamView;
    private BestTeamController bestTeamController;

    private InitialisePredictionsView initView;
    private InitialisePredictionsViewModel initViewModel;
    private InitialisePredictionsController initController;
    private InMemoryPlayerDataAccess playerDataAccess = new InMemoryPlayerDataAccess();

    // With these lines:
    private final FileTeamDataAccessObject fileTeamDAO = new FileTeamDataAccessObject("team.json");
    private final TeamDataAccessInterface teamDataAccess = fileTeamDAO;  // Team Entry (can't change name)
    private final DisplayTeamDataAccessInterface displayTeamDataAccess = fileTeamDAO;  // Display Team
    private final TransferSuggestionsTeamDataAccessInterface transferSuggestionsTeamDataAccess = fileTeamDAO;  // Transfer Suggestions
    private final StartingLineupTeamDataAccessInterface startingLineupTeamDataAccess = fileTeamDataAccess;

    // todo remove
    private final use_case.TeamDataAccessInterface useCaseTeamDataAccess = fileTeamDAO;

    // Transfer Suggestions components
    private TransferSuggestionsView transferSuggestionsView;
    private TransferSuggestionsViewModel transferSuggestionsViewModel;
    private TransferSuggestionsController transferSuggestionsController;


    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
    }

    public InMemoryPlayerDataAccess getPlayerDataAccess() {
        return playerDataAccess;
    }

    // TODO remove

    private TestScrollableListViewV2 testScrollableListViewV2;
    private TestDisplayPlayersViewModel testDisplayPlayersViewModel;
    private TestDisplayPlayersController testDisplayPlayersController;

    // Add with the other view/controller declarations
    private TestTeamVisualizationView testTeamVisualizationView;
    private DisplayTeamViewModel displayTeamViewModel;
    private DisplayTeamController displayTeamController;

    // TODO REMOVE
    public AppBuilder addTestDisplayPlayersUseCase() {
        // 1. ViewModel
        testDisplayPlayersViewModel = new TestDisplayPlayersViewModel();

        // 2. View
        testScrollableListViewV2 = new TestScrollableListViewV2(
                testDisplayPlayersViewModel,
                viewManagerModel
        );
        cardPanel.add(testScrollableListViewV2, testScrollableListViewV2.getViewName());

        // 3. Presenter
        TestDisplayPlayersPresenter presenter =
                new TestDisplayPlayersPresenter(testDisplayPlayersViewModel);

        // 4. Interactor
        TestDisplayPlayersInteractor interactor =
                new TestDisplayPlayersInteractor(
                        playerDataAccess,  // Already exists
                        presenter
                );

        // 5. Controller
        testDisplayPlayersController =
                new TestDisplayPlayersController(interactor);

        // 6. Wire Controller to View
        testScrollableListViewV2.setController(testDisplayPlayersController);

        return this;
    }

    // TODO REMOVE
    public AppBuilder addTestTeamVisualizationUseCase() {
        // 1. ViewModel
        displayTeamViewModel = new DisplayTeamViewModel();

        // 2.  View
        testTeamVisualizationView = new TestTeamVisualizationView(
                displayTeamViewModel,
                viewManagerModel
        );
        cardPanel.add(testTeamVisualizationView, testTeamVisualizationView.getViewName());

        // 3.  Presenter
        DisplayTeamPresenter presenter = new DisplayTeamPresenter(displayTeamViewModel);

        // 4.  Interactor (uses shared teamDataAccess)
        DisplayTeamInputBoundary interactor = new DisplayTeamInteractor(
                displayTeamDataAccess,  // Already exists - shared with other use cases
                presenter
        );

        // 5. Controller
        displayTeamController = new DisplayTeamController(interactor);

        // 6. Wire Controller to View
        testTeamVisualizationView.setController(displayTeamController);

        return this;
    }

    public AppBuilder addInitialisePredictions() {
        // Create shared player data access (will be used by other use cases)

        // Create ViewModel
        initViewModel = new InitialisePredictionsViewModel();

        // Create View (loading screen)
        initView = new InitialisePredictionsView(initViewModel);
        cardPanel.add(initView, initView.getViewName());

        // Create Presenter (needs ViewManagerModel to switch views)
        InitialisePredictionsPresenter presenter =
                new InitialisePredictionsPresenter(
                        initViewModel,
                        viewManagerModel,
                        "home"  // Name of the home view to switch to when done
                );

        // Create Data Access objects
        BootstrapDataAccessInterface bootstrapAccess = new BootstrapDataGateway();
        GameWeekDataAccessInterface gameweekAccess = new GameWeekDataGateway();
        ModelCoefficientDataAccessInterface coefficientAccess =
                new ModelCoefficientDataGateway();

        // Create Interactor
        InitialisePredictionsInputBoundary interactor =
                new InitialisePredictionsInteractor(
                        bootstrapAccess,
                        gameweekAccess,
                        coefficientAccess,
                        presenter,
                        playerDataAccess
                );

        // Create Controller
        initController = new InitialisePredictionsController(interactor);

        // Inject controller into view (view will call it automatically)
        initView.setInitialisePredictionsController(initController);

        return this;
    }

    /**
     * Creates and registers the Home page view with the application.
     *
     * @return this builder instance
     */
    public AppBuilder addHomePageView() {
        homeViewModel = new HomeViewModel();
        homePageView = new HomePageView(homeViewModel);
        cardPanel.add(homePageView, homePageView.getViewName());
        return this;
    }

    /**
     * Sets up the Home use case and connects its controller to the Home view.
     *
     * @return this builder instance
     */
    public AppBuilder addHomeUseCase() {
        final HomeController homeController = new HomeController(
                homeViewModel,
                teamEntryController,
                teamEntryInputBoundary,
                startingLineupController,
                displayIndividualStatController,
                transferSuggestionsController,
                viewManagerModel
        );
        homePageView.setHomeController(homeController);
        return this;
    }

    /**
     * Creates and registers the Team Entry view with the application.
     *
     * @return this builder instance
     */
    public AppBuilder addTeamEntryView() {
        teamEntryViewModel = new TeamEntryViewModel();
        teamEntryView = new TeamEntryView(teamEntryViewModel, testDisplayPlayersViewModel);
        cardPanel.add(teamEntryView, teamEntryView.getViewName());
        return this;
    }

    public AppBuilder addDisplayIndividualStatsView() {
        displayIndividualStatViewModel = new DisplayIndividualStatViewModel();
        displayIndividualStatsView = new IndividualStatsPageView(displayIndividualStatViewModel, testDisplayPlayersViewModel, playerDataAccess, viewManagerModel);
        cardPanel.add(displayIndividualStatsView, displayIndividualStatsView.getViewName());
        return this;
    }

    public AppBuilder addStartingLineupView() {
        TeamViewModel.DisplayConfig lineupConfig = new TeamViewModel.DisplayConfig(
                "Starting Lineup",
                "No valid lineup available.",
                false,
                new String[]{"Name", "Position", "Club", "Price", "Points"}
        );
        startingLineupViewModel = new TeamViewModel("starting lineup", lineupConfig);
        startingLineupViewModelAdapter = new StartingLineupViewModel(startingLineupViewModel);
        startingLineupView = new TeamDisplayView(startingLineupViewModel, startingLineupViewModelAdapter);
        cardPanel.add(startingLineupView, startingLineupView.getViewName());
        startingLineupView.setBackAction("Back", () -> {
            if (homePageView != null) {
                viewManagerModel.setState(homePageView.getViewName());
                viewManagerModel.firePropertyChange();
            }
        });
        return this;
    }

    public AppBuilder addBestTeamView() {
        bestTeamViewModel = new BestTeamViewModel();
        bestTeamView = new BestTeamView(bestTeamViewModel);
        cardPanel.add(bestTeamView, bestTeamView.getViewTitle());

        bestTeamView.setBackAction("Back", () -> {
            if (homePageView != null) {
                viewManagerModel.setState(homePageView.getViewName());
                viewManagerModel.firePropertyChange();
            }
        });
        return this;
    }

    /**
     * Sets up the Team Entry use case and connects its controller to the view.
     *
     * @return this builder instance
     */

    public AppBuilder addTeamEntryViewUseCase() {
        final TeamEntryPresenter presenter =
                new TeamEntryPresenter(viewManagerModel, teamEntryViewModel, homeViewModel);

        final TeamEntryInteractor interactor =
                new TeamEntryInteractor(presenter, teamDataAccess);

        teamEntryController = new TeamEntryController(interactor, teamEntryViewModel);

        teamEntryView.setTeamEntryController(teamEntryController);
        teamEntryView.setPlayerListController(testDisplayPlayersController);

        return this;
    }

    public AppBuilder addDisplayIndividualStatUseCase() {
        final DisplayIndividualStatOutputBoundary outputBoundary = new DisplayIndividualStatPresenter(
                viewManagerModel, displayIndividualStatViewModel);

        final DisplayIndividualStatInputBoundary interactor = new DisplayIndividualStatInteractor(
                outputBoundary, playerDataAccess);

        displayIndividualStatController = new DisplayIndividualStatController(
                interactor);
        displayIndividualStatsView.setDisplayIndividualStatController(displayIndividualStatController);
        displayIndividualStatsView.setPlayerListController(testDisplayPlayersController);
        return this;
    }

    public AppBuilder addStartingLineupUseCase() {
        startingLineupPresenter = new StartingLineupPresenter(viewManagerModel, startingLineupViewModelAdapter);
        StartingLineupOutputBoundary outputBoundary = startingLineupPresenter;

        startingLineupInputBoundary = new StartingLineupInteractor(outputBoundary, startingLineupTeamDataAccess, playerDataAccess);
        startingLineupController = new StartingLineupController(startingLineupInputBoundary);
        return this;
    }

    public AppBuilder addBestTeamUseCase() {
        // pushes data into BestTeamViewModel and changes view
        BestTeamPresenter presenter = new BestTeamPresenter(bestTeamViewModel, viewManagerModel);
        // uses shared playerDataAccess (InMemoryPlayerDataAccess)
        BestTeamInteractor interactor = new BestTeamInteractor(playerDataAccess, presenter);
        // called from HomePageView when "Best Team" is clicked
        bestTeamController = new BestTeamController(interactor);
        // connect controller to home page
        homePageView.setBestTeamController(bestTeamController);
        return this;
    }

    public AppBuilder addTransferSuggestionsView() {
        transferSuggestionsViewModel = new TransferSuggestionsViewModel();
        transferSuggestionsView = new TransferSuggestionsView(
                transferSuggestionsViewModel,
                viewManagerModel
        );
        cardPanel.add(transferSuggestionsView, transferSuggestionsView.getViewName());
        return this;
    }

    public AppBuilder addTransferSuggestionsUseCase() {
        // Create Presenter
        final TransferSuggestionsPresenter presenter = new TransferSuggestionsPresenter(
                transferSuggestionsViewModel,
                viewManagerModel,
                homeViewModel
        );

        // Create Interactor
        final TransferSuggestionsInteractor interactor = new TransferSuggestionsInteractor(
                transferSuggestionsTeamDataAccess,      // Need to add this field - see below
                playerDataAccess,    // Already exists
                presenter
        );

        // Create Controller
        transferSuggestionsController = new TransferSuggestionsController(
                interactor,
                transferSuggestionsViewModel
        );

        // Inject controller into view
        transferSuggestionsView.setController(transferSuggestionsController);

        return this;
    }

    public JFrame build() {
        final JFrame application = new JFrame("Premier League Fantasy App");
        application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        application.add(cardPanel);

        // Set initial view to initialization screen
        viewManagerModel.setState(initView.getViewName());
        viewManagerModel.firePropertyChange();

//        viewManagerModel.setState(homePageView.getViewName());
//        viewManagerModel.firePropertyChange();


        return application;
    }
}
