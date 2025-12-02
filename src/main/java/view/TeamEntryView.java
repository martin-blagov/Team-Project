package view;

import interface_adapter.team_entry.TeamEntryController;
import interface_adapter.team_entry.TeamEntryState;
import interface_adapter.team_entry.TeamEntryViewModel;
import interface_adapter.test_display_players.TestDisplayPlayersController;
import interface_adapter.test_display_players.TestDisplayPlayersViewModel;
import view.components.ScrollableListViewV2;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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

    private JButton confirmButton;
    private JButton menuButton;
    private JButton resetButton;

    private final ScrollableListViewV2 playerSelectionPanel;

    private int currentlyEditingFieldIndex = -1;
    private boolean notDocListener;

    public TeamEntryView(TeamEntryViewModel teamEntryViewModel,
                         TestDisplayPlayersViewModel playerListViewModel) {

        this.teamEntryViewModel = teamEntryViewModel;
        this.playerListViewModel = playerListViewModel;

        teamEntryViewModel.addPropertyChangeListener(this);
        playerListViewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        final JPanel topMenuPanel = createTopMenuPanel();
        add(topMenuPanel, BorderLayout.NORTH);

        final JPanel teamEntryPanel = createTeamEntryPanel();

        final JPanel centeringWrapper = new JPanel(new GridBagLayout());
        centeringWrapper.add(teamEntryPanel);

        final JScrollPane scrollPane = new JScrollPane(centeringWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        playerSelectionPanel = createPlayerSelectionPanel();
        add(playerSelectionPanel, BorderLayout.EAST);

        final JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        setupEventListeners();
    }

    private JPanel createTopMenuPanel() {
        final JPanel topMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        menuButton = new JButton(TeamEntryViewModel.MENU_BUTTON_LABEL);
        topMenuPanel.add(menuButton);
        return topMenuPanel;
    }

    private JPanel createTeamEntryPanel() {
        final JPanel teamEntryPanel = new JPanel();
        teamEntryPanel.setLayout(new BoxLayout(teamEntryPanel, BoxLayout.Y_AXIS));
        teamEntryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JLabel header = new JLabel(teamEntryViewModel.getTitleLabel());
        header.setFont(new Font("Arial", Font.BOLD, HEADER_FONT_SIZE));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamEntryPanel.add(header);
        teamEntryPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_SIZE));

        addPlayerInputFields(teamEntryPanel);
        addBudgetField(teamEntryPanel);

        teamEntryPanel.add(Box.createVerticalStrut(VERTICAL_STRUT_SIZE));

        resetButton = new JButton("Reset All");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamEntryPanel.add(resetButton);
        resetButton.addActionListener(err -> resetFields());

        return teamEntryPanel;
    }

    private void addPlayerInputFields(JPanel teamEntryPanel) {
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
    }

    private void addBudgetField(JPanel teamEntryPanel) {
        final JPanel budgetRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        budgetRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JLabel budgetLabel = new JLabel(teamEntryViewModel.getBudgetLabel());
        budgetField = new JTextField(TEXT_FIELD_WIDTH);

        budgetRow.add(budgetLabel);
        budgetRow.add(budgetField);
        teamEntryPanel.add(budgetRow);
    }

    private ScrollableListViewV2 createPlayerSelectionPanel() {
        final ScrollableListViewV2 panel = new ScrollableListViewV2();
        panel.setDimensions(PANEL_DIMENSION, PANEL_DIMENSION);

        panel.setOnFilterChange(criteria -> {
            if (playerListController != null) {
                playerListController.filterPlayers(
                        criteria.searchText,
                        criteria.positionFilter,
                        criteria.teamFilter,
                        criteria.maxPrice
                );
            }
        });

        panel.setPlayerSelectionListener(player -> {
            if (currentlyEditingFieldIndex != -1) {
                notDocListener = true;
                playerInputFields[currentlyEditingFieldIndex].setText(player.getWebName());
                notDocListener = false;
                updateViewModelField(currentlyEditingFieldIndex, player.getWebName(), player.getId()
                );
            }
        });

        return panel;
    }

    private JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel();
        confirmButton = new JButton(teamEntryViewModel.getConfirmButtonLabel());
        buttonPanel.add(confirmButton);
        return buttonPanel;
    }

    private void setupEventListeners() {
        menuButton.addActionListener(event -> {
            if (teamEntryController != null) {
                teamEntryController.switchToHomePage();
            }
        });

        confirmButton.addActionListener(event -> {
            if (teamEntryController != null) {
                teamEntryController.execute(getFieldTexts(), teamEntryViewModel.getState().getPlayerIds(),
                        getSlotPositions(), teamEntryViewModel.getState().getBudget()
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
                // Clear any previous messages when view is shown
                final TeamEntryState state = teamEntryViewModel.getState();
                state.setErrorMessage(null);
                state.setSuccessMessage(null);
                teamEntryViewModel.setState(state);

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
                    if (!notDocListener) {
                        updateViewModelField(index, playerInputFields[index].getText(), -1);
                    }
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
                if (players[i] != null) {
                    playerInputFields[i].setText(players[i]);
                }
                else {
                    playerInputFields[i].setText("");
                }
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
            if (state.getErrorMessage() == null) {
                playerSelectionPanel.setPlayers(
                        state.getPlayers(),
                        state.getAvailableTeams()
                );
            }
            else {
                JOptionPane.showMessageDialog(this, state.getErrorMessage());
            }
        }
    }

    public void setTeamEntryController(TeamEntryController controller) {
        this.teamEntryController = controller;
    }

    public void setPlayerListController(TestDisplayPlayersController controller) {
        this.playerListController = controller;
    }

    /**
     * Gets View Name.
     * @return viewName the name of the view
     */
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