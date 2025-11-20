package app;

import javax.swing.*;

public class   Main {
    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();
        JFrame application = appBuilder
                .addInitialisePredictions()
                .addHomePageView()
                .addTeamEntryView()
                .addStartingLineupView()
                .addOpenTeamEntryViewUseCase()
                .addStartingLineupUseCase()
                .addHomeUseCase()
                .build();
        application.setSize(500, 500);  // width=1000px, height=700px
        application.setLocationRelativeTo(null); // center on screen
        application.setVisible(true);
    }
}
