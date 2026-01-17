package si.um.feri.gasilci;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import si.um.feri.gasilci.config.GameConfig;
import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.data.PointsLoader;
import si.um.feri.gasilci.data.PointsLoader.Point;
import si.um.feri.gasilci.entities.DispatchManager;
import si.um.feri.gasilci.renderers.MapTileRenderer;
import si.um.feri.gasilci.renderers.RouteRenderer;
import si.um.feri.gasilci.services.RoutingService;
import si.um.feri.gasilci.services.RoutingService.LatLon;

public class GameWorld {
    private final List<FirePoint> firePoints;
    private final Point stationPoint;
    private final RoutingService routingService;
    private final MapTileRenderer mapTileRenderer;
    private final RouteRenderer routeRenderer;
    private FireClickListener fireClickListener;
    private DispatchManager dispatchManager;


    public interface FireClickListener {
        void onFireClicked(FirePoint fire, float screenX, float screenY);
    }

    public GameWorld(MapTileRenderer mapTileRenderer, RouteRenderer routeRenderer, TextureAtlas atlas) {
        this.mapTileRenderer = mapTileRenderer;
        this.routeRenderer = routeRenderer;
        this.routingService = new RoutingService();
        this.dispatchManager = new DispatchManager(atlas, getStationWorldPosition());
        this.dispatchManager.setArrivalListener((truck, fire) -> {
            // Fire extinguishing starts here when truck arrives
            if (fireClickListener != null) {
                fireClickListener.onFireClicked(fire, 0, 0); // Trigger extinguish animation
            }
        });

        firePoints = PointsLoader.loadFires("data/fires.json");
        stationPoint = PointsLoader.loadStation("data/station.json");
    }

    public float[] getStationWorldPosition() {
        if (stationPoint != null) {
            return mapTileRenderer.latLonToWorld(stationPoint.lat, stationPoint.lon);
        }
        return new float[]{GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2};
    }

    public List<FirePoint> getFires() {
        return firePoints;
    }

    public Point getStation() {
        return stationPoint;
    }

    public void setFireClickListener(FireClickListener listener) {
        this.fireClickListener = listener;
    }

    public void handleMapClick(float worldX, float worldY, float screenX, float screenY) {
        float radius = 0.25f;
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

        if (nearest == null) {
            return;
        }

        if (nearest == null) return;

        final FirePoint targetFire = nearest;

        try {
            List<LatLon> latlons = routingService.getRouteLatLon(
                stationPoint.lat, stationPoint.lon,
                targetFire.lat, targetFire.lon, "drive");

            List<float[]> routeWorldPoints = new ArrayList<>();
            for (LatLon ll : latlons) {
                routeWorldPoints.add(mapTileRenderer.latLonToWorld(ll.lat, ll.lon));
            }

            routeRenderer.setRoute(routeWorldPoints);
            dispatchManager.dispatchTruck(targetFire, routeWorldPoints);

        } catch (Exception ignored) { }
    }

    public void update(float delta) {
        dispatchManager.update(delta);
    }

    public DispatchManager getDispatchManager() {
        return dispatchManager;
    }
}
