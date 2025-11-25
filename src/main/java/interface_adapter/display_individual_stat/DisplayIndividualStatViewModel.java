package interface_adapter.display_individual_stat;

import interface_adapter.ViewModel;

public class DisplayIndividualStatViewModel extends ViewModel<DisplayIndividualStatState> {

    // Individual Stats View Labels
    public static final String TITLE_LABEL = "Player Stats";
    public static final String HOME_BUTTON_LABEL = "Home";
    public static final String NAME_LABEL = "Name: ";
    public static final String AGE_LABEL = "Age: ";
    public static final String POSITION_LABEL = "Position: ";
    public static final String TEAM_LABEL = "Team: ";
    public static final String PRICE_LABEL = "Price: ";
    public static final String GOALS_SCORED_LABEL = "Goals Scored: ";
    public static final String ASSISTS_LABEL = "Assists: ";
    public static final String POINTS_LABEL = "Points: ";

    public DisplayIndividualStatViewModel() {
        super("display individual stats");
        setState(new DisplayIndividualStatState());
    }
}
