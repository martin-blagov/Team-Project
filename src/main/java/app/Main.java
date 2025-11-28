package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();
        JFrame application = appBuilder
                .addInitialisePredictions()
                .addHomePageView()
                .addTeamEntryView()
                .addDisplayIndividualStatsView()
                .addStartingLineupView()
                .addBestTeamView()
                .addRiskAssessmentView()
                .addDisplayIndividualStatUseCase()
                .addTeamEntryViewUseCase()
                .addTestDisplayPlayersUseCase() //  todo remove
                .addTestTeamVisualizationUseCase() // todo remove
                .addStartingLineupUseCase()
                .addRiskAssessmentUseCase()
                .addHomeUseCase()
                .addBestTeamUseCase()
                .build();
        application.setSize(1100, 800);  // width=1000px, height=700px
        application.setLocationRelativeTo(null); // center on screen
        application.setVisible(true);
    }
}
