package si.um.feri.gasilci.data;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import si.um.feri.gasilci.services.CityService;

public class CityManager {
    private static final String PREFS_NAME = "GasilskiSimulatorPrefs";
    private static final String KEY_CITIES = "cities";
    private static final String KEY_SELECTED_CITY = "selectedCity";

    private Preferences prefs;
    private List<CityData> cities;
    private CityService cityService;

    public CityManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        cityService = new CityService();
        cities = new ArrayList<>();
    }

    public List<CityData> getCities() {
        if (cities.isEmpty()) {
            loadCities();
        }
        return cities;
    }

    private void loadCities() {
        String citiesJson = prefs.getString(KEY_CITIES, null);

        if (citiesJson == null || citiesJson.isEmpty()) {
            // First time running - fetch from API
            System.out.println("First run detected. Fetching cities from API...");
            fetchAndSaveCities();
        } else {
            // Load from preferences
            System.out.println("Loading cities from preferences...");
            cities = parseCitiesFromJson(citiesJson);

            if (cities.isEmpty()) {
                // If parsing failed, fetch from API
                fetchAndSaveCities();
            }
        }
    }

    private void fetchAndSaveCities() {
        cities = cityService.fetchSlovenianCities();
        saveCities();
    }

    private void saveCities() {
        if (cities.isEmpty()) {
            return;
        }

        // Convert cities to JSON string
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < cities.size(); i++) {
            CityData city = cities.get(i);
            json.append("{");
            json.append("\"name\":\"").append(city.name).append("\",");
            json.append("\"lat\":").append(city.lat).append(",");
            json.append("\"lon\":").append(city.lon);
            json.append("}");

            if (i < cities.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        prefs.putString(KEY_CITIES, json.toString());
        prefs.flush();
        System.out.println("Saved " + cities.size() + " cities to preferences");
    }

    private List<CityData> parseCitiesFromJson(String json) {
        List<CityData> result = new ArrayList<>();

        try {
            JsonValue root = new JsonReader().parse(json);

            for (JsonValue cityJson : root) {
                String name = cityJson.getString("name");
                double lat = cityJson.getDouble("lat");
                double lon = cityJson.getDouble("lon");

                result.add(new CityData(name, lat, lon));
            }
        } catch (Exception e) {
            System.err.println("Failed to parse cities from preferences: " + e.getMessage());
        }

        return result;
    }

    public void setSelectedCity(String cityName) {
        prefs.putString(KEY_SELECTED_CITY, cityName);
        prefs.flush();
    }

    public String getSelectedCity() {
        return prefs.getString(KEY_SELECTED_CITY, "Maribor");
    }

    public CityData getCityByName(String name) {
        for (CityData city : getCities()) {
            if (city.name.equals(name)) {
                return city;
            }
        }
        // Default to Maribor if not found
        return new CityData("Maribor", 46.5547, 15.6459);
    }
}
