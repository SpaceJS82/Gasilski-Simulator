package si.um.feri.gasilci.data;

import com.badlogic.gdx.audio.Sound;

public class FirePoint extends PointsLoader.Point {
    public final int severity; // 1-3
    public final String accessibility; // "good", "medium", "poor"
    public final float duration; // minutes
    private boolean active;
    private final int requiredTrucks; // Number of trucks needed
    private int assignedTrucks; // Number of trucks currently assigned
    private float extinguishTime; // Time in seconds to extinguish
    private float elapsedExtinguishTime; // Time spent extinguishing
    private Sound fireAmbientSound;
    private long fireAmbientSoundId = -1;

    public FirePoint(String id, String name, double lat, double lon, int severity, String accessibility) {
        super(id, name, lat, lon);
        this.severity = Math.max(1, Math.min(3, severity));
        this.accessibility = accessibility;
        this.duration = calculateDuration(this.severity);
        this.active = true;
        this.requiredTrucks = calculateRequiredTrucks(this.severity);
        this.assignedTrucks = 0;
        this.extinguishTime = 0;
        this.elapsedExtinguishTime = 0;
    }

    private int calculateRequiredTrucks(int sev) {
        // Severity 1: 1-2 trucks, Severity 2: 2-3 trucks, Severity 3: 3-4 trucks
        return sev + (Math.random() < 0.5 ? 0 : 1);
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

    public void setFireAmbientSound(Sound sound) {
        this.fireAmbientSound = sound;
    }

    public void startFireSound() {
        // Start playing the fire sound during extinguishing
        if (fireAmbientSound != null && fireAmbientSoundId == -1) {
            fireAmbientSoundId = fireAmbientSound.loop(0.6f); // 60% volume, looping
        }
    }

    public void stopFireSound() {
        // Stop fire ambient sound
        if (fireAmbientSound != null && fireAmbientSoundId != -1) {
            fireAmbientSound.stop(fireAmbientSoundId);
            fireAmbientSoundId = -1;
        }
    }

    public void putOut() {
        this.active = false;
        stopFireSound();
    }

    public void dispose() {
        // Stop fire sound if still playing
        if (fireAmbientSound != null && fireAmbientSoundId != -1) {
            fireAmbientSound.stop(fireAmbientSoundId);
            fireAmbientSoundId = -1;
        }
    }

    public String getLocation() {
        return String.format("%.4f, %.4f", lat, lon);
    }

    public int getRequiredTrucks() {
        return requiredTrucks;
    }

    public int getAssignedTrucks() {
        return assignedTrucks;
    }

    public int getRemainingTrucksNeeded() {
        return Math.max(0, requiredTrucks - assignedTrucks);
    }

    public void addAssignedTrucks(int numTrucks) {
        this.assignedTrucks += numTrucks;
        // Recalculate extinguish time based on total assigned trucks
        if (assignedTrucks > 0) {
            float baseTime = 30f; // seconds
            float optimalTrucks = (float) requiredTrucks;
            float truckRatio = Math.max(0.2f, (float) assignedTrucks / optimalTrucks);
            this.extinguishTime = baseTime / truckRatio;
        }
    }

    public void assignTrucks(int numTrucks) {
        this.assignedTrucks = numTrucks;
        // Base extinguish time: 30 seconds for full required trucks
        // If we send more or less, adjust accordingly
        float baseTime = 30f; // seconds
        float optimalTrucks = (float) requiredTrucks;
        float truckRatio = Math.max(0.2f, (float) numTrucks / optimalTrucks);
        this.extinguishTime = baseTime / truckRatio;
        this.elapsedExtinguishTime = 0;
    }

    public void resetAssignedTrucks() {
        this.assignedTrucks = 0;
        this.extinguishTime = 0;
        this.elapsedExtinguishTime = 0;
    }

    public boolean updateExtinguishing(float deltaTime) {
        if (assignedTrucks == 0) return false;
        
        elapsedExtinguishTime += deltaTime;
        
        return elapsedExtinguishTime >= extinguishTime; // Extinguishing complete
    }

    public float getExtinguishProgress() {
        if (extinguishTime <= 0) return 0;
        return Math.min(1.0f, elapsedExtinguishTime / extinguishTime);
    }

    public float getRemainingExtinguishTime() {
        return Math.max(0, extinguishTime - elapsedExtinguishTime);
    }
}
