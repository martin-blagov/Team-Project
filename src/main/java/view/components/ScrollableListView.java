package view.components;

import entity.Player;
import use_case.PlayerDataAccessInterface;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple scrollable list view for displaying players.
 * Just shows players sorted by predicted points - no filtering yet.
 */
public class ScrollableListView extends JPanel {
    private final PlayerDataAccessInterface playerDataAccess;
    private final DefaultListModel<String> listModel;
    private JScrollPane scrollPane; // ADD THIS LINE
    private final JList<String> playerList;
    private JTextField searchField;
    private List<Player> allPlayers;  // Store all players
    private JComboBox<String> positionFilter;
    private JComboBox<String> teamFilter;
    private JComboBox<String> priceFilter;
    private java.util.function.Consumer<Player> playerSelectionListener;

    public ScrollableListView(PlayerDataAccessInterface playerDataAccess) {
        this.playerDataAccess = playerDataAccess;
        this.allPlayers = new ArrayList<>();


        setLayout(new BorderLayout());

        // Create filter panel at the top
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));

        // Search bar (first row)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(30);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterPlayers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterPlayers(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterPlayers(); }
        });
        searchPanel.add(searchField);
        filterPanel.add(searchPanel);

        // Dropdown filters (second row)
        JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));

        // Position filter
        positionFilter = new JComboBox<>(new String[]{
                "All positions", "Goalkeeper", "Defender", "Midfielder", "Forward"
        });
        positionFilter.addActionListener(e -> filterPlayers());
        dropdownPanel.add(positionFilter);

        // Team filter
        teamFilter = new JComboBox<>();
        teamFilter.addItem("All teams");
        teamFilter.addActionListener(e -> filterPlayers());
        dropdownPanel.add(teamFilter);

        // Price filter
        priceFilter = new JComboBox<>();
        priceFilter.addItem("Any price");
        for (double price = 3.5; price <= 15.5; price += 0.5) {
            priceFilter.addItem(String.format("£%.1fm", price));
        }
        priceFilter.addActionListener(e -> filterPlayers());
        dropdownPanel.add(priceFilter);

        filterPanel.add(dropdownPanel);

        add(filterPanel, BorderLayout.NORTH);

        // Create the list model and JList
        listModel = new DefaultListModel<>();
        playerList = new JList<>(listModel);
        playerList.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // sample cell
        playerList.setFixedCellWidth(650);
        playerList.setFixedCellHeight(20);   // Set fixed height
        playerList.setVisibleRowCount(20);

        // Make it scrollable with border
        scrollPane = new JScrollPane(playerList);
        scrollPane.setPreferredSize(new Dimension(600, 400));
//        scrollPane.setMinimumSize(new Dimension(400, 300));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        scrollPane.setMinimumSize(new Dimension(600, 400));
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
        add(scrollPane, BorderLayout.CENTER);

        // Load data when component is shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refresh();
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> scrollToLeft());
            }
        });

        // Add Click Selection Listener
        playerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && playerList.getSelectedIndex() != -1) {
                handlePlayerSelection();
            }
        });
    }

    public void refresh() {
        //System.out.println("refresh() called");

        // Get all players from data access and STORE them
        allPlayers = playerDataAccess.getAllPlayers();  // CHANGED: store in allPlayers
        //System.out.println("Number of players loaded: " + allPlayers.size());

        // Sort by predicted points (highest first)
        allPlayers.sort((a, b) -> {
            if (a.getPredictedPoints() == null) return 1;
            if (b.getPredictedPoints() == null) return -1;
            return Double.compare(b.getPredictedPoints(), a.getPredictedPoints());
        });
        // Populate team filter with unique teams
        teamFilter.removeAllItems();
        teamFilter.addItem("All teams");
        allPlayers.stream()
                .map(Player::getTeamName)
                .filter(team -> team != null)
                .distinct()
                .sorted()
                .forEach(teamFilter::addItem);

        // Instead of directly displaying, call filterPlayers
        filterPlayers();  // CHANGED: this will display all initially (empty search)
        scrollToLeft();

    }

    private void displayPlayers(List<Player> players) {
        listModel.clear();
        for (Player player : players) {
            String line = String.format("%-20s %-15s %-12s £%.1fm  %.1f pts",
                    player.getWebName(),
                    player.getTeamName() != null ? player.getTeamName() : "Unknown",
                    capitalizeFirst(player.getPosition()),  // ADD POSITION
                    player.getNowCost(),
                    player.getPredictedPoints() != null ? player.getPredictedPoints() : 0.0);
            listModel.addElement(line);
        }
        System.out.println("Displayed " + players.size() + " players");
        scrollToLeft();

    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void filterPlayers() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedPosition = (String) positionFilter.getSelectedItem();
        String selectedTeam = (String) teamFilter.getSelectedItem();
        String selectedPrice = (String) priceFilter.getSelectedItem();

        // If any selection is null, skip filtering (happens during refresh)
        if (selectedPosition == null || selectedTeam == null || selectedPrice == null) {
            return;
        }
        // Extract max price
        Double maxPrice = null;
        if (selectedPrice != null && !selectedPrice.equals("Any price")) {
            String priceStr = selectedPrice.replace("£", "").replace("m", "");
            maxPrice = Double.parseDouble(priceStr);
        }

        final Double finalMaxPrice = maxPrice;

        // Apply all filters
        List<Player> filtered = allPlayers.stream()
                .filter(player -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty() ||
                            player.getWebName().toLowerCase().contains(searchText);

                    // Position filter
                    boolean matchesPosition = selectedPosition.equals("All positions") ||
                            player.getPosition().equalsIgnoreCase(selectedPosition);

                    // Team filter
                    boolean matchesTeam = selectedTeam.equals("All teams") ||
                            (player.getTeamName() != null && player.getTeamName().equals(selectedTeam));

                    // Price filter
                    boolean matchesPrice = finalMaxPrice == null ||
                            player.getNowCost() <= finalMaxPrice;

                    return matchesSearch && matchesPosition && matchesTeam && matchesPrice;
                })
                .collect(Collectors.toList());

        displayPlayers(filtered);
    }

    public void setPlayerSelectionListener(java.util.function.Consumer<Player> listener) {
        this.playerSelectionListener = listener;
    }

    private void handlePlayerSelection() {
        int selectedIndex = playerList.getSelectedIndex();
        if (selectedIndex == -1 || playerSelectionListener == null) {
            return;
        }

        // Get the player name from the selected line
        String selectedLine = listModel.get(selectedIndex);
        String playerName = selectedLine.substring(0, 20).trim(); // Extract name from formatted string

        // Find the actual Player object
        Player selectedPlayer = allPlayers.stream()
                .filter(p -> p.getWebName().equals(playerName))
                .findFirst()
                .orElse(null);

        if (selectedPlayer != null && playerSelectionListener != null) {
            playerSelectionListener.accept(selectedPlayer);
        }
    }

    public void setDimensions(int width, int height) {
        scrollPane.setPreferredSize(new Dimension(width, height));

        // Prevent the entire panel from growing
        setPreferredSize(new Dimension(width, height + 100));
//        setMaximumSize(new Dimension(width, height + 100));
//        setMinimumSize(new Dimension(width, height + 100));
    }

    /**
     * Scroll the viewport to show the leftmost part of the list
     */
    public void scrollToLeft() {
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            viewport.setViewPosition(new Point(0, viewport.getViewPosition().y));
        });
    }

}