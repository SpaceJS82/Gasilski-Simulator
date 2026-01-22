package si.um.feri.gasilci;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import si.um.feri.gasilci.config.GameConfig;
import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.data.FireStation;
import si.um.feri.gasilci.data.PointsLoader;
import si.um.feri.gasilci.entities.DispatchManager;
import si.um.feri.gasilci.renderers.MapTileRenderer;
import si.um.feri.gasilci.renderers.RouteRenderer;
import si.um.feri.gasilci.services.RoutingService;
import si.um.feri.gasilci.services.RoutingService.LatLon;

public class GameWorld {
    private final List<FirePoint> allFirePoints; // Pool of all 10 fires
    private final List<FirePoint> activeFirePoints; // Currently active fires
    private final List<FirePoint> spawnedFirePoints; // All fires that have been spawned (including extinguished)
    private final FireStation station;
    private final RoutingService routingService;
    private final MapTileRenderer mapTileRenderer;
    private final RouteRenderer routeRenderer;
    private FireClickListener fireClickListener;
    private StationClickListener stationClickListener;
    private final DispatchManager dispatchManager;

    // Fire spawning system
    private float nextFireSpawnTime;
    private static final float MIN_SPAWN_INTERVAL = 15f; // 15 seconds
    private static final float MAX_SPAWN_INTERVAL = 30f; // 30 seconds
    private float timeSinceLastSpawn = 0;

    // Game state tracking
    private int totalFiresExtinguished = 0;

    // Route tracking - which fire has active route displayed
    private FirePoint currentRouteTarget = null;


    public interface FireClickListener {
        void onFireClicked(FirePoint fire, float screenX, float screenY);
    }

    public interface StationClickListener {
        void onStationClicked(FireStation station, float screenX, float screenY);
    }

    public GameWorld(MapTileRenderer mapTileRenderer, RouteRenderer routeRenderer, TextureAtlas atlas, double cityLat, double cityLon) {
        this.mapTileRenderer = mapTileRenderer;
        this.routeRenderer = routeRenderer;
        this.routingService = new RoutingService();

        // Load all fires and filter nearby ones (within 0.05 degrees ~ 5km)
        List<FirePoint> allFires = PointsLoader.loadFires("data/fires.json");
        List<FirePoint> nearbyFires = PointsLoader.filterNearbyFires(allFires, cityLat, cityLon, 0.05);

        // Pick 10 random fires from nearby ones as the fire pool
        this.allFirePoints = PointsLoader.pickRandom(nearbyFires, 10);
        this.activeFirePoints = new ArrayList<>();
        this.spawnedFirePoints = new ArrayList<>();

        // Shuffle the pool to randomize spawn order
        Collections.shuffle(this.allFirePoints);

        // Start with 3 fires
        spawnInitialFires();

        // Schedule first new fire spawn
        nextFireSpawnTime = MIN_SPAWN_INTERVAL + (float)(Math.random() * (MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL));

        // Load all stations and filter nearby ones
        List<FireStation> allStations = PointsLoader.loadStations("data/station.json");
        List<FireStation> nearbyStations = PointsLoader.filterNearbyStations(allStations, cityLat, cityLon, 0.05);
        // Pick 1 random station from nearby ones
        station = PointsLoader.pickRandomStation(nearbyStations);

        this.dispatchManager = new DispatchManager(atlas, getStationWorldPosition(), station);
        this.dispatchManager.setArrivalListener((truck, fire, numTrucks) -> {
            System.out.println("Trucks arrived at fire: " + fire.name + " (" + numTrucks + " trucks)");
            if (extinguishAnimationListener != null) {
                extinguishAnimationListener.onStartExtinguishing(fire);
            }
        });
        this.dispatchManager.setExtinguishCompleteListener((fire) -> {
            System.out.println("Fire extinguished: " + fire.name);
            activeFirePoints.remove(fire);
            totalFiresExtinguished++;
            if (extinguishCompleteListener != null) {
                extinguishCompleteListener.onFireExtinguished(fire);
            }
        });


        this.dispatchManager.setAllTrucksArrivedListener((fire) -> {
            // Clear route only if this fire is the current route target
            if (currentRouteTarget == fire) {
                routeRenderer.clearRoute();
                currentRouteTarget = null;
            }
        });
    }

    private void spawnInitialFires() {
        // Spawn first 3 fires from the pool
        for (int i = 0; i < Math.min(3, allFirePoints.size()); i++) {
            FirePoint fire = allFirePoints.get(i);
            activeFirePoints.add(fire);
            spawnedFirePoints.add(fire);
        }
    }

    public interface ExtinguishAnimationListener {
        void onStartExtinguishing(FirePoint fire);
    }

    private ExtinguishAnimationListener extinguishAnimationListener;

    public void setExtinguishAnimationListener(ExtinguishAnimationListener listener) {
        this.extinguishAnimationListener = listener;
    }
    public interface ExtinguishCompleteListener {
        void onFireExtinguished(FirePoint fire);
    }


    private ExtinguishCompleteListener extinguishCompleteListener;

    public void setExtinguishCompleteListener(ExtinguishCompleteListener listener) {
        this.extinguishCompleteListener = listener;
    }

    public interface FireSpawnListener {
        void onFireSpawned(FirePoint fire);
    }

    private FireSpawnListener fireSpawnListener;

    public void setFireSpawnListener(FireSpawnListener listener) {
        this.fireSpawnListener = listener;
    }

    public void setTruckDrivingSound(Sound sound) {
        this.dispatchManager.setTruckDrivingSound(sound);
    }

    public void setTruckSirenSound(Sound sound) {
        this.dispatchManager.setTruckSirenSound(sound);
    }

    public void setWaterExtinguishingSound(Sound sound) {
        this.dispatchManager.setWaterExtinguishingSound(sound);
    }

    public void setFireAmbientSound(Sound sound) {
        // Set fire ambient sound on all active fires
        for (FirePoint fire : activeFirePoints) {
            if (fire.isActive()) {
                fire.setFireAmbientSound(sound);
            }
        }
    }

    public void stopAllSounds() {
        // Stop all dispatch manager sounds (trucks, water, fire)
        dispatchManager.stopAllSounds();
        // Stop all fire sounds
        for (FirePoint fire : activeFirePoints) {
            fire.stopFireSound();
        }
    }

    private void spawnNextFire() {
        // Find next unspawned fire from the pool
        for (FirePoint fire : allFirePoints) {
            if (!spawnedFirePoints.contains(fire)) {
                activeFirePoints.add(fire);
                spawnedFirePoints.add(fire);
                // Set fire ambient sound if available
                if (!activeFirePoints.isEmpty() && activeFirePoints.get(0).fireAmbientSound != null) {
                    fire.setFireAmbientSound(activeFirePoints.get(0).fireAmbientSound);
                }
                System.out.println("New fire spawned: " + fire.name);

                if (fireSpawnListener != null) {
                    fireSpawnListener.onFireSpawned(fire);
                }
                break;
            }
        }

        // Schedule next spawn
        nextFireSpawnTime = MIN_SPAWN_INTERVAL + (float)(Math.random() * (MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL));
        timeSinceLastSpawn = 0;
    }

    public int getActiveFireCount() {
        return activeFirePoints.size();
    }

    public int getTotalSpawnedCount() {
        return spawnedFirePoints.size();
    }

    public int getTotalFiresExtinguished() {
        return totalFiresExtinguished;
    }

    public int getTotalFiresInPool() {
        return allFirePoints.size();
    }

    public boolean allFiresSpawned() {
        return spawnedFirePoints.size() >= allFirePoints.size();
    }

    public boolean allFiresExtinguished() {
        return totalFiresExtinguished >= allFirePoints.size();
    }



    public float[] getStationWorldPosition() {
        if (station != null) {
            return mapTileRenderer.latLonToWorld(station.lat, station.lon);
        }
        return new float[]{GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2};
    }

    public List<FirePoint> getFires() {
        return activeFirePoints;
    }

    public FireStation getStation() {
        return station;
    }

    public void setFireClickListener(FireClickListener listener) {
        this.fireClickListener = listener;
    }

    public void setStationClickListener(StationClickListener listener) {
        this.stationClickListener = listener;
    }

    public void handleMapClick(float worldX, float worldY, float screenX, float screenY) {
        float radius = 0.25f;

        // Check if station was clicked
        if (station != null) {
            float[] stationWorld = mapTileRenderer.latLonToWorld(station.lat, station.lon);
            float dx = stationWorld[0] - worldX;
            float dy = stationWorld[1] - worldY;
            float d2 = dx*dx + dy*dy;

            if (d2 <= radius * radius) {
                if (stationClickListener != null) {
                    stationClickListener.onStationClicked(station, screenX, screenY);
                }
                return;
            }
        }

        // Check if fire was clicked
        FirePoint nearest = null;
        float bestDist2 = radius * radius;

        for (FirePoint p : activeFirePoints) {
            if (!p.isActive()) continue;

            float[] w = mapTileRenderer.latLonToWorld(p.lat, p.lon);
            float dx = w[0] - worldX;
            float dy = w[1] - worldY;
            float d2 = dx*dx + dy*dy;
            if (d2 <= bestDist2) {
                bestDist2 = d2;
                nearest = p;
            }
        }

        if (nearest != null && fireClickListener != null) {
            fireClickListener.onFireClicked(nearest, screenX, screenY);
        }
    }

    public void dispatchToFire(FirePoint fire, int numTrucks) {
        try {
            List<LatLon> latlons = routingService.getRouteLatLon(
                station.lat, station.lon,
                fire.lat, fire.lon, "drive");

            List<float[]> routeWorldPoints = new ArrayList<>();
            for (LatLon ll : latlons) {
                routeWorldPoints.add(mapTileRenderer.latLonToWorld(ll.lat, ll.lon));
            }

            // Set route and remember which fire it belongs to
            routeRenderer.setRoute(routeWorldPoints);
            currentRouteTarget = fire;
            dispatchManager.dispatchTrucks(fire, routeWorldPoints, numTrucks);

        } catch (Exception e) {
            System.err.println("Failed to dispatch trucks: " + e.getMessage());
        }
    }

    public void update(float delta) {
        dispatchManager.update(delta);

        // Update fire spawning timer (only spawn if not all fires spawned yet and not game over)
        if (!allFiresSpawned() && activeFirePoints.size() < 5) {
            timeSinceLastSpawn += delta;
            if (timeSinceLastSpawn >= nextFireSpawnTime) {
                spawnNextFire();
            }
        }
    }



    public DispatchManager getDispatchManager() {
        return dispatchManager;
    }
}
