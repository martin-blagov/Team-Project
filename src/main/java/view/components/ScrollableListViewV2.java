package view. components;

import entity.Player;

import javax.swing.*;
import java.awt.*;
import java. util. ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util. stream.Collectors;

/**
 * V2 - REFACTORED Scrollable List View (DUMB COMPONENT)
 *
 * This version follows Clean Architecture:
 * - NO PlayerDataAccessInterface dependency
 * - Receives data via setPlayers() method
 * - Notifies parent view when filters change (via callback)
 * - Pure UI component - reusable across use cases
 *
 * DIFFERENCES FROM V1:
 * - No data access dependency
 * - No refresh() method that fetches data
 * - Uses callback pattern to notify parent of filter changes
 */
public class ScrollableListViewV2 extends JPanel {

    // UI Components
    private final DefaultListModel<String> listModel;
    private final JScrollPane scrollPane;
    private final JList<String> playerList;
    private JTextField searchField;
    private JComboBox<String> positionFilter;
    private JComboBox<String> teamFilter;
    private JComboBox<String> priceFilter;

    // Data (received from parent, not fetched)
    private List<Player> allPlayers;

    // Callback for when filters change (using Java's Consumer)
    private Consumer<FilterCriteria> filterChangeCallback;
    private Consumer<Player> playerSelectionListener;

    /**
     * Simple data class to hold filter criteria.
     */
    public static class FilterCriteria {
        public final String searchText;
        public final String positionFilter;
        public final String teamFilter;
        public final Double maxPrice;

        public FilterCriteria(String searchText, String positionFilter,
                              String teamFilter, Double maxPrice) {
            this.searchText = searchText;
            this.positionFilter = positionFilter;
            this. teamFilter = teamFilter;
            this. maxPrice = maxPrice;
        }
    }

