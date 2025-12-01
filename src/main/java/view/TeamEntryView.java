package view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import interface_adapter.team_entry.TeamEntryController;
import interface_adapter.team_entry.TeamEntryState;
import interface_adapter.team_entry.TeamEntryViewModel;
import interface_adapter.test_display_players.TestDisplayPlayersController;
import interface_adapter.test_display_players.TestDisplayPlayersViewModel;
import view.components.ScrollableListViewV2;

public class TeamEntryView extends JPanel implements PropertyChangeListener {

    private static final int FORWARD_START = 0;
    private static final int FORWARD_END = 2;
    private static final int MIDFIELDER_START = 3;
    private static final int MIDFIELDER_END = 7;
    private static final int DEFENDER_START = 8;
    private static final int DEFENDER_END = 12;
    private static final int GOALKEEPER_START = 13;
    private static final int GOALKEEPER_END = 14;
    private static final int TEXT_FIELD_WIDTH = 15;
    private static final int HEADER_FONT_SIZE = 18;
    private static final int VERTICAL_STRUT_SIZE = 15;
    private static final int PANEL_DIMENSION = 600;

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

        final JPanel topMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        menuButton = new JButton(TeamEntryViewModel.MENU_BUTTON_LABEL);
        topMenuPanel.add(menuButton);
        add(topMenuPanel, BorderLayout.NORTH);

        final JPanel teamEntryPanel = new JPanel();
        teamEntryPanel.setLayout(new BoxLayout(teamEntryPanel, BoxLayout.Y_AXIS));
        teamEntryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JLabel header = new JLabel(teamEntryViewModel.getTitleLabel());
        header.setFont(new Font("Arial", Font.BOLD, HEADER_FONT_SIZE));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamEntryPanel.add(header);
        teamEntryPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_SIZE));

        final String[] playerLabels = teamEntryViewModel.getPlayerLabels();
        playerInputFields = new JTextField[playerLabels.length];

        for (int i = 0; i < playerLabels.length; i++) {
            final JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            final JLabel label = new JLabel(playerLabels[i] + ":");
            final JTextField inputField = new JTextField(TEXT_FIELD_WIDTH);

            row.add(label);
            row.add(inputField);

            playerInputFields[i] = inputField;
            teamEntryPanel.add(row);
        }

        final JPanel budgetRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        budgetRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JLabel budgetLabel = new JLabel(teamEntryViewModel.getBudgetLabel());
        budgetField = new JTextField(TEXT_FIELD_WIDTH);

        budgetRow.add(budgetLabel);
        budgetRow.add(budgetField);
        teamEntryPanel.add(budgetRow);

        teamEntryPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_SIZE));

        resetButton = new JButton("Reset All");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamEntryPanel.add(resetButton);
        resetButton.addActionListener(e -> resetFields());

        final JPanel centeringWrapper = new JPanel(new GridBagLayout());
        centeringWrapper.add(teamEntryPanel);

        final JScrollPane scrollPane = new JScrollPane(centeringWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        playerSelectionPanel = new ScrollableListViewV2();
        playerSelectionPanel.setDimensions(PANEL_DIMENSION, PANEL_DIMENSION);
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

        final JPanel buttonPanel = new JPanel();
        confirmButton = new JButton(teamEntryViewModel.getConfirmButtonLabel());
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);

        menuButton.addActionListener(event -> {
            if (teamEntryController != null) {
                teamEntryController.switchToHomePage();
            }
        });

        confirmButton.addActionListener(event -> {
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
                final TeamEntryState state = teamEntryViewModel.getState();
                state.setBudget(budgetField.getText());
                teamEntryViewModel.setState(state);
            }

            @Override public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override public void changedUpdate(DocumentEvent e) {
                update();
            }
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
                    if (notDocListener) {
                        return;
                    }
                    updateViewModelField(index, playerInputFields[index].getText(), -1);
                }

                @Override public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override public void changedUpdate(DocumentEvent e) {
                    update();
                }
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
        final TeamEntryState state = teamEntryViewModel.getState();
        state.getPlayers()[index] = name;
        state.getPlayerIds()[index] = id;
        teamEntryViewModel.setState(state);
    }

    private void updateFieldsFromState(TeamEntryState state) {
        final String[] players = state.getPlayers();
        if (players != null && players.length == playerInputFields.length) {
            notDocListener = true;
            for (int i = 0; i < players.length; i++) {
                playerInputFields[i].setText(players[i] != null ? players[i] : "");
            }
            notDocListener = false;
        }
    }

    private String[] getFieldTexts() {
        final String[] texts = new String[playerInputFields.length];
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

        final TeamEntryState state = teamEntryViewModel.getState();
        state.setPlayers(new String[playerInputFields.length]);
        state.setPlayerIds(new int[playerInputFields.length]);
        teamEntryViewModel.setState(state);
        teamEntryViewModel.firePropertyChange();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getSource() == teamEntryViewModel) {
            final TeamEntryState state = teamEntryViewModel.getState();

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
            final var state = playerListViewModel.getState();
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
        final String viewName = "team entry";
        return viewName;
    }

    private String[] getSlotPositions() {
        final String[] positions = new String[playerInputFields.length];

        // 0–2: Forwards
        for (int i = FORWARD_START; i <= FORWARD_END; i++) {
            positions[i] = "forward";
        }

        // 3–7: Midfielders
        for (int i = MIDFIELDER_START; i <= MIDFIELDER_END; i++) {
            positions[i] = "midfielder";
        }

        // 8–12: Defenders
        for (int i = DEFENDER_START; i <= DEFENDER_END; i++) {
            positions[i] = "defender";
        }

        // 13–14: Goalkeepers
        positions[GOALKEEPER_START] = "goalkeeper";
        positions[GOALKEEPER_END] = "goalkeeper";

        return positions;
    }

}