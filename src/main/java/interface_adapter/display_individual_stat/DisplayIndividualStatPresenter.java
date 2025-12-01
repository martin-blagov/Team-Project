package interface_adapter.display_individual_stat;

import use_case.display_individual_stat.DisplayIndividualStatOutputBoundary;
import use_case.display_individual_stat.DisplayIndividualStatOutputData;
import interface_adapter.ViewManagerModel;

/**
 * The Presenter for the Display Individual Stats Use Case.
 */
public class DisplayIndividualStatPresenter implements DisplayIndividualStatOutputBoundary {

    private final DisplayIndividualStatViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    public DisplayIndividualStatPresenter(ViewManagerModel viewManagerModel,
                                          DisplayIndividualStatViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
    }

    @Override
    public void presentView(DisplayIndividualStatOutputData outputData) {
        final DisplayIndividualStatState playerDisplayState = viewModel.getState();
        playerDisplayState.setPlayerName(outputData.getPlayerName());
        playerDisplayState.setPlayerPosition(outputData.getPlayerPosition());
        playerDisplayState.setPlayerTeam(outputData.getPlayerTeam());
        playerDisplayState.setPlayerCost(outputData.getPlayerCost());
        playerDisplayState.setPlayerGoals(outputData.getPlayerGoals());
        playerDisplayState.setPlayerAssists(outputData.getPlayerAssists());
        playerDisplayState.setPlayerPoints(outputData.getPlayerPoints());

        viewModel.firePropertyChange();
    }
}