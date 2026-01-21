package si.um.feri.gasilci;

import java.util.ArrayList;
import java.util.List;

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
    private final List<FirePoint> firePoints;
    private final FireStation station;
    private final RoutingService routingService;
    private final MapTileRenderer mapTileRenderer;
    private final RouteRenderer routeRenderer;
    private FireClickListener fireClickListener;
    private StationClickListener stationClickListener;
    private final DispatchManager dispatchManager;


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
        // Pick 3 random fires from nearby ones
        firePoints = PointsLoader.pickRandom(nearbyFires, 3);

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
            if (extinguishCompleteListener != null) {
                extinguishCompleteListener.onFireExtinguished(fire);
            }
        });
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




    public float[] getStationWorldPosition() {
        if (station != null) {
            return mapTileRenderer.latLonToWorld(station.lat, station.lon);
        }
        return new float[]{GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2};
    }

    public List<FirePoint> getFires() {
        return firePoints;
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

        for (FirePoint p : firePoints) {
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

            routeRenderer.setRoute(routeWorldPoints);
            dispatchManager.dispatchTrucks(fire, routeWorldPoints, numTrucks);

        } catch (Exception e) {
            System.err.println("Failed to dispatch trucks: " + e.getMessage());
        }
    }

    public void update(float delta) {
        dispatchManager.update(delta);
    }

    public DispatchManager getDispatchManager() {
        return dispatchManager;
    }
}
