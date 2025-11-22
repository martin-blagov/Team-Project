package interface_adapter.display_individual_stat;

import interface_adapter.ViewModel;

public class DisplayIndividualStatViewModel extends ViewModel<DisplayIndividualStatState> {
    public DisplayIndividualStatViewModel() {
        super("display individual stats");
        setState(new DisplayIndividualStatState());
    }
}
