package si.um.feri.gasilci;

import java.util.ArrayList;
import java.util.List;
import si.um.feri.gasilci.config.GameConfig;
import si.um.feri.gasilci.data.PointsLoader;
import si.um.feri.gasilci.data.PointsLoader.Point;
import si.um.feri.gasilci.renderers.MapTileRenderer;
import si.um.feri.gasilci.renderers.RouteRenderer;
import si.um.feri.gasilci.services.RoutingService;
import si.um.feri.gasilci.services.RoutingService.LatLon;

public class GameWorld {
    private final List<Point> firePoints;
    private final Point stationPoint;
    private final RoutingService routingService;
    private final MapTileRenderer mapTileRenderer;
    private final RouteRenderer routeRenderer;

    public GameWorld(MapTileRenderer mapTileRenderer, RouteRenderer routeRenderer) {
        this.mapTileRenderer = mapTileRenderer;
        this.routeRenderer = routeRenderer;
        this.routingService = new RoutingService();

        firePoints = PointsLoader.loadFires("data/fires.json");
        stationPoint = PointsLoader.loadStation("data/station.json");
    }

    public float[] getStationWorldPosition() {
        if (stationPoint != null) {
            return mapTileRenderer.latLonToWorld(stationPoint.lat, stationPoint.lon);
        }
        return new float[]{GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2};
    }

    public List<Point> getFires() {
        return firePoints;
    }

    public Point getStation() {
        return stationPoint;
    }

    public void handleMapClick(float worldX, float worldY) {
        float radius = 0.25f;
        Point nearest = null;
        float bestDist2 = radius * radius;

        for (Point p : firePoints) {
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

        try {
            List<LatLon> latlons = routingService.getRouteLatLon(stationPoint.lat, stationPoint.lon, nearest.lat, nearest.lon, "drive");
            List<float[]> routeWorldPoints = new ArrayList<>();

            for (LatLon ll : latlons) {
                routeWorldPoints.add(mapTileRenderer.latLonToWorld(ll.lat, ll.lon));
            }
            routeRenderer.setRoute(routeWorldPoints);
        } catch (Exception ignored) { }
    }
}
