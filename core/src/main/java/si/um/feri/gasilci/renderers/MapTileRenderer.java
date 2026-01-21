package si.um.feri.gasilci.renderers;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.services.TileLoaderService;
import si.um.feri.gasilci.util.MapUtil;

public class MapTileRenderer {
    private static final int GRID_WIDTH = 32;
    private static final int GRID_HEIGHT = 18;
    private static final int MAP_ZOOM = 17;

    private final Map<String, Texture> tileCache = new HashMap<>();
    private final TileLoaderService tileLoaderService;
    private Texture placeholderTexture;
    private final int startTileX;
    private final int startTileY;
    private final double centerLat;
    private final double centerLon;

    public MapTileRenderer(double lat, double lon) {
        this.centerLat = lat;
        this.centerLon = lon;
        tileLoaderService = new TileLoaderService(tileCache);
        createPlaceholderTexture();

        int[] centerTile = latLonToTile(lat, lon);
        int centerTileX = centerTile[0];
        int centerTileY = centerTile[1];

        startTileX = centerTileX - GRID_WIDTH / 2;
        startTileY = centerTileY - GRID_HEIGHT / 2;
        loadAllTiles();
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
                tileLoaderService.loadTileAsync(tileX, tileY, MAP_ZOOM);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                int tileX = startTileX + x;
                int tileY = startTileY + y;

                String key = MAP_ZOOM + "_" + tileX + "_" + tileY;
                Texture tile = tileCache.get(key);

                float worldX = x * 1.0f;
                float worldY = (GRID_HEIGHT - 1 - y) * 1.0f;

                if (tile != null) {
                    batch.draw(tile, worldX, worldY, 1.0f, 1.0f);
                } else {
                    batch.draw(placeholderTexture, worldX, worldY, 1.0f, 1.0f);
                }
            }
        }
    }

    private int[] latLonToTile(double lat, double lon) {
        return MapUtil.latLonToTile(lat, lon, MAP_ZOOM);
    }

    public float[] latLonToWorld(double lat, double lon) {
        double[] tile = MapUtil.latLonToTileDouble(lat, lon, MAP_ZOOM);
        float worldX = (float)(tile[0] - startTileX);
        float worldY = (float)((startTileY + GRID_HEIGHT) - tile[1]);
        return new float[]{worldX, worldY};
    }

    public double[] worldToLatLon(float worldX, float worldY) {
        double tileX = worldX + startTileX;
        double tileY = (startTileY + GRID_HEIGHT) - worldY;
        return MapUtil.tileToLatLon(tileX, tileY, MAP_ZOOM);
    }

    public void dispose() {
        tileLoaderService.dispose();
        placeholderTexture.dispose();
        for (Texture texture : tileCache.values()) {
            texture.dispose();
        }
        tileCache.clear();
    }
}
