package si.um.feri.gasilci.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.util.MapUtil;

import java.util.HashMap;
import java.util.Map;

public class MapRenderer {
    private static final int GRID_WIDTH = 16;
    private static final int GRID_HEIGHT = 9;
    private static final int MAP_ZOOM = 15;

    private final Map<String, Texture> tileCache = new HashMap<>();
    private final TileLoader tileLoader;
    private Texture placeholderTexture;
    private final int startTileX;
    private final int startTileY;

    public MapRenderer() {
        tileLoader = new TileLoader(tileCache);
        createPlaceholderTexture();

        int[] centerTile = latLonToTile();
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
    }
}
