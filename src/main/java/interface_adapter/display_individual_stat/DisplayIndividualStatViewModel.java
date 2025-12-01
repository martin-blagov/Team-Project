package interface_adapter.display_individual_stat;

import interface_adapter.ViewModel;

public class DisplayIndividualStatViewModel extends ViewModel<DisplayIndividualStatState> {

    // Individual Stats View Labels
    public static final String TITLE_LABEL = "Player Stats";
    public static final String HOME_BUTTON_LABEL = "Home";


    public DisplayIndividualStatViewModel() {
        super("display individual stats");
        setState(new DisplayIndividualStatState());
    }
}
