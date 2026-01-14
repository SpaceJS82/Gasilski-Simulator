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

    public static Point loadStation(String internalPath) {
        FileHandle file = Gdx.files.internal(internalPath);
        JsonValue root = new JsonReader().parse(file);
        JsonValue st = root.get("station");
        String id = st.getString("id");
        String name = st.getString("name", id);
        double lat = st.getDouble("lat");
        double lon = st.getDouble("lon");
        return new Point(id, name, lat, lon);
    }

    // WIP
    public static List<FirePoint> pickRandom(List<FirePoint> points, int count) {
        if (points.size() <= count) return points;
        List<FirePoint> copy = new ArrayList<>(points);
        Collections.shuffle(copy);
        return new ArrayList<>(copy.subList(0, count));
    }
}
