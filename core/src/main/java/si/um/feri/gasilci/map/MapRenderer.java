package si.um.feri.gasilci.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import si.um.feri.gasilci.assets.RegionNames;
import si.um.feri.gasilci.config.GameConfig;
import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.data.PointsLoader;
import si.um.feri.gasilci.data.PointsLoader.Point;
import si.um.feri.gasilci.services.RoutingService;
import si.um.feri.gasilci.services.RoutingService.LatLon;
import si.um.feri.gasilci.util.MapUtil;

public class MapRenderer {
    private static final int GRID_WIDTH = 32;
    private static final int GRID_HEIGHT = 18;
    private static final int MAP_ZOOM = 17; //lower further away, higher closer

    private final Map<String, Texture> tileCache = new HashMap<>();
    private final TileLoader tileLoader;
    private Texture placeholderTexture;
    private final int startTileX;
    private final int startTileY;
    private final ShapeRenderer shapeRenderer;
    private final TextureAtlas atlas;
    private final TextureRegion fireIcon;
    private final TextureRegion stationIcon;
    private List<Point> firePoints;
    private Point stationPoint;
    private final RoutingService routingService = new RoutingService();
    private List<float[]> routeWorldPoints = new ArrayList<>();

    public MapRenderer(TextureAtlas atlas) {
        this.atlas = atlas;
        tileLoader = new TileLoader(tileCache);
        createPlaceholderTexture();

        int[] centerTile = latLonToTile();
        int centerTileX = centerTile[0];
        int centerTileY = centerTile[1];

        startTileX = centerTileX - GRID_WIDTH / 2;
        startTileY = centerTileY - GRID_HEIGHT / 2;
        loadAllTiles();

        shapeRenderer = new ShapeRenderer();
        // atlas is provided via constructor now; resolve regions via RegionNames
        TextureRegion fire = atlas.findRegion(RegionNames.FIRE_PRIMARY);
        fireIcon = (fire != null) ? fire : atlas.findRegion(RegionNames.FIRE_FALLBACK);
        TextureRegion tr = atlas.findRegion(RegionNames.STATION_PRIMARY);
        stationIcon = (tr != null) ? tr : atlas.findRegion(RegionNames.STATION_FALLBACK);
        // Load points from JSON
        firePoints = PointsLoader.loadFires("data/fires.json");
        // For now show all fires (initial requirement: 3 static fires)
        stationPoint = PointsLoader.loadStation("data/station.json");
    }

    public float[] getStationWorldPosition() {
        if (stationPoint != null) {
            return latLonToWorld(stationPoint.lat, stationPoint.lon);
        }
        return new float[]{GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2};
    }

    private void createPlaceholderTexture() {
        Pixmap pixmap = new Pixmap(256, 256, Pixmap.Format.RGB888);
        pixmap.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        pixmap.fill();
        placeholderTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    private void loadAllTiles() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                int tileX = startTileX + x;
                int tileY = startTileY + y;
                tileLoader.loadTileAsync(tileX, tileY, MAP_ZOOM);
            }
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                int tileX = startTileX + x;
                int tileY = startTileY + y;

                String key = MAP_ZOOM + "_" + tileX + "_" + tileY;
                Texture tile = tileCache.get(key);

                float worldX = x * 1.0f;
                float worldY = (GRID_HEIGHT - 1 - y) * 1.0f; // Flip Y

                if (tile != null) {
                    batch.draw(tile, worldX, worldY, 1.0f, 1.0f);
                } else {
                    batch.draw(placeholderTexture, worldX, worldY, 1.0f, 1.0f);
                }
            }
        }

        // Draw markers (icons) on top of tiles within the batch
        // Responsive icon sizes: start larger, but get slightly smaller when zooming in
        float stationBase = 0.5f; // base fraction of a tile at zoom=1
        float fireBase = 0.25f;
        // Map zoom range is clamped to [0.1, 1.0]. We want factor=1 at 1.0 and 0.5 at 0.1.
        float t = (camera.zoom - 0.1f) / 0.9f; // [0,1]
        t = Math.max(0f, Math.min(1f, t));
        float sizeFactor = 0.5f + 0.5f * t;
        float stationSize = stationBase * sizeFactor;
        float fireSize = fireBase * sizeFactor;

        // Station icon
        float[] sWorld = latLonToWorld(stationPoint.lat, stationPoint.lon);
        if (stationIcon != null) {
            batch.draw(stationIcon, sWorld[0] - stationSize/2f, sWorld[1] - stationSize/2f, stationSize, stationSize);
        }
        // Fire icons
        if (fireIcon != null) {
            for (Point p : firePoints) {
                float[] w = latLonToWorld(p.lat, p.lon);
                batch.draw(fireIcon, w[0] - fireSize/2f, w[1] - fireSize/2f, fireSize, fireSize);
            }
        }

        // Route overlay (polyline) with ShapeRenderer
        batch.end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Route polyline (yellow)
        if (!routeWorldPoints.isEmpty()) {
            shapeRenderer.setColor(Color.YELLOW);
            for (int i = 0; i < routeWorldPoints.size() - 1; i++) {
                float[] a = routeWorldPoints.get(i);
                float[] b = routeWorldPoints.get(i + 1);
                shapeRenderer.rectLine(a[0], a[1], b[0], b[1], 0.03f);
            }
        }
        shapeRenderer.end();
        batch.begin();
    }

    private int[] latLonToTile() {
        return MapUtil.latLonToTile(GeoapifyConfig.MARIBOR_LAT, GeoapifyConfig.MARIBOR_LON, MapRenderer.MAP_ZOOM);
    }

    public void dispose() {
        tileLoader.dispose();
        placeholderTexture.dispose();
        for (Texture texture : tileCache.values()) {
            texture.dispose();
        }
        tileCache.clear();
        shapeRenderer.dispose();
        // atlas lifecycle handled by AssetManager outside
    }

    private float[] latLonToWorld(double lat, double lon) {
        double[] tile = MapUtil.latLonToTileDouble(lat, lon, MAP_ZOOM);
        float worldX = (float)(tile[0] - startTileX);
        float worldY = (float)((startTileY + GRID_HEIGHT) - tile[1]);
        return new float[]{worldX, worldY};
    }

    private double[] worldToLatLon(float worldX, float worldY) {
        double tileX = worldX + startTileX;
        double tileY = (startTileY + GRID_HEIGHT) - worldY;
        return MapUtil.tileToLatLon(tileX, tileY, MAP_ZOOM);
    }

    public void onMapClick(float worldX, float worldY) {
        // Display clicked coordinates
        double[] latLon = worldToLatLon(worldX, worldY);

        // Find nearest fire within radius
        float radius = 0.25f;
        Point nearest = null;
        float bestDist2 = radius * radius;
        for (Point p : firePoints) {
            float[] w = latLonToWorld(p.lat, p.lon);
            float dx = w[0] - worldX;
            float dy = w[1] - worldY;
            float d2 = dx*dx + dy*dy;
            if (d2 <= bestDist2) {
                bestDist2 = d2;
                nearest = p;
            }
        }
        if (nearest == null) return;
        try {
            List<LatLon> latlons = routingService.getRouteLatLon(stationPoint.lat, stationPoint.lon, nearest.lat, nearest.lon, "drive");
            routeWorldPoints.clear();
            for (LatLon ll : latlons) {
                routeWorldPoints.add(latLonToWorld(ll.lat, ll.lon));
            }
        } catch (Exception ignored) { }
    }
}
