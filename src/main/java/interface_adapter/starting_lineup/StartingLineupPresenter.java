package interface_adapter.starting_lineup;

import interface_adapter.ViewManagerModel;
import use_case.starting_lineup.StartingLineupOutputBoundary;
import use_case.starting_lineup.StartingLineupOutputData;

/**
 * Presenter that shows the starting lineup view.
 */
public class StartingLineupPresenter implements StartingLineupOutputBoundary {

    private final ViewManagerModel viewManagerModel;
    private final StartingLineupViewModel viewModel;

    public StartingLineupPresenter(ViewManagerModel viewManagerModel, StartingLineupViewModel viewModel) {
        this.viewManagerModel = viewManagerModel;
        this.viewModel = viewModel;
    }

    @Override
    public void presentLineup(StartingLineupOutputData outputData) {
        viewManagerModel.setState(viewModel.getViewName());
        viewManagerModel.firePropertyChange();
    }
}

