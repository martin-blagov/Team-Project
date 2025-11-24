package interface_adapter.test_display_players;

import use_case.test_display_players.TestDisplayPlayersOutputBoundary;
import use_case.test_display_players.TestDisplayPlayersOutputData;
import entity.Player;
import java.util.List;

/**
 * DEMO Presenter for Test Display Players use case.
 *
 * RESPONSIBILITIES:
 * 1. Receive OutputData from the Interactor
 * 2. Update the ViewModel with the data
 * 3. Trigger property change notification
 */
public class TestDisplayPlayersPresenter implements TestDisplayPlayersOutputBoundary {

    private final TestDisplayPlayersViewModel viewModel;

    /**
     * Constructor - ViewModel is injected.
     *
     * @param viewModel The ViewModel to update
     */
    public TestDisplayPlayersPresenter(TestDisplayPlayersViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Present the filtered players to the user.
     * @param outputData Contains filtered players and available teams
     */
    @Override
    public void presentPlayers(TestDisplayPlayersOutputData outputData) {
        // Step 1: Get the current state (or create new one)
        TestDisplayPlayersState state = viewModel.getState();

        // Step 2: Update the state with new data
        state.setPlayers(outputData.getFilteredPlayers());
        state.setAvailableTeams(outputData.getAvailableTeams());
        state.setErrorMessage(null);  // Clear any previous error

        // Step 3: Put the updated state back in ViewModel
        viewModel.setState(state);

        // Step 4: Fire property change to notify the View
        viewModel.firePropertyChange();
    }

    /**
     * Present a failure message to the user.
     *
     * Called if something goes wrong during data fetching/filtering.
     *
     * @param errorMessage Description of what went wrong
     */
    @Override
    public void presentFailure(String errorMessage) {
        // Step 1: Get the current state
        TestDisplayPlayersState state = viewModel.getState();

        // Step 2: Set the error message
        state.setErrorMessage(errorMessage);
        state.setPlayers(null);  // Clear players on error

        // Step 3: Update ViewModel
        viewModel.setState(state);

        // Step 4: Fire property change
        viewModel.firePropertyChange();
    }
}