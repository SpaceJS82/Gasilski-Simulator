package si.um.feri.gasilci.data;

public class FirePoint extends PointsLoader.Point {
    public final int severity; // 1-3
    public final String accessibility; // "good", "medium", "poor"
    public final float duration; // minutes
    private boolean active;

    public FirePoint(String id, String name, double lat, double lon, int severity, String accessibility) {
        super(id, name, lat, lon);
        this.severity = Math.max(1, Math.min(3, severity));
        this.accessibility = accessibility;
        this.duration = calculateDuration(this.severity);
        this.active = true;
    }

    private float calculateDuration(int sev) {
        // Severity 1: 2-3 min, Severity 2: 3-4 min, Severity 3: 4-5 min
        float min = 2f + (sev - 1);
        float max = 3f + (sev - 1);
        return min + (float) (Math.random() * (max - min));
    }

    public boolean isActive() {
        return active;
    }

    public void putOut() {
        this.active = false;
    }

    public String getLocation() {
        return String.format("%.4f, %.4f", lat, lon);
    }
}
