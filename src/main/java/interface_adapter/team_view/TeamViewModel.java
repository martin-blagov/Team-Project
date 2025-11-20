package interface_adapter.team_view;

import entity.Team;
import interface_adapter.ViewModel;

/**
 * Reusable ViewModel for displaying players in the team in table format.
 */
public class TeamViewModel extends ViewModel<Team>{

    public static final String TEAM_STATE_LABEL = "team";
    private final DisplayConfig displayConfig;

    /**
     * Creates a view model for a specific use case.
     */
    public TeamViewModel(String viewName, DisplayConfig displayConfig){
        super(viewName);
        this.displayConfig = displayConfig;
    }

    /**
     * @return immutable display configuration describing how the team should be displayed.
     */
    public DisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    /**
     * Updates the current team and notifies listeners that TEAM_STATE_LABEL changed.
     *
     * @param team team to render, or {@code null} to clear the table
     */
    public void setTeam(Team team) {
        this.setState(team);
        this.firePropertyChange(TEAM_STATE_LABEL);
    }


    public Team getTeam() {
        return this.getState();
    }


    public boolean hasTeam() {
        return this.getState() != null;
    }


    public void clearTeam() {
        this.setState(null);
        this.firePropertyChange(TEAM_STATE_LABEL);
    }

    /**
     * Immutable configuration describing how a team should be displayed.
     */
    public static class DisplayConfig {
        private final String title;
        private final String emptyStateMessage;
        private final boolean showBudget;
        private final String[] columnHeaders;

        public DisplayConfig(String title,
                             String emptyStateMessage,
                             boolean showBudget,
                             String[] columnHeaders) {
            this.title = title;
            this.emptyStateMessage = emptyStateMessage;
            this.showBudget = showBudget;
            this.columnHeaders = columnHeaders;
        }


        public String getTitle() {
            return title;
        }


        public String getEmptyStateMessage() {
            return emptyStateMessage;
        }


        public boolean shouldShowBudget() {
            return showBudget;
        }


        public String[] getColumnHeaders() {
            return columnHeaders;
        }
    }
}
