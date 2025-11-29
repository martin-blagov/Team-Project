package data_access;

import entity.Player;
import entity.Team;
import use_case.risk_assessment.RiskAssessmentTeamAccessInterface;
import use_case.team_entry.TeamDataAccessInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for saving/loading the user's confirmed FPL team to a local JSON file.
 */
public class FileTeamDataAccessObject implements TeamDataAccessInterface, RiskAssessmentTeamAccessInterface {

    private final String filepath;
    private Team savedTeam;

    public FileTeamDataAccessObject(String filepath) {
        this.filepath = filepath;
        System.out.println("Team JSON file path: " + new File(filepath).getAbsolutePath());
        loadTeamFromFile();
    }

    // Saves the team to the json file
    @Override
    public void saveTeam(Team team) {
        savedTeam = team;

        JSONObject json = new JSONObject();
        JSONArray playersArray = new JSONArray();

        for (Player p : team.getPlayers()) {
            JSONObject obj = new JSONObject();
            obj.put("id", p.getId());
            obj.put("webName", p.getWebName());
            obj.put("elementType", p.getElementType());
            obj.put("status", p.getStatus());
            obj.put("nowCost", p.getNowCost());
            obj.put("position", p.getPosition());
            obj.put("team", p.getTeamName());

            // Stats maps
            obj.put("seasonTotalStats", new JSONObject(p.getAllSeasonTotalStats()));
            obj.put("seasonAvgStats", new JSONObject(p.getAllSeasonAvgStats()));
            obj.put("last3Stats", new JSONObject(p.getAllLast3Stats()));
            obj.put("last5Stats", new JSONObject(p.getAllLast5Stats()));

            playersArray.put(obj);
        }

        json.put("players", playersArray);
        json.put("budget", team.getBudget());
        json.put("confirmed", team.isConfirmed());

        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(json.toString(4)); // Readable JSON data
        }
        catch (IOException ex) {
            throw new RuntimeException("Failed to save team JSON file.", ex);
        }
    }

    // Gets team from JSON file
    @Override
    public Team getTeam() {
        return savedTeam;
    }

    // Resets the JSON file
    @Override
    public void clearTeam() {
        savedTeam = null;

        File f = new File(filepath);
        if (f.exists()) f.delete();
    }

    // Load the saved team from the JSON file
    private void loadTeamFromFile() {
        File f = new File(filepath);
        if (!f.exists()) {
            savedTeam = null;
            return;
        }

        try {
            String content = readFile(filepath);
            JSONObject json = new JSONObject(content);

            JSONArray array = json.getJSONArray("players");
            List<Player> players = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject p = array.getJSONObject(i);

                int id = p.getInt("id");
                String webName = p.getString("webName");
                int elementType = p.getInt("elementType");
                String status = p.getString("status");
                double nowCost = p.getDouble("nowCost");
                int position = convertPositionToInt(p.getString("position"));
                String teamName = p.getString("team");

                Map<String, Double> seasonTotal = jsonToMap(p.getJSONObject("seasonTotalStats"));
                Map<String, Double> seasonAvg = jsonToMap(p.getJSONObject("seasonAvgStats"));
                Map<String, Double> last3 = jsonToMap(p.getJSONObject("last3Stats"));
                Map<String, Double> last5 = jsonToMap(p.getJSONObject("last5Stats"));

                Player player = new Player(
                        id,
                        webName,
                        elementType,
                        status,
                        nowCost,
                        position,
                        teamName,
                        seasonTotal,
                        seasonAvg,
                        last3,
                        last5
                );

                players.add(player);
            }

            float budget = (float) json.getDouble("budget");
            boolean confirmed = json.getBoolean("confirmed");

            savedTeam = new Team(players, budget, confirmed);

        }
        catch (IOException | JSONException ex) { // If there's an issue with getting the JSON file
            throw new RuntimeException("Could not load team JSON file.", ex);
        }
    }

    private Map<String, Double> jsonToMap(JSONObject obj) {
        Map<String, Double> map = new HashMap<>();
        for (String key : obj.keySet()) {
            map.put(key, obj.getDouble(key));
        }
        return map;
    }

    private int convertPositionToInt(String pos) {
        switch (pos.toLowerCase()) {
            case "goalkeeper": return 1;
            case "defender":   return 2;
            case "midfielder": return 3;
            case "forward":    return 4;
            default: return -1;
        }
    }

    private String readFile(String path) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
