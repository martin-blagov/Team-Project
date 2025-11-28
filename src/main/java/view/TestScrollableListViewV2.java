package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.test_display_players.TestDisplayPlayersController;
import interface_adapter.test_display_players.TestDisplayPlayersState;
import interface_adapter.test_display_players.TestDisplayPlayersViewModel;
import view.components.ScrollableListViewV2;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * V2 - REFACTORED Test Scrollable List View (SMART VIEW)
 *
 * This version follows Clean Architecture:
 * - Has ViewModel (observes it for changes)
 * - Has Controller (calls it when user acts)
 * - Uses ScrollableListViewV2 (dumb component)
 * - NO direct data access
 *
 * This is a DEMO/TEMPLATE to show teammates how to wire up a use case properly.
 *
 * FLOW:
 * 1. User changes filter in ScrollableListViewV2
 * 2. ScrollableListViewV2 calls callback → THIS view
 * 3. THIS view calls Controller
 * 4. Controller → Interactor → Presenter → ViewModel
 * 5. ViewModel fires property change → THIS view
 * 6. THIS view updates ScrollableListViewV2 with new data
 */
public class TestScrollableListViewV2 extends JPanel implements PropertyChangeListener {

    private final String viewName = "test scrollable list v2";

    // Clean Architecture components
    private final TestDisplayPlayersViewModel viewModel;
    private TestDisplayPlayersController controller;

    // UI components
    private final ScrollableListViewV2 scrollableListView;
    private final JLabel statusLabel;
    private final ViewManagerModel viewManagerModel;
    private java.util.function.Consumer<entity.Player> onPlayerSelected;


    /**
     * Constructor - Takes ViewModel (NOT data access!).
     *
     * The ViewModel is injected by AppBuilder.
     * The Controller will be injected later via setController().
     *
     * @param viewModel The ViewModel for this use case
     * @param viewManagerModel For navigation
     */
    public TestScrollableListViewV2(TestDisplayPlayersViewModel viewModel,
                                    ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        // Listen to ViewModel changes
        viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title panel at top
        JPanel titlePanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Test Display Players - Clean Architecture Demo");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titlePanel.add(statusLabel, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // Create the dumb ScrollableListView component (NO data access!)
        scrollableListView = new ScrollableListViewV2();

        // Wire up the callback - when filters change, call Controller
        scrollableListView.setOnFilterChange(criteria -> {
            if (controller != null) {
                // THIS is Clean Architecture!
                // View doesn't fetch data - it asks Controller to do it
                controller.filterPlayers(
                        criteria.searchText,
                        criteria.positionFilter,
                        criteria.teamFilter,
                        criteria.maxPrice
                );
                statusLabel.setText("Filtering...");
            }
        });

        // Setting up how the list is displayed
        scrollableListView.setDimensions(550,500);
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapperPanel.add(scrollableListView);
        add(wrapperPanel, BorderLayout.CENTER);

        // Button panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> {
            viewManagerModel.setState("home");
            viewManagerModel.firePropertyChange();
        });

        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load data when component is actually shown (not when constructed)
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (controller != null) {
                    controller.loadAllPlayers();
                    statusLabel.setText("Loading players...");
                }
            }
        });
    }

    /**
     * Set the Controller.
     * Called by AppBuilder after wiring everything together.
     *
     * @param controller The Controller for this use case
     */
    public void setController(TestDisplayPlayersController controller) {
        this.controller = controller;
        // Don't load here - let componentShown() do it when view is actually displayed
    }

    /**
     * Property change listener - called when ViewModel changes.
     *
     * This is the OBSERVER PATTERN in action.
     * When the Presenter updates the ViewModel, we get notified here.
     *
     * @param evt Property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Get the updated state from ViewModel
        TestDisplayPlayersState state = viewModel.getState();

        // Check for errors
        if (state.getErrorMessage() != null) {
            statusLabel.setText("Error: " + state.getErrorMessage());
            JOptionPane.showMessageDialog(this,
                    state.getErrorMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update the ScrollableListView with new data
        // (Passing clean data - NOT fetching it ourselves!)
        scrollableListView.setPlayers(
                state.getPlayers(),
                state.getAvailableTeams()
        );

        // Update status label
        int playerCount = state.getPlayers().size();
        statusLabel.setText(playerCount + " players loaded");
    }

    public String getViewName() {
        return viewName;
    }

    public void setOnPlayerSelected(java.util.function.Consumer<entity.Player> listener) {
        this.onPlayerSelected = listener;
    }

}