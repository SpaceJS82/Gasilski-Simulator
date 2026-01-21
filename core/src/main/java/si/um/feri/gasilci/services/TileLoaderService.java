package si.um.feri.gasilci.services;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import si.um.feri.gasilci.config.GeoapifyConfig;
import si.um.feri.gasilci.util.HttpUtil;

public class TileLoaderService {
    private final ExecutorService executor;
    private final Map<String, Texture> tileCache;
    private final ConcurrentHashMap<String, Boolean> loading;

    public TileLoaderService(Map<String, Texture> tileCache) {
        this.executor = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true); // Daemon threads won't block JVM shutdown
            return thread;
        });
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
                try (InputStream stream = HttpUtil.getStream(url)) {
                    byte[] bytes = stream.readAllBytes();
                    final Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);

                    Gdx.app.postRunnable(() -> {
                        Texture texture = new Texture(pixmap);
                        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                        pixmap.dispose();
                        tileCache.put(key, texture);
                        loading.remove(key);
                    });
                }
            } catch (Exception e) {
                loading.remove(key);
            }
        });
    }

    public void dispose() {
        System.out.println("TileLoaderService: Starting shutdown...");
        executor.shutdownNow(); // Immediately cancel all tasks
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                System.err.println("TileLoaderService: Executor did not terminate in time");
            } else {
                System.out.println("TileLoaderService: Executor terminated successfully");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("TileLoaderService: Interrupted during shutdown");
        }
    }
}
