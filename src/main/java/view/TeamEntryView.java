package view;

import entity.Player;
import interface_adapter.team_entry.TeamEntryController;
import interface_adapter.team_entry.TeamEntryState;
import interface_adapter.team_entry.TeamEntryViewModel;
import use_case.PlayerDataAccessInterface;
import view.components.ScrollableListView;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The View for the Team Entry Page of the Premier League Fantasy App
 */
public class TeamEntryView extends JPanel implements PropertyChangeListener {

    private final String viewName = "team entry";

    private final TeamEntryViewModel teamEntryViewModel;
    private TeamEntryController teamEntryController = null;

    private JTextField[] playerInputFields;

    private final JButton confirmButton;
    private final JButton menuButton;

    private ScrollableListView playerSelectionPanel;
    private final PlayerDataAccessInterface playerDataAccess;

    private int currentlyEditingFieldIndex = -1;
    private boolean notDocListener = false;

    public TeamEntryView(TeamEntryViewModel teamEntryViewModel, PlayerDataAccessInterface playerDataAccess) {
        this.playerDataAccess = playerDataAccess;
        this.teamEntryViewModel = teamEntryViewModel;
        teamEntryViewModel.addPropertyChangeListener(this);

        this.setLayout(new BorderLayout());

        // Menu button
        JPanel topMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        menuButton = new JButton(TeamEntryViewModel.MENU_BUTTON_LABEL);
        topMenuPanel.add(menuButton);
        this.add(topMenuPanel, BorderLayout.NORTH);

        // Team entry panel
        JPanel teamEntryPanel = new JPanel();
        teamEntryPanel.setLayout(new BoxLayout(teamEntryPanel, BoxLayout.Y_AXIS));
        teamEntryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel header = new JLabel(teamEntryViewModel.getTitleLabel());
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        teamEntryPanel.add(header);
        teamEntryPanel.add(Box.createVerticalStrut(15));

        // Player entry rows
        String[] playerLabels = teamEntryViewModel.getPlayerLabels();
        playerInputFields = new JTextField[playerLabels.length];

        for (int i = 0; i < playerLabels.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // right-justify textfields
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel label = new JLabel(playerLabels[i] + ":");
            JTextField inputField = new JTextField(15);

            row.add(label);
            row.add(inputField);

            playerInputFields[i] = inputField;
            teamEntryPanel.add(row);
        }

        // Add space
        teamEntryPanel.add(Box.createVerticalStrut(15));

        // Reset button under all text fields
                JButton resetButton = new JButton("Reset All");
                resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                teamEntryPanel.add(resetButton);

        // Add listener
                resetButton.addActionListener(evt -> resetFields());

        // Scroll
        JPanel centeringWrapper = new JPanel(new GridBagLayout());
        centeringWrapper.add(teamEntryPanel);

        JScrollPane scrollPane = new JScrollPane(centeringWrapper);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.add(scrollPane, BorderLayout.CENTER);

        // Scrollable player list
        playerSelectionPanel = new ScrollableListView(playerDataAccess);
        playerSelectionPanel.setPreferredSize(new Dimension(600, 600));
        this.add(playerSelectionPanel, BorderLayout.EAST);
        playerSelectionPanel.refresh();

        // When a player is selected in the list, update the correct text field
        playerSelectionPanel.setPlayerSelectionListener((Player selectedPlayer) -> {
            if (currentlyEditingFieldIndex != -1) {

                notDocListener = true;
                playerInputFields[currentlyEditingFieldIndex].setText(selectedPlayer.getWebName());
                notDocListener = false;

                // Update ViewModel with correct name + ID
                updateViewModelField(
                        currentlyEditingFieldIndex,
                        selectedPlayer.getWebName(),
                        selectedPlayer.getId()
                );
            }
        });



        // Confirm button
        JPanel buttonPanel = new JPanel();
        confirmButton = new JButton(teamEntryViewModel.getConfirmButtonLabel());
        buttonPanel.add(confirmButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // Menu button action listener
        menuButton.addActionListener(evt -> {
            if (teamEntryController != null) {
                teamEntryController.switchToHomePage();
            }
        });

        // Confirm button action listener
        confirmButton.addActionListener(evt -> {
            if (teamEntryController != null) {
                teamEntryController.execute(getFieldTexts());
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

                    String name = playerInputFields[index].getText();
                    updateViewModelField(index, name, -1);
                }
                @Override public void insertUpdate(DocumentEvent e) { update(); }
                @Override public void removeUpdate(DocumentEvent e) { update(); }
                @Override public void changedUpdate(DocumentEvent e) { update(); }
            });

            // When the field is clicked, show the player list and remember which text field we're on
            playerInputFields[i].addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    currentlyEditingFieldIndex = index;
                    playerSelectionPanel.refresh();
                }
            });
        }
    }

    private String[] getFieldTexts() {
        String[] texts = new String[playerInputFields.length];
        for (int i = 0; i < playerInputFields.length; i++) {
            texts[i] = playerInputFields[i].getText();
        }
        return texts;
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


    private void updateViewModelField(int index, String name, int id) {
        TeamEntryState state = teamEntryViewModel.getState();
        String[] names = state.getPlayers();
        int[] ids = state.getPlayerIds();

        names[index] = name;
        ids[index] = id;

        teamEntryViewModel.setState(state);
    }


    private void resetFields() {
        // Clear text boxes
        notDocListener = true;
        for (int i = 0; i < playerInputFields.length; i++) {
            playerInputFields[i].setText("");
        }
        notDocListener = false;

        // Clear state arrays
        TeamEntryState state = teamEntryViewModel.getState();

        String[] emptyNames = new String[playerInputFields.length];
        int[] emptyIds = new int[playerInputFields.length];

        for (int i = 0; i < emptyIds.length; i++) {
            emptyNames[i] = "";
            emptyIds[i] = -1;
        }

        state.setPlayers(emptyNames);
        state.setPlayerIds(emptyIds);

        teamEntryViewModel.setState(state);
        teamEntryViewModel.firePropertyChange();
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        TeamEntryState state = (TeamEntryState) evt.getNewValue();
        if (state.getErrorMessage() != null) {
            JOptionPane.showMessageDialog(this, state.getErrorMessage());
        }

        if (state.getSuccessMessage() != null) {
            JOptionPane.showMessageDialog(this, state.getSuccessMessage(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        updateFieldsFromState(state);
    }

    public String getViewName() {
        return viewName;
    }

    public void setTeamEntryController(TeamEntryController controller) {
        this.teamEntryController = controller;
    }

}