    /**
     * Constructor - NO dependencies!
     * This makes the component reusable across different use cases.
     */
    public ScrollableListViewV2() {
        this. allPlayers = new ArrayList<>();

        setLayout(new BorderLayout());

        // Create filter panel at the top
        JPanel filterPanel = new JPanel();
        filterPanel. setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));

        // Search bar (first row)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(new javax.swing.event. DocumentListener() {
            public void changedUpdate(javax.swing.event. DocumentEvent e) { notifyFilterChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { notifyFilterChange(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { notifyFilterChange(); }
        });
        searchPanel.add(searchField);
        filterPanel.add(searchPanel);

        // Dropdown filters (second row)
        JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

        // Position filter
        positionFilter = new JComboBox<>(new String[]{
                "All positions", "Goalkeeper", "Defender", "Midfielder", "Forward"
        });
        positionFilter.addActionListener(e -> notifyFilterChange());
        dropdownPanel.add(positionFilter);

        // Team filter
        teamFilter = new JComboBox<>();
        teamFilter. addItem("All teams");
        teamFilter.addActionListener(e -> notifyFilterChange());
        dropdownPanel.add(teamFilter);

        // Price filter
        priceFilter = new JComboBox<>();
        priceFilter.addItem("Any price");
        for (double price = 3.5; price <= 15.5; price += 0.5) {
            priceFilter. addItem(String. format("£%.1fm", price));
        }
        priceFilter.addActionListener(e -> notifyFilterChange());
        dropdownPanel.add(priceFilter);

        filterPanel. add(dropdownPanel);
        add(filterPanel, BorderLayout. NORTH);

        // === LIST PANEL WITH FROZEN HEADER ===
        JPanel listPanel = new JPanel(new BorderLayout());

        // Frozen header row
        JLabel headerLabel = new JLabel(String.format("%-20s %-15s %-12s %-9s %s",
                "Name", "Team", "Position", "Price", "Predicted Pts"));
        headerLabel.setFont(new Font("Monospaced", Font. BOLD, 12));
        headerLabel.setBorder(BorderFactory. createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory. createEmptyBorder(5, 5, 5, 5)
        ));
        headerLabel.setBackground(new Color(240, 240, 240));
        headerLabel.setOpaque(true);
        listPanel.add(headerLabel, BorderLayout.NORTH);

        // Create the list model and JList
        listModel = new DefaultListModel<>();
        playerList = new JList<>(listModel);
        playerList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        playerList.setFixedCellWidth(550);
        playerList.setFixedCellHeight(20);
        playerList.setVisibleRowCount(30);

        // Make it scrollable with border
        scrollPane = new JScrollPane(playerList);
        scrollPane.setPreferredSize(new Dimension(550, 600));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane. HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory. createLineBorder(Color. GRAY, 1));
        scrollPane.setMinimumSize(new Dimension(600, 400));
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
        listPanel.add(scrollPane, BorderLayout. CENTER);

        add(listPanel, BorderLayout.CENTER);

        // Add resize listener to scroll left
        addComponentListener(new java.awt.event. ComponentAdapter() {
            @Override
            public void componentResized(java.awt. event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> scrollToLeft());
            }
        });

        playerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && playerList.getSelectedIndex() != -1) {
                handlePlayerSelection();
            }
        });
    }

    /**
     * Set the players to display.
     * This is called by the parent view after receiving data from the ViewModel.
     *
     * @param players List of players to display (already sorted/filtered by use case)
     * @param availableTeams List of team names for the team filter dropdown
     */
    public void setPlayers(List<Player> players, List<String> availableTeams) {
        if (players == null) {
            players = new ArrayList<>();
        }

        // Store the players (defensive copy)
        this.allPlayers = new ArrayList<>(players);

        // Update team filter dropdown
        updateTeamFilter(availableTeams);

        // Display the players (apply UI-level filtering for instant feedback)
        filterAndDisplayPlayers();
    }

    /**
     * Update the team filter dropdown with available teams.
     */
    private void updateTeamFilter(List<String> availableTeams) {
        String currentSelection = (String) teamFilter.getSelectedItem();
        teamFilter.removeAllItems();
        teamFilter.addItem("All teams");

        if (availableTeams != null) {
            for (String team : availableTeams) {
                teamFilter.addItem(team);
            }
        }

        // Restore selection if possible
        if (currentSelection != null) {
            teamFilter. setSelectedItem(currentSelection);
        }
    }

    /**
     * Set the callback for filter changes.
     * Parent view should set this to wire up to the Controller.
     *
     * Example usage:
     * listView.setOnFilterChange(criteria -> {
     *     controller.filterPlayers(
     *         criteria. searchText,
     *         criteria.positionFilter,
     *         criteria.teamFilter,
     *         criteria.maxPrice
     *     );
     * });
     *
     * @param callback Callback to notify when filters change
     */
    public void setOnFilterChange(Consumer<FilterCriteria> callback) {
        this.filterChangeCallback = callback;
    }

    /**
     * Notify parent view that filters have changed.
     * This triggers the use case to re-fetch/re-filter data.
     */
    private void notifyFilterChange() {
        if (filterChangeCallback != null) {
            FilterCriteria criteria = new FilterCriteria(
                    searchField.getText(),
                    (String) positionFilter.getSelectedItem(),
                    (String) teamFilter.getSelectedItem(),
                    extractMaxPrice()
            );

            filterChangeCallback. accept(criteria);
        }

        // Also apply UI-level filtering for instant feedback
        filterAndDisplayPlayers();
    }

    /**
     * Extract max price from the price filter dropdown.
     *
     * @return Max price as Double, or null if "Any price" selected
     */
    private Double extractMaxPrice() {
        String selectedPrice = (String) priceFilter. getSelectedItem();
        if (selectedPrice == null || selectedPrice.equals("Any price")) {
            return null;
        }

        String priceStr = selectedPrice.replace("£", ""). replace("m", "");
        return Double.parseDouble(priceStr);
    }

    /**
     * Apply UI-level filtering and display players.
     *
     * This is CLIENT-SIDE filtering for instant UX feedback.
     * The real filtering happens in the Interactor.
     */
    private void filterAndDisplayPlayers() {
        String searchText = searchField.getText().toLowerCase(). trim();
        String selectedPosition = (String) positionFilter.getSelectedItem();
        String selectedTeam = (String) teamFilter.getSelectedItem();
        String selectedPrice = (String) priceFilter. getSelectedItem();

        // If any selection is null, skip filtering
        if (selectedPosition == null || selectedTeam == null || selectedPrice == null) {
            return;
        }

        // Extract max price
        Double maxPrice = extractMaxPrice();

        // Apply filters
        List<Player> filtered = allPlayers.stream()
                .filter(player -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty() ||
                            player.getWebName(). toLowerCase().contains(searchText);

                    // Position filter
                    boolean matchesPosition = selectedPosition.equals("All positions") ||
                            player. getPosition().equalsIgnoreCase(selectedPosition);

                    // Team filter
                    boolean matchesTeam = selectedTeam.equals("All teams") ||
                            (player.getTeamName() != null && player.getTeamName().equals(selectedTeam));

                    // Price filter
                    boolean matchesPrice = maxPrice == null ||
                            player.getNowCost() <= maxPrice;

                    return matchesSearch && matchesPosition && matchesTeam && matchesPrice;
                })
                .collect(Collectors.toList());

        displayPlayers(filtered);
    }

    /**
     * Display the players in the list.
     *
     * @param players List of players to display
     */
    private void displayPlayers(List<Player> players) {
        listModel.clear();
        for (Player player : players) {
            String line = String.format("%-20s %-15s %-12s %-9s %.1f pts",
                    player.getWebName(),
                    player. getTeamName() != null ? player. getTeamName() : "Unknown",
                    capitalizeFirst(player.getPosition()),
                    String.format("£%.1fm", player.getNowCost()),
                    player.getPredictedPoints() != null ? player.getPredictedPoints() : 0.0);
            listModel. addElement(line);
        }
        scrollToLeft();
    }

    /**
     * Capitalize the first letter of a string.
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Scroll the viewport to show the leftmost part of the list.
     */
    public void scrollToLeft() {
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            viewport.setViewPosition(new Point(0, viewport.getViewPosition(). y));
        });
    }

    /**
     * Set dimensions of the scroll pane.
     *
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public void setDimensions(int width, int height) {
        scrollPane. setPreferredSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height + 100));
    }

    // NEW: Method to handle when a player is clicked
    private void handlePlayerSelection() {
        int selectedIndex = playerList.getSelectedIndex();
        if (selectedIndex == -1 || playerSelectionListener == null) {
            return;
        }

        // Get the player name from the selected line
        String selectedLine = listModel.get(selectedIndex);

        // Extract player name (first 20 characters of formatted string)
        String playerName = selectedLine.substring(0, 20).trim();

        // Find the actual Player object from allPlayers
        Player selectedPlayer = allPlayers.stream()
                .filter(p -> p.getWebName().equals(playerName))
                .findFirst()
                .orElse(null);

        // Notify the parent view
        if (selectedPlayer != null && playerSelectionListener != null) {
            playerSelectionListener.accept(selectedPlayer);
        }
    }

    // NEW: Setter for the selection listener
    /**
     * Set a listener to be called when a player is selected from the list.
     *
     * @param listener Consumer that receives the selected Player
     */
    public void setPlayerSelectionListener(Consumer<Player> listener) {
        this.playerSelectionListener = listener;
    }


}