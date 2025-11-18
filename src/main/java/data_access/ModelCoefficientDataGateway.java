package data_access;

import org.json.JSONObject;
import use_case.initialise_predictions.ModelCoefficientDataAccessInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway for loading model coefficients from JSON file.
 * Reads the model_coefficients.json file containing Ridge Regression coefficients.
 */
public class ModelCoefficientDataGateway implements ModelCoefficientDataAccessInterface {
    private final String filePath;
    private Map<Integer, Map<String, Double>> cachedCoefficients;

    /**
     * Create gateway with default file path.
     */
    public ModelCoefficientDataGateway() {
        this("model_coefficients.json");
    }

    /**
     * Create gateway with custom file path.
     *
     * @param filePath Path to the coefficients JSON file
     */
    public ModelCoefficientDataGateway(String filePath) {
        this.filePath = filePath;
        this.cachedCoefficients = null;
    }

    @Override
    public Map<String, Double> getCoefficients(int position) throws IOException {
        if (position < 1 || position > 4) {
            throw new IllegalArgumentException("Position must be between 1 and 4");
        }

        if (cachedCoefficients == null) {
            loadCoefficients();
        }

        return cachedCoefficients.get(position);
    }

    @Override
    public Map<Integer, Map<String, Double>> getAllCoefficients() throws IOException {
        if (cachedCoefficients == null) {
            loadCoefficients();
        }

        return new HashMap<>(cachedCoefficients);
    }

    /**
     * Load and parse coefficients from JSON file.
     * First tries to load from classpath resources, then falls back to file system.
     */
    private void loadCoefficients() throws IOException {
        String content;

        // Try loading from classpath first (for resources folder)
        try (java.io.InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(filePath)) {

            if (inputStream != null) {
                // Found in classpath - read it
                content = new String(inputStream.readAllBytes());
            } else {
                // Not in classpath - try file system
                content = new String(Files.readAllBytes(Paths.get(filePath)));
            }
        } catch (IOException e) {
            // If classpath failed, try file system as fallback
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        }

        // Parse JSON
        JSONObject root = new JSONObject(content);
        cachedCoefficients = new HashMap<>();

        // Parse each position (1-4)
        for (int position = 1; position <= 4; position++) {
            String positionKey = String.valueOf(position);
            if (root.has(positionKey)) {
                JSONObject positionData = root.getJSONObject(positionKey);
                JSONObject coefficients = positionData.getJSONObject("coefficients");

                Map<String, Double> coeffMap = new HashMap<>();
                for (String key : coefficients.keySet()) {
                    coeffMap.put(key, coefficients.getDouble(key));
                }

                cachedCoefficients.put(position, coeffMap);
            }
        }
    }
}