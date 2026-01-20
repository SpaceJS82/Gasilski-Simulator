package si.um.feri.gasilci.data;

public class FireStation extends PointsLoader.Point {
    private final int totalTrucks;
    private int availableTrucks;
    private int onMission;
    private int firesExtinguished;
    private float totalResponseTime;

    public FireStation(String id, String name, double lat, double lon, int totalTrucks) {
        super(id, name, lat, lon);
        this.totalTrucks = totalTrucks;
        this.availableTrucks = totalTrucks;
        this.onMission = 0;
        this.firesExtinguished = 0;
        this.totalResponseTime = 0;
    }

    public int getTotalTrucks() {
        return totalTrucks;
    }

    public int getAvailableTrucks() {
        return availableTrucks;
    }

    public int getOnMission() {
        return onMission;
    }

    public int getFiresExtinguished() {
        return firesExtinguished;
    }

    public float getAverageResponseTime() {
        if (firesExtinguished == 0) return 0;
        return totalResponseTime / firesExtinguished;
    }

    public float getOccupancyPercentage() {
        return (float) onMission / totalTrucks * 100;
    }

    public boolean canDispatch(int numTrucks) {
        return availableTrucks >= numTrucks;
    }

    public void dispatchTrucks(int numTrucks) {
        if (canDispatch(numTrucks)) {
            availableTrucks -= numTrucks;
            onMission += numTrucks;
        }
    }

    public void returnTrucks(int numTrucks) {
        onMission -= numTrucks;
        availableTrucks += numTrucks;
        firesExtinguished++;
    }

    public void addResponseTime(float time) {
        totalResponseTime += time;
    }

    public String getLocation() {
        return String.format("%.4f, %.4f", lat, lon);
    }
}
