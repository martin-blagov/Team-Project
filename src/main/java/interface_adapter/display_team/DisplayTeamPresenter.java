package interface_adapter.display_team;

import use_case.display_team.DisplayTeamOutputBoundary;
import use_case.display_team.DisplayTeamOutputData;

/**
 * Presenter for Display Team use case.
 *
 * RESPONSIBILITIES:
 * 1. Receive OutputData from Interactor
 * 2. Update the ViewModel's State
 * 3. Trigger property change notification
 */
public class DisplayTeamPresenter implements DisplayTeamOutputBoundary {

    private final DisplayTeamViewModel viewModel;

    /**
     * Constructor - ViewModel is injected.
     * @param viewModel The ViewModel to update
     */
    public DisplayTeamPresenter(DisplayTeamViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentTeam(DisplayTeamOutputData outputData) {
        // Step 1: Get current state
        DisplayTeamState state = viewModel.getState();

        // Step 2: Update state with team data
        state.setTeam(outputData.getTeam());
        state.setErrorMessage(null);  // Clear any previous error

        // Step 3: Put updated state back in ViewModel
        viewModel.setState(state);

        // Step 4: Fire property change to notify View
        viewModel.firePropertyChange();
    }

    @Override
    public void presentFailure(String errorMessage) {
        // Step 1: Get current state
        DisplayTeamState state = viewModel.getState();

        // Step 2: Set error message
        state.setErrorMessage(errorMessage);
        state.setTeam(null);  // Clear team on error

        // Step 3: Update ViewModel
        viewModel.setState(state);

        // Step 4: Fire property change
        viewModel.firePropertyChange();
    }
}