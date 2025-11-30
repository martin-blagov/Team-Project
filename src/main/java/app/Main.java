package app;

import javax.swing.*;

public class Main {
    private static final int APP_WIDTH = 1100;
    private static final int APP_HEIGHT = 800;

    /**
     * Builds the Fantasy Premier League App.
     * @param args configures app at start up.
     */
    public static void main(String[] args) {
        final AppBuilder appBuilder = new AppBuilder();

        final JFrame application = appBuilder
                .addInitialisePredictions()
                .addTestDisplayPlayersUseCase()
                .addHomePageView()
                .addDisplayIndividualStatsView()
                .addStartingLineupView()
                .addBestTeamView()
                .addTeamEntryView()
                .addDisplayIndividualStatUseCase()
                .addTeamEntryViewUseCase()
                .addTestDisplayPlayersUseCase() //  todo remove
                .addTestTeamVisualizationUseCase() // todo remove
                .addStartingLineupUseCase()
                .addBestTeamUseCase()
                .addHomeUseCase()
                .build();

        application.setSize(APP_WIDTH, APP_HEIGHT);
        application.setLocationRelativeTo(null);
        application.setVisible(true);
    }
}
