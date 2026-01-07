package si.um.feri.gasilci.services;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.util.HttpUtil;

public class RoutingService {
    // Simple in-memory LRU cache to avoid repeated API calls for same routes
    private final java.util.LinkedHashMap<String, List<LatLon>> cache =
            new java.util.LinkedHashMap<String, List<LatLon>>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, List<LatLon>> eldest) {
                    return size() > 100;
                }
            };
    public static class LatLon {
        public final double lat;
        public final double lon;
        public LatLon(double lat, double lon) { this.lat = lat; this.lon = lon; }
    }

    public List<LatLon> getRouteLatLon(double fromLat, double fromLon, double toLat, double toLon, String mode) throws Exception {
        String key = keyFor(fromLat, fromLon, toLat, toLon, mode);
        List<LatLon> cached = cache.get(key);
        if (cached != null) {
            return new ArrayList<>(cached);
        }
        String url = GeoapifyConfig.getRoutingUrl(fromLat, fromLon, toLat, toLon, mode);
        try (InputStream is = HttpUtil.getStream(url)) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<LatLon> parsed = parseRouteCoordinates(json);
            cache.put(key, new ArrayList<>(parsed));
            return parsed;
        }
    }

    private List<LatLon> parseRouteCoordinates(String json) {
        List<LatLon> result = new ArrayList<>();
        JsonValue root = new JsonReader().parse(json);
        JsonValue features = root.get("features");
        if (features == null || features.size == 0) return result;
        JsonValue geometry = features.get(0).get("geometry");
        if (geometry == null) return result;
        JsonValue coords = geometry.get("coordinates");
        if (coords == null) return result;
        String type = geometry.getString("type", "");
        if ("LineString".equals(type)) {
            for (JsonValue p = coords.child; p != null; p = p.next) {
                // p is [lon, lat, (optional alt)]
                double lon = p.get(0).asDouble();
                double lat = p.get(1).asDouble();
                result.add(new LatLon(lat, lon));
            }
        } else if ("MultiLineString".equals(type)) {
            for (JsonValue line = coords.child; line != null; line = line.next) {
                for (JsonValue p = line.child; p != null; p = p.next) {
                    double lon = p.get(0).asDouble();
                    double lat = p.get(1).asDouble();
                    result.add(new LatLon(lat, lon));
                }
            }
        } else {
            // Fallback: try to flatten first array level if present
            JsonValue first = coords.get(0);
            JsonValue lineArray = (first != null && first.isArray()) ? first : coords;
            for (JsonValue p = lineArray.child; p != null; p = p.next) {
                if (p.size >= 2) {
                    double lon = p.get(0).asDouble();
                    double lat = p.get(1).asDouble();
                    result.add(new LatLon(lat, lon));
                }
            }
        }
        return result;
    }

    private String keyFor(double aLat, double aLon, double bLat, double bLon, String mode) {
        return String.format(java.util.Locale.ROOT, "%s:%.5f,%.5f->%.5f,%.5f", mode, aLat, aLon, bLat, bLon);
    }
}
