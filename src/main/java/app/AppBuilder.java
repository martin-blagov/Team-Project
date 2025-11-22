package app;

import data_access.InMemoryPlayerDataAccess;
import entity.Player;
import interface_adapter.ViewManagerModel;
import interface_adapter.display_individual_stat.DisplayIndividualStatController;
import interface_adapter.display_individual_stat.DisplayIndividualStatPresenter;
import interface_adapter.display_individual_stat.DisplayIndividualStatViewModel;
import interface_adapter.home.HomeController;
import interface_adapter.home.HomeViewModel;
import interface_adapter.open_team_entry.OpenTeamEntryController;
import interface_adapter.open_team_entry.OpenTeamEntryPresenter;
import interface_adapter.open_team_entry.OpenTeamEntryViewModel;
import interface_adapter.starting_lineup.StartingLineupController;
import interface_adapter.starting_lineup.StartingLineupPresenter;
import interface_adapter.starting_lineup.StartingLineupViewModel;
import interface_adapter.team_view.TeamViewModel;
import interface_adapter.best_team.BestTeamController;
import interface_adapter.best_team.BestTeamPresenter;
import interface_adapter.best_team.BestTeamViewModel;
import use_case.display_individual_stat.DisplayIndividualStatInputBoundary;
import use_case.display_individual_stat.DisplayIndividualStatInteractor;
import use_case.display_individual_stat.DisplayIndividualStatOutputBoundary;
import use_case.open_team_entry.OpenTeamEntryInputBoundary;
import use_case.open_team_entry.OpenTeamEntryInteractor;
import use_case.starting_lineup.StartingLineupInputBoundary;
import use_case.starting_lineup.StartingLineupInteractor;
import use_case.starting_lineup.StartingLineupOutputBoundary;
import use_case.PlayerDataAccessInterface;
import use_case.best_team.BestTeamInputBoundary;
import use_case.best_team.BestTeamInteractor;
import view.*;

import data_access.BootstrapDataGateway;
import data_access.GameWeekDataGateway;
import data_access.InMemoryPlayerDataAccess;
import data_access.ModelCoefficientDataGateway;
import interface_adapter.initialise_predictions.InitialisePredictionsController;
import interface_adapter.initialise_predictions.InitialisePredictionsPresenter;
import interface_adapter.initialise_predictions.InitialisePredictionsViewModel;
import use_case.initialise_predictions.BootstrapDataAccessInterface;
import use_case.initialise_predictions.GameWeekDataAccessInterface;
import use_case.initialise_predictions.InitialisePredictionsInputBoundary;
import use_case.initialise_predictions.InitialisePredictionsInteractor;
import use_case.initialise_predictions.ModelCoefficientDataAccessInterface;
import view.InitialisePredictionsView;

//TODO REMOVE
import view.TestScrollableListView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AppBuilder {
    private final JPanel cardPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    ViewManager viewManager = new ViewManager(cardPanel, cardLayout, viewManagerModel);

    private HomePageView homePageView;
    private HomeViewModel homeViewModel;
    private TeamEntryView teamEntryView;
    private OpenTeamEntryViewModel teamEntryViewModel;
    private OpenTeamEntryController openTeamEntryController;
    private OpenTeamEntryInputBoundary openTeamEntryInputBoundary;
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
    private final PlayerDataAccessInterface playerDataAccess = new InMemoryPlayerDataAccess();
    private InMemoryPlayerDataAccess playerDataAccess;

    //TODO REMOVE
    private TestScrollableListView testScrollableListView;

    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
    }

    public InMemoryPlayerDataAccess getPlayerDataAccess() {
        return playerDataAccess;
    }

    //TODO REMOVE
    public AppBuilder addTestScrollableListView() {
        testScrollableListView = new TestScrollableListView(playerDataAccess, viewManagerModel);
        cardPanel.add(testScrollableListView, testScrollableListView.getViewName());
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


    public AppBuilder addHomePageView() {
        homeViewModel = new HomeViewModel();
        homePageView = new HomePageView(homeViewModel);
        cardPanel.add(homePageView, homePageView.getViewName());
        return this;
    }

    public AppBuilder addHomeUseCase() {
        final HomeController homeController = new HomeController(
                homeViewModel,
                openTeamEntryController,
                openTeamEntryInputBoundary,
                startingLineupController,
                displayIndividualStatController,
                viewManagerModel
        );
        homePageView.setHomeController(homeController);
        return this;
    }

    public AppBuilder addTeamEntryView() {
        teamEntryViewModel = new OpenTeamEntryViewModel();
        teamEntryView = new TeamEntryView(teamEntryViewModel);
        cardPanel.add(teamEntryView, teamEntryView.getViewName());
        return this;
    }

    public AppBuilder addDisplayIndividualStatsView() {
        displayIndividualStatViewModel = new DisplayIndividualStatViewModel();
        displayIndividualStatsView = new IndividualStatsPageView(displayIndividualStatViewModel);
        cardPanel.add(displayIndividualStatsView, displayIndividualStatsView.getViewName());
        return this;
    }

    public AppBuilder addStartingLineupView() {
        TeamViewModel.DisplayConfig lineupConfig = new TeamViewModel.DisplayConfig(
                "Starting Lineup",
                "No lineup selected.",
                false,
                new String[]{"Name", "Position", "Club"}
        );
        startingLineupViewModel = new TeamViewModel("starting lineup", lineupConfig);
        startingLineupView = new TeamDisplayView(startingLineupViewModel);
        startingLineupViewModelAdapter = new StartingLineupViewModel(startingLineupViewModel);
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

    public AppBuilder addOpenTeamEntryViewUseCase() {
        final OpenTeamEntryPresenter presenter =
                new OpenTeamEntryPresenter(viewManagerModel, teamEntryViewModel, homeViewModel);

        final OpenTeamEntryInteractor interactor =
                new OpenTeamEntryInteractor(presenter);

        openTeamEntryController = new OpenTeamEntryController(interactor);

        teamEntryView.setTeamEntryController(openTeamEntryController);
        return this;
    }

    public AppBuilder addDisplayIndividualStatUseCase() {
        final DisplayIndividualStatOutputBoundary outputBoundary = new DisplayIndividualStatPresenter(
                viewManagerModel,displayIndividualStatViewModel);

        final DisplayIndividualStatInputBoundary interactor = new DisplayIndividualStatInteractor(
                outputBoundary);

        displayIndividualStatController = new DisplayIndividualStatController(
                interactor);
        displayIndividualStatsView.setDisplayIndividualStatController(displayIndividualStatController);

        return this;
    }

    public AppBuilder addStartingLineupUseCase() {
        startingLineupPresenter = new StartingLineupPresenter(viewManagerModel, startingLineupViewModelAdapter);
        StartingLineupOutputBoundary outputBoundary = startingLineupPresenter;
        startingLineupInputBoundary = new StartingLineupInteractor(outputBoundary);
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
