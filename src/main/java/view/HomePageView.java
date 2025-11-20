package view;

import interface_adapter.home.HomeController;
import interface_adapter.home.HomeViewModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The View for the Home Page of the Premier League Fantasy App
 */
public class HomePageView extends JPanel implements PropertyChangeListener {

    private final String viewName = "home";

    private final HomeViewModel homeViewModel;
    private HomeController homeController = null;

    private final JButton bestTeamButton;
    private final JButton teamInputButton;
    private final JButton replacementButton;
    private final JButton bestPlayersButton;
    private final JButton transferButton;
    private final JButton statsButton;
    private final JButton lineupButton;

    public HomePageView(HomeViewModel homeViewModel) {
        this.homeViewModel = homeViewModel;
        homeViewModel.addPropertyChangeListener(this);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Color.white);

        // Page Title
        final JLabel title = new JLabel(HomeViewModel.TITLE_LABEL);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(title);
        this.add(Box.createRigidArea(new Dimension(0, 20)));

        // Page Buttons
        bestTeamButton = new JButton(HomeViewModel.BEST_TEAM_BUTTON_LABEL);
        teamInputButton = new JButton(HomeViewModel.TEAM_INPUT_BUTTON_LABEL);
        replacementButton = new JButton(HomeViewModel.REPLACEMENT_BUTTON_LABEL);
        bestPlayersButton = new JButton(HomeViewModel.BEST_PLAYERS_BUTTON_LABEL);
        transferButton = new JButton(HomeViewModel.TRANSFER_BUTTON_LABEL);
        statsButton = new JButton(HomeViewModel.STATS_BUTTON_LABEL);
        lineupButton = new JButton(HomeViewModel.LINEUP_BUTTON_LABEL);

        // Set consistent button size
        Dimension buttonSize = new Dimension(250, 40);
        JButton[] buttons = {
                bestTeamButton, teamInputButton, replacementButton,
                bestPlayersButton, transferButton, statsButton, lineupButton
        };

        for (JButton button : buttons) {
            button.setMaximumSize(buttonSize);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.add(button);
            this.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Action listeners for buttons
        bestTeamButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        homeController.openBestTeamPage();
                    }
                }
        );

        teamInputButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        homeController.openTeamInputPage();
                    }
                }
        );

        replacementButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        homeController.openReplacementPage();
                    }
                }
        );

        bestPlayersButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        homeController.openBestPlayersPage();
                    }
                }
        );

        transferButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        homeController.openTransferPage();
                    }
                }
        );

        statsButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        homeController.openStatsPage();
                    }
                }
        );

        lineupButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        homeController.openLineupPage();
                    }
                }
        );
    }

    public String getViewName() {
        return viewName;
    }

    public void setHomeController(HomeController controller) {
        this.homeController = controller;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
