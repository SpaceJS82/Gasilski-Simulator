package si.um.feri.gasilci.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.util.HttpUtil;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.*;

public class TileLoader {
    private final ExecutorService executor;
    private final Map<String, Texture> tileCache;
    private final ConcurrentHashMap<String, Boolean> loading;

    public TileLoader(Map<String, Texture> tileCache) {
        this.executor = Executors.newFixedThreadPool(4);
        this.tileCache = tileCache;
        this.loading = new ConcurrentHashMap<>();
    }

    public void loadTileAsync(int x, int y, int zoom) {
        String key = zoom + "_" + x + "_" + y;

        if (tileCache.containsKey(key) || loading.containsKey(key)) {
            return;
        }
        loading.put(key, true);

        executor.submit(() -> {
            try {
                String url = GeoapifyConfig.getTileUrl(zoom, x, y);
                InputStream stream = HttpUtil.getStream(url);
                byte[] bytes = stream.readAllBytes();
                stream.close();

                final Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
                Gdx.app.postRunnable(() -> {
                    Texture texture = new Texture(pixmap);
                    pixmap.dispose();
                    tileCache.put(key, texture);
                    loading.remove(key);
                });
            } catch (Exception e) {
                loading.remove(key);
            }
        });
    }

    public void dispose() {
        executor.shutdown();
    }
}
