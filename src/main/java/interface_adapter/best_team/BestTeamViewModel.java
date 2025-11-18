package interface_adapter.best_team;

import entity.Player;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;

public class BestTeamViewModel {
    public static final String VIEW_TITLE = "Best Team";
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private List<Player> players = Collections.emptyList();
    private double totalCost;
    private double totalPredictedPoints;

    public void setPlayers(List<Player> players){
        this.players = players;
        support.firePropertyChange("players", null, players);
    }

    public void setTotalCost(double totalCost){
        this.totalCost = totalCost;
        support.firePropertyChange("totalCost", null, totalCost);
    }

    public void setTotalPredictedPoints(double totalPredictedPoints){
        this.totalPredictedPoints = totalPredictedPoints;
        support.firePropertyChange("totalPredictedPoints", null, totalPredictedPoints);
    }

    public List<Player> getPlayers(){return players;}
    public double getTotalCost(){return totalCost;}
    public double getTotalPredictedPoints(){return totalPredictedPoints;}
    public void addPropertyChangeListener(PropertyChangeListener listener){
        support.addPropertyChangeListener(listener);
    }
}
