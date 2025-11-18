package app;

import interface_adapter.ViewManagerModel;
import interface_adapter.home.HomeController;
import interface_adapter.home.HomeViewModel;
import interface_adapter.open_team_entry.OpenTeamEntryController;
import interface_adapter.open_team_entry.OpenTeamEntryPresenter;
import interface_adapter.open_team_entry.OpenTeamEntryViewModel;
import interface_adapter.starting_lineup.StartingLineupController;
import interface_adapter.starting_lineup.StartingLineupPresenter;
import interface_adapter.starting_lineup.StartingLineupViewModel;
import interface_adapter.team_view.TeamViewModel;
import use_case.open_team_entry.OpenTeamEntryInputBoundary;
import use_case.open_team_entry.OpenTeamEntryInteractor;
import use_case.starting_lineup.StartingLineupInputBoundary;
import use_case.starting_lineup.StartingLineupInteractor;
import use_case.starting_lineup.StartingLineupOutputBoundary;
import view.HomePageView;
import view.TeamDisplayView;
import view.TeamEntryView;
import view.ViewManager;

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
    private OpenTeamEntryViewModel teamEntryViewModel;
    private OpenTeamEntryController openTeamEntryController;
    private OpenTeamEntryInputBoundary openTeamEntryInputBoundary;
    private TeamViewModel startingLineupViewModel;
    private TeamDisplayView startingLineupView;
    private StartingLineupController startingLineupController;
    private StartingLineupViewModel startingLineupViewModelAdapter;
    private StartingLineupPresenter startingLineupPresenter;
    private StartingLineupInputBoundary startingLineupInputBoundary;

    private InitialisePredictionsView initView;
    private InitialisePredictionsViewModel initViewModel;
    private InitialisePredictionsController initController;
    private InMemoryPlayerDataAccess playerDataAccess;


    public AppBuilder() {
        cardPanel.setLayout(cardLayout);
    }

    public InMemoryPlayerDataAccess getPlayerDataAccess() {
        return playerDataAccess;
    }

    public AppBuilder addInitialisePredictions() {
        // Create shared player data access (will be used by other use cases)
        playerDataAccess = new InMemoryPlayerDataAccess();

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
                startingLineupController
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

    public AppBuilder addOpenTeamEntryViewUseCase() {
        final OpenTeamEntryPresenter presenter =
                new OpenTeamEntryPresenter(viewManagerModel, teamEntryViewModel, homeViewModel);

        final OpenTeamEntryInteractor interactor =
                new OpenTeamEntryInteractor(presenter);

        openTeamEntryController = new OpenTeamEntryController(interactor);

        teamEntryView.setTeamEntryController(openTeamEntryController);
        return this;
    }

    public AppBuilder addStartingLineupUseCase() {
        startingLineupPresenter = new StartingLineupPresenter(viewManagerModel, startingLineupViewModelAdapter);
        StartingLineupOutputBoundary outputBoundary = startingLineupPresenter;
        startingLineupInputBoundary = new StartingLineupInteractor(outputBoundary);
        startingLineupController = new StartingLineupController(startingLineupInputBoundary);
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
