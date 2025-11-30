package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();

        JFrame application = appBuilder
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

        application.setSize(1100, 800);
        application.setLocationRelativeTo(null);
        application.setVisible(true);
    }
}
