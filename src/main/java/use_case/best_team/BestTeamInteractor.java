package use_case.best_team;

import entity.Player;
import entity.Team;
import use_case.PlayerDataAccessInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BestTeamInteractor implements BestTeamInputBoundary {
    private final PlayerDataAccessInterface playerDataAccess;
    private final BestTeamOutputBoundary presenter;

    private static final int POS_GK = 1;
    private static final int POS_DEF = 2;
    private static final int POS_MID = 3;
    private static final int POS_FWD = 4;

    private static final int GK_COUNT = 2;
    private static final int DEF_COUNT = 5;
    private static final int MID_COUNT = 5;
    private static final int FWD_COUNT = 3;

    private static final double BUDGET = 100.0;
    private static final int GK_MAX = 4;
    private static final int DEF_MAX = 7;
    private static final int MID_MAX = 7;
    private static final int FWD_MAX = 6;

    public BestTeamInteractor(PlayerDataAccessInterface playerDataAccess, BestTeamOutputBoundary presenter) {
        this.playerDataAccess = playerDataAccess;
        this.presenter = presenter;
    }

    @Override
    public void execute(BestTeamRequestModel requestModel) {

        // get all players with predictions
        List<Player> all = playerDataAccess.getAllPlayers().stream().filter(p -> p.getPredictedPoints() != null).collect(Collectors.toList());

        // start with the original per-position candidates limits
        int GK_LIMIT = GK_MAX;
        int DEF_LIMIT = DEF_MAX;
        int MID_LIMIT = MID_MAX;
        int FWD_LIMIT = FWD_MAX;

        // remember candidate list size
        // start at -1 so the loop always goes at least once
        int prevGKsize = -1;
        int prevDEFsize = -1;
        int prevMIDsize = -1;
        int prevFWDsize = -1;

        BestResult bestOverall = null;
        boolean found = false;

        // keep expanding the candidate pools until a squad is found or there are no more players to add to MAX
        while (true) {
            List<Player> gks = topByPosition(all, POS_GK, GK_LIMIT);
            List<Player> defs = topByPosition(all, POS_DEF, DEF_LIMIT);
            List<Player> mids = topByPosition(all, POS_MID, MID_LIMIT);
            List<Player> fwds = topByPosition(all, POS_FWD, FWD_LIMIT);

            // not enough players to fill slots and there is no possible valid squad
            if (gks.size() < GK_COUNT || defs.size() < DEF_COUNT ||  mids.size() < MID_COUNT || fwds.size() < FWD_COUNT) {
                break;
            }

            // increasing the limits no longer increases any list sizes
            // we've tried all possible candidates so stop (avoid infinite loop)
            if (gks.size() == prevGKsize || defs.size() == prevDEFsize ||  mids.size() == prevMIDsize || fwds.size() < prevFWDsize) {
                break;
            }

            BestResult bestThisRound = new BestResult();
            List<Player> current = new ArrayList<>();
            Map<String, Integer> teamCounts = new HashMap<>();

            searchGK(gks, 0, GK_COUNT, defs, mids, fwds, current, teamCounts, 0.0, 0.0, bestThisRound);

            if (bestThisRound.squad != null && bestThisRound.squad.size()==15) {
                bestOverall = bestThisRound;
                found = true;
                break;
            }

            // remember current sizes and expand candidate limits
            prevGKsize = gks.size();
            prevDEFsize = defs.size();
            prevMIDsize = mids.size();
            prevFWDsize = fwds.size();

            GK_LIMIT++;
            DEF_LIMIT++;
            MID_LIMIT++;
            FWD_LIMIT++;
        }

        // build response from best result
        // build response from best result
        List<Player> squad;
        double totalCost;
        double totalPoints;
        Team bestTeam;  // for visualization

        if (found && bestOverall != null) {
            squad = bestOverall.squad;
            totalCost = bestOverall.totalCost;
            totalPoints = bestOverall.totalPoints;

            bestTeam = buildTeamFromSquad(squad, totalCost);
        } else {
            squad = new ArrayList<>();
            totalCost = 0.0;
            totalPoints = 0.0;
            bestTeam = null;
        }

        BestTeamResponseModel response = new BestTeamResponseModel(
                bestTeam,
                squad,
                totalCost,
                totalPoints
        );
        presenter.present(response);

    }

    private Team buildTeamFromSquad(List<Player> squad, double totalCost) {
        // Remaining budget cannot be negative
        final float remainingBudget = (float) Math.max(0.0, BUDGET - totalCost);

        // Team should be marked confirmed only if it has all 15 players
        final boolean isConfirmed = squad.size() == 15;

        // Use a defensive copy of the list
        return new Team(new ArrayList<>(squad), remainingBudget, isConfirmed);
    }

    private List<Player> topByPosition(List<Player> all, int pos, int limit) {
        return all.stream().filter(p -> p.getElementType() == pos).sorted((a, b) -> Double.compare(b.getPredictedPoints(), a.getPredictedPoints())).limit(limit).collect(Collectors.toList());
    }

    // holds the best squad found so far so it won't be lost during backtracking
    private static class BestResult {
        List<Player> squad = null;
        double totalPoints = -1.0;
        double totalCost = 0.0;
    }

    private void searchGK(List<Player> gks, int index, int gkNeeded, List<Player> defs, List<Player> mids, List<Player> fwds, List<Player> current, Map<String, Integer> teamCounts, double costSoFar, double pointsSoFar, BestResult best) {
        if (costSoFar > BUDGET) {
            return;
        }
        if (gkNeeded == 0) {
            searchDEF(defs, 0, DEF_COUNT, mids, fwds, current, teamCounts, costSoFar, pointsSoFar, best);
            return;
        }
        if (index >= gks.size()) {
            return;
        }
        Player p = gks.get(index);

        searchGK(gks, index+1, gkNeeded, defs, mids, fwds, current, teamCounts, costSoFar, pointsSoFar, best);

        double cost = p.getNowCost();
        if (costSoFar + cost <= BUDGET && canAddPlayer(p, teamCounts)) {
            current.add(p);
            addTeamCount(p, teamCounts);
            searchGK(gks, index + 1, gkNeeded - 1, defs, mids, fwds, current, teamCounts, costSoFar + cost, pointsSoFar + safePoints(p), best);
            current.remove(current.size() - 1);
            removeTeamCount(p, teamCounts);
        }
    }

    private void searchDEF(List<Player> defs, int index, int defNeeded, List<Player> mids, List<Player> fwds, List<Player> current, Map<String, Integer> teamCounts, double costSoFar, double pointsSoFar, BestResult best) {
        if (costSoFar > BUDGET) {
            return;
        }
        if (defNeeded == 0) {
            searchMID(mids, 0, MID_COUNT, fwds, current, teamCounts, costSoFar, pointsSoFar, best);
            return;
        }
        if (index >= defs.size()) {return;}
        Player p = defs.get(index);

        searchDEF(defs, index + 1, defNeeded, mids, fwds, current, teamCounts, costSoFar, pointsSoFar, best);

        double cost = p.getNowCost();
        if (costSoFar + cost <= BUDGET && canAddPlayer(p, teamCounts)) {
            current.add(p);
            addTeamCount(p, teamCounts);
            searchDEF(defs, index + 1, defNeeded - 1, mids, fwds, current, teamCounts, costSoFar + cost, pointsSoFar + safePoints(p), best);
            current.remove(current.size() - 1);
            removeTeamCount(p, teamCounts);
        }
    }

    private void searchMID(List<Player> mids, int index, int midNeeded, List<Player> fwds, List<Player> current, Map<String, Integer> teamCounts, double costSoFar, double pointsSoFar, BestResult best) {
        if (costSoFar > BUDGET) {
            return;
        }
        if (midNeeded == 0) {
            searchFWD(fwds, 0, FWD_COUNT, current, teamCounts, costSoFar, pointsSoFar, best);
            return;
        }
        if (index >= mids.size()) {
            return;
        }
        Player p = mids.get(index);

        searchMID(mids, index + 1, midNeeded, fwds, current, teamCounts, costSoFar, pointsSoFar, best);

        double cost = p.getNowCost();
        if (costSoFar + cost <= BUDGET && canAddPlayer(p, teamCounts)) {
            current.add(p);
            addTeamCount(p, teamCounts);
            searchMID(mids, index + 1, midNeeded - 1, fwds, current, teamCounts, costSoFar + cost, pointsSoFar + safePoints(p), best);
            current.remove(current.size() - 1);
            removeTeamCount(p, teamCounts);
        }
    }

    private void searchFWD(List<Player> fwds, int index, int fwdNeeded, List<Player> current, Map<String, Integer> teamCounts, double costSoFar, double pointsSoFar, BestResult best) {
        if (costSoFar > BUDGET) {
            return;
        }
        if (fwdNeeded == 0) {
            // full squad chosen
            if (pointsSoFar > best.totalPoints) {
                best.totalPoints = pointsSoFar;
                best.totalCost = costSoFar;
                best.squad = new ArrayList<>(current);
            }
            return;
        }
        if (index >= fwds.size()) {return;}
        Player p = fwds.get(index);

        searchFWD(fwds, index + 1, fwdNeeded, current, teamCounts, costSoFar, pointsSoFar, best);

        double cost = p.getNowCost();
        if (costSoFar + cost <= BUDGET && canAddPlayer(p, teamCounts)) {
            current.add(p);
            addTeamCount(p, teamCounts);

            searchFWD(fwds, index + 1, fwdNeeded - 1, current, teamCounts, costSoFar + cost, pointsSoFar + safePoints(p), best);
            current.remove(current.size() - 1);
            removeTeamCount(p, teamCounts);
        }
    }

    private boolean canAddPlayer(Player p, Map<String, Integer> teamCounts) {
        String teamName = p.getTeamName();
        int count = teamCounts.getOrDefault(teamName, 0);
        return count < 3;
    }

    private void addTeamCount(Player p, Map<String, Integer> teamCounts) {
        String teamName = p.getTeamName();
        teamCounts.put(teamName, teamCounts.getOrDefault(teamName, 0) + 1);
    }

    private void removeTeamCount(Player p, Map<String, Integer> teamCounts) {
        String teamName = p.getTeamName();
        int count =  teamCounts.getOrDefault(teamName, 0);
        if (count <= 1) {
            teamCounts.remove(teamName);
        } else {
            teamCounts.put(teamName, count - 1);
        }
    }

    private double safePoints(Player p) {
        if (p.getPredictedPoints() == null) {
            return 0.0;
        } else {
            return p.getPredictedPoints();
        }
    }
}