package use_case.team_entry;

public interface TeamEntryOutputBoundary {
    /**
     * Prepares the success view for the Team Entry Page Use Case.
     * @param outputData the output data
     */
    void prepareSuccessView(TeamEntryOutputData outputData);

    /**
     * Prepares the fail view for the Team Entry Page Use Case.
     * @param errorMessage the error message associated with the fail reason
     */
    void prepareFailView(String errorMessage);

    /**
     * Switches to the Home Page View.
     */
    void switchToHomePage();

    /**
     * Opens default team entry page view.
     */
    void prepareOpenPageView();

    /**
     * Opens team entry page view with saved players if user has already entered and confirmed a team in the past.
     * @param names array of names of the saved players
     * @param ids array of saved players' ids
     */
    void prepareSavedTeamView(String[] names, int[] ids);
}
