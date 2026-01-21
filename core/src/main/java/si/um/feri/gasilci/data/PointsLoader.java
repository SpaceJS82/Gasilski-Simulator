package si.um.feri.gasilci.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class PointsLoader {
    public static class Point {
        public final String id;
        public final String name;
        public final double lat;
        public final double lon;

        public Point(String id, String name, double lat, double lon) {
            this.id = id;
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static List<FirePoint> loadFires(String internalPath) {
        FileHandle file = Gdx.files.internal(internalPath);
        JsonValue root = new JsonReader().parse(file);
        List<FirePoint> result = new ArrayList<>();

        for (JsonValue fire : root.get("fires")) {
            String id = fire.getString("id");
            String name = fire.getString("name", id);
            double lat = fire.getDouble("lat");
            double lon = fire.getDouble("lon");
            int severity = fire.getInt("severity", 1);
            String accessibility = fire.getString("accessibility", "good");
            result.add(new FirePoint(id, name, lat, lon, severity, accessibility));
        }
        return result;
    }

    public static FireStation loadStation(String internalPath) {
        FileHandle file = Gdx.files.internal(internalPath);
        JsonValue root = new JsonReader().parse(file);
        JsonValue st = root.get("station");
        if (st != null) {
            // Old format - single station
            String id = st.getString("id");
            String name = st.getString("name", id);
            double lat = st.getDouble("lat");
            double lon = st.getDouble("lon");
            int totalTrucks = st.getInt("totalTrucks", 5);
            return new FireStation(id, name, lat, lon, totalTrucks);
        }
        // New format - first station from array
        JsonValue stations = root.get("stations");
        if (stations != null && stations.size > 0) {
            JsonValue first = stations.get(0);
            String id = first.getString("id");
            String name = first.getString("name", id);
            double lat = first.getDouble("lat");
            double lon = first.getDouble("lon");
            int totalTrucks = first.getInt("totalTrucks", 5);
            return new FireStation(id, name, lat, lon, totalTrucks);
        }
        throw new RuntimeException("No station found in " + internalPath);
    }

    public static List<FireStation> loadStations(String internalPath) {
        FileHandle file = Gdx.files.internal(internalPath);
        JsonValue root = new JsonReader().parse(file);
        List<FireStation> result = new ArrayList<>();
        
        JsonValue stations = root.get("stations");
        if (stations != null) {
            for (JsonValue st : stations) {
                String id = st.getString("id");
                String name = st.getString("name", id);
                double lat = st.getDouble("lat");
                double lon = st.getDouble("lon");
                int totalTrucks = st.getInt("totalTrucks", 5);
                result.add(new FireStation(id, name, lat, lon, totalTrucks));
            }
        }
        return result;
    }

    public static FireStation findNearestStation(List<FireStation> stations, double targetLat, double targetLon) {
        if (stations.isEmpty()) {
            throw new RuntimeException("No stations available");
        }
        FireStation nearest = stations.get(0);
        double minDistance = distance(nearest.lat, nearest.lon, targetLat, targetLon);
        
        for (FireStation station : stations) {
            double dist = distance(station.lat, station.lon, targetLat, targetLon);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = station;
            }
        }
        return nearest;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double dx = lat2 - lat1;
        double dy = lon2 - lon1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static List<FirePoint> filterNearbyFires(List<FirePoint> allFires, double cityLat, double cityLon, double maxDistance) {
        List<FirePoint> nearby = new ArrayList<>();
        for (FirePoint fire : allFires) {
            double dist = distance(fire.lat, fire.lon, cityLat, cityLon);
            if (dist <= maxDistance) {
                nearby.add(fire);
            }
        }
        return nearby;
    }

    public static List<FireStation> filterNearbyStations(List<FireStation> allStations, double cityLat, double cityLon, double maxDistance) {
        List<FireStation> nearby = new ArrayList<>();
        for (FireStation station : allStations) {
            double dist = distance(station.lat, station.lon, cityLat, cityLon);
            if (dist <= maxDistance) {
                nearby.add(station);
            }
        }
        return nearby;
    }

    // WIP
    public static List<FirePoint> pickRandom(List<FirePoint> points, int count) {
        if (points.size() <= count) return points;
        List<FirePoint> copy = new ArrayList<>(points);
        Collections.shuffle(copy);
        return new ArrayList<>(copy.subList(0, count));
    }

    public static FireStation pickRandomStation(List<FireStation> stations) {
        if (stations.isEmpty()) {
            throw new RuntimeException("No stations available");
        }
        if (stations.size() == 1) {
            return stations.get(0);
        }
        List<FireStation> copy = new ArrayList<>(stations);
        Collections.shuffle(copy);
        return copy.get(0);
    }
}
