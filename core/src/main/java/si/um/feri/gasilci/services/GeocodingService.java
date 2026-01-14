package si.um.feri.gasilci.services;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.util.HttpUtil;

public class GeocodingService {
    private static final String REVERSE_GEOCODE_URL = "https://api.geoapify.com/v1/geocode/reverse";

    public String getAddress(double lat, double lon) {
        try {
            String url = String.format("%s?lat=%f&lon=%f&apiKey=%s",
                REVERSE_GEOCODE_URL, lat, lon, GeoapifyConfig.getApiKey());
            try (InputStream is = HttpUtil.getStream(url)) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return parseAddress(json);
            }
        } catch (Exception e) {
            return "Unknown location";
        }
    }

    private String parseAddress(String json) {
        JsonValue root = new JsonReader().parse(json);
        JsonValue features = root.get("features");
        if (features != null && features.size > 0) {
            JsonValue props = features.get(0).get("properties");
            if (props != null) {
                return props.getString("formatted", "Unknown address");
            }
        }
        return "Unknown address";
    }
}

