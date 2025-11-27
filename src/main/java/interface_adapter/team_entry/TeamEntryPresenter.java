package interface_adapter.team_entry;
import interface_adapter.ViewManagerModel;
import interface_adapter.home.HomeViewModel;
import use_case.team_entry.TeamEntryOutputBoundary;
import use_case.team_entry.TeamEntryOutputData;

public class TeamEntryPresenter implements TeamEntryOutputBoundary {

    private final ViewManagerModel viewManagerModel;
    private final TeamEntryViewModel teamEntryViewModel;
    private final HomeViewModel homePageViewModel;


    public TeamEntryPresenter(ViewManagerModel viewManagerModel,
                              TeamEntryViewModel teamEntryViewModel,
                              HomeViewModel homePageViewModel) {
        this.viewManagerModel = viewManagerModel;
        this.teamEntryViewModel = teamEntryViewModel;
        this.homePageViewModel = homePageViewModel;
    }

    @Override
    public void prepareOpenPageView() {
        TeamEntryState state = teamEntryViewModel.getState();
        state.setErrorMessage(null);
        state.setSuccessMessage(null);

        viewManagerModel.setState("team entry");
        viewManagerModel.firePropertyChange();
    }

    @Override
    public void prepareSavedTeamView(String[] names, int[] ids) {
        TeamEntryState state = teamEntryViewModel.getState();

        state.setPlayers(names);
        state.setPlayerIds(ids);

        teamEntryViewModel.setState(state);
        teamEntryViewModel.firePropertyChange();

        // switch to team entry view
        viewManagerModel.setState(teamEntryViewModel.getViewName());
        viewManagerModel.firePropertyChange();
    }


    @Override
    public void prepareSuccessView(TeamEntryOutputData outputData) {
        TeamEntryState state = teamEntryViewModel.getState();
        state.setSuccessMessage("Your team was successfully submitted! You may now return to the menu.");
        state.setErrorMessage(null);
        teamEntryViewModel.setState(state);
        teamEntryViewModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        TeamEntryState state = teamEntryViewModel.getState();
        state.setErrorMessage(errorMessage);
        state.setSuccessMessage(null);
        teamEntryViewModel.setState(state);
        teamEntryViewModel.firePropertyChange();
    }

    @Override
    public void switchToHomePage() {
        viewManagerModel.setState(homePageViewModel.getViewName());
        viewManagerModel.firePropertyChange();
    }
}

