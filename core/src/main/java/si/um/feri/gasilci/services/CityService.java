package si.um.feri.gasilci.services;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.data.CityData;
import si.um.feri.gasilci.util.HttpUtil;

public class CityService {
    private static final String GEOCODING_SEARCH_URL = "https://api.geoapify.com/v1/geocode/search";

    public List<CityData> fetchSlovenianCities() {
        List<CityData> cities = new ArrayList<>();

        try {
            // Fetch cities from Geoapify
            String url = String.format("%s?text=Slovenia&type=city&limit=50&apiKey=%s",
                GEOCODING_SEARCH_URL, GeoapifyConfig.getApiKey());

            try (InputStream is = HttpUtil.getStream(url)) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                cities = parseCities(json);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch cities: " + e.getMessage());
            // Return fallback cities if API fails
            cities = getFallbackCities();
        }

        return cities;
    }

    private List<CityData> parseCities(String json) {
        List<CityData> cities = new ArrayList<>();

        try {
            JsonValue root = new JsonReader().parse(json);
            JsonValue features = root.get("features");

            if (features != null) {
                for (JsonValue feature : features) {
                    JsonValue properties = feature.get("properties");
                    JsonValue geometry = feature.get("geometry");

                    if (properties != null && geometry != null) {
                        String city = properties.getString("city", null);
                        if (city == null) {
                            city = properties.getString("name", null);
                        }

                        if (city != null && !city.isEmpty()) {
                            JsonValue coordinates = geometry.get("coordinates");
                            if (coordinates != null && coordinates.size >= 2) {
                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

                                // Check if city already exists in list
                                boolean exists = false;
                                for (CityData existing : cities) {
                                    if (existing.name.equals(city)) {
                                        exists = true;
                                        break;
                                    }
                                }

                                if (!exists) {
                                    String normalizedCity = normalizeCityName(city);
                                    cities.add(new CityData(normalizedCity, lat, lon));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse cities: " + e.getMessage());
        }

        // If we got few or no cities, add fallback
        if (cities.size() < 5) {
            cities = getFallbackCities();
        }

        return cities;
    }

    private List<CityData> getFallbackCities() {
        List<CityData> cities = new ArrayList<>();
        cities.add(new CityData("Ljubljana", 46.0569, 14.5058));
        cities.add(new CityData("Maribor", 46.5547, 15.6459));
        cities.add(new CityData("Celje", 46.2397, 15.2677));
        cities.add(new CityData("Kranj", 46.2384, 14.3555));
        cities.add(new CityData("Velenje", 46.3592, 15.1116));
        cities.add(new CityData("Koper", 45.5469, 13.7301));
        cities.add(new CityData("Novo Mesto", 45.8042, 15.1695));
        cities.add(new CityData("Ptuj", 46.4206, 15.8697));
        cities.add(new CityData("Kamnik", 46.2258, 14.6113));
        cities.add(new CityData("Jesenice", 46.4297, 14.0528));
        cities.add(new CityData("Domzale", 46.1378, 14.5964));
        cities.add(new CityData("Skofja Loka", 46.1664, 14.3069));
        cities.add(new CityData("Nova Gorica", 45.9564, 13.6478));
        cities.add(new CityData("Murska Sobota", 46.6622, 16.1664));
        cities.add(new CityData("Slovenj Gradec", 46.5103, 15.0808));
        return cities;
    }

    private String normalizeCityName(String name) {
        if (name == null) {
            return null;
        }
        return name.replace("č", "c")
                   .replace("Č", "C")
                   .replace("š", "s")
                   .replace("Š", "S")
                   .replace("ž", "z")
                   .replace("Ž", "Z");
    }
}
