package view;

import entity.Player;
import interface_adapter.team_entry.TeamEntryController;
import interface_adapter.team_entry.TeamEntryState;
import interface_adapter.team_entry.TeamEntryViewModel;
import interface_adapter.test_display_players.TestDisplayPlayersController;
import interface_adapter.test_display_players.TestDisplayPlayersViewModel;
import view.components.ScrollableListViewV2;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TeamEntryView extends JPanel implements PropertyChangeListener {

    private final String viewName = "team entry";

    private final TeamEntryViewModel teamEntryViewModel;
    private final TestDisplayPlayersViewModel playerListViewModel;

    private TeamEntryController teamEntryController;
    private TestDisplayPlayersController playerListController;

    private JTextField[] playerInputFields;
    private JTextField budgetField;

    private final JButton confirmButton;
    private final JButton menuButton;
    private final JButton resetButton;

    private final ScrollableListViewV2 playerSelectionPanel;

    private int currentlyEditingFieldIndex = -1;
    private boolean notDocListener = false;

    public TeamEntryView(TeamEntryViewModel teamEntryViewModel,
                         TestDisplayPlayersViewModel playerListViewModel) {

        this.teamEntryViewModel = teamEntryViewModel;
        this.playerListViewModel = playerListViewModel;

        teamEntryViewModel.addPropertyChangeListener(this);
        playerListViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JPanel topMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        menuButton = new JButton(TeamEntryViewModel.MENU_BUTTON_LABEL);
        topMenuPanel.add(menuButton);
        add(topMenuPanel, BorderLayout.NORTH);

        JPanel teamEntryPanel = new JPanel();
        teamEntryPanel.setLayout(new BoxLayout(teamEntryPanel, BoxLayout.Y_AXIS));
        teamEntryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel header = new JLabel(teamEntryViewModel.getTitleLabel());
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamEntryPanel.add(header);
        teamEntryPanel.add(Box.createVerticalStrut(15));

        String[] playerLabels = teamEntryViewModel.getPlayerLabels();
        playerInputFields = new JTextField[playerLabels.length];

        for (int i = 0; i < playerLabels.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel label = new JLabel(playerLabels[i] + ":");
            JTextField inputField = new JTextField(15);

            row.add(label);
            row.add(inputField);

            playerInputFields[i] = inputField;
            teamEntryPanel.add(row);
        }

        JPanel budgetRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        budgetRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel budgetLabel = new JLabel(teamEntryViewModel.getBudgetLabel());
        budgetField = new JTextField(15);

        budgetRow.add(budgetLabel);
        budgetRow.add(budgetField);
        teamEntryPanel.add(budgetRow);

        teamEntryPanel.add(Box.createVerticalStrut(15));

        resetButton = new JButton("Reset All");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamEntryPanel.add(resetButton);
        resetButton.addActionListener(e -> resetFields());

        JPanel centeringWrapper = new JPanel(new GridBagLayout());
        centeringWrapper.add(teamEntryPanel);

        JScrollPane scrollPane = new JScrollPane(centeringWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        playerSelectionPanel = new ScrollableListViewV2();
        playerSelectionPanel.setDimensions(600, 600);
        add(playerSelectionPanel, BorderLayout.EAST);

        playerSelectionPanel.setOnFilterChange(criteria -> {
            if (playerListController != null) {
                playerListController.filterPlayers(
                        criteria.searchText,
                        criteria.positionFilter,
                        criteria.teamFilter,
                        criteria.maxPrice
                );
            }
        });

        playerSelectionPanel.setPlayerSelectionListener(player -> {
            if (currentlyEditingFieldIndex != -1) {
                notDocListener = true;
                playerInputFields[currentlyEditingFieldIndex]
                        .setText(player.getWebName());
                notDocListener = false;

                updateViewModelField(
                        currentlyEditingFieldIndex,
                        player.getWebName(),
                        player.getId()
                );
            }
        });

        JPanel buttonPanel = new JPanel();
        confirmButton = new JButton(teamEntryViewModel.getConfirmButtonLabel());
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);

        menuButton.addActionListener(e -> {
            if (teamEntryController != null) {
                teamEntryController.switchToHomePage();
            }
        });

        confirmButton.addActionListener(e -> {
            if (teamEntryController != null) {
                teamEntryController.execute(
                        getFieldTexts(),
                        teamEntryViewModel.getState().getPlayerIds(),
                        getSlotPositions(),
                        teamEntryViewModel.getState().getBudget()
                );

            }
        });

        budgetField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                TeamEntryState state = teamEntryViewModel.getState();
                state.setBudget(budgetField.getText());
                teamEntryViewModel.setState(state);
            }

            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (playerListController != null) {
                    playerListController.loadAllPlayers();
                }
            }
        });

        addFieldListeners();
    }

    private void addFieldListeners() {
        for (int i = 0; i < playerInputFields.length; i++) {
            final int index = i;

            playerInputFields[i].getDocument().addDocumentListener(new DocumentListener() {
                private void update() {
                    if (notDocListener) return;
                    updateViewModelField(index, playerInputFields[index].getText(), -1);
                }

                @Override public void insertUpdate(DocumentEvent e) { update(); }
                @Override public void removeUpdate(DocumentEvent e) { update(); }
                @Override public void changedUpdate(DocumentEvent e) { update(); }
            });

            playerInputFields[i].addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    currentlyEditingFieldIndex = index;
                }
            });
        }
    }

    private void updateViewModelField(int index, String name, int id) {
        TeamEntryState state = teamEntryViewModel.getState();
        state.getPlayers()[index] = name;
        state.getPlayerIds()[index] = id;
        teamEntryViewModel.setState(state);
    }

    private void updateFieldsFromState(TeamEntryState state) {
        String[] players = state.getPlayers();
        if (players != null && players.length == playerInputFields.length) {
            notDocListener = true;
            for (int i = 0; i < players.length; i++) {
                playerInputFields[i].setText(players[i] != null ? players[i] : "");
            }
            notDocListener = false;
        }
    }

    private String[] getFieldTexts() {
        String[] texts = new String[playerInputFields.length];
        for (int i = 0; i < playerInputFields.length; i++) {
            texts[i] = playerInputFields[i].getText();
        }
        return texts;
    }

    private void resetFields() {
        notDocListener = true;
        for (JTextField field : playerInputFields) {
            field.setText("");
        }
        notDocListener = false;

        TeamEntryState state = teamEntryViewModel.getState();
        state.setPlayers(new String[playerInputFields.length]);
        state.setPlayerIds(new int[playerInputFields.length]);
        teamEntryViewModel.setState(state);
        teamEntryViewModel.firePropertyChange();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getSource() == teamEntryViewModel) {
            TeamEntryState state = teamEntryViewModel.getState();

            if (state.getErrorMessage() != null) {
                JOptionPane.showMessageDialog(this, state.getErrorMessage());
            }

            if (state.getSuccessMessage() != null) {
                JOptionPane.showMessageDialog(this,
                        state.getSuccessMessage(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            updateFieldsFromState(state);
        }

        if (evt.getSource() == playerListViewModel) {
            var state = playerListViewModel.getState();
            if (state.getErrorMessage() != null) {
                JOptionPane.showMessageDialog(this, state.getErrorMessage());
                return;
            }

            playerSelectionPanel.setPlayers(
                    state.getPlayers(),
                    state.getAvailableTeams()
            );
        }
    }

    public void setTeamEntryController(TeamEntryController controller) {
        this.teamEntryController = controller;
    }

    public void setPlayerListController(TestDisplayPlayersController controller) {
        this.playerListController = controller;
    }

    public String getViewName() {
        return viewName;
    }

    private String[] getSlotPositions() {
        String[] positions = new String[playerInputFields.length];

        // 0–2: Forwards
        positions[0] = "forward";
        positions[1] = "forward";
        positions[2] = "forward";

        // 3–7: Midfielders
        for (int i = 3; i <= 7; i++) {
            positions[i] = "midfielder";
        }

        // 8–12: Defenders
        for (int i = 8; i <= 12; i++) {
            positions[i] = "defender";
        }

        // 13–14: Goalkeepers
        positions[13] = "goalkeeper";
        positions[14] = "goalkeeper";

        return positions;
    }

}
