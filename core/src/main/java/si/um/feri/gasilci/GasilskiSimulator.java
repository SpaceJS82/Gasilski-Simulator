package si.um.feri.gasilci;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import si.um.feri.gasilci.assets.Assets;
import si.um.feri.gasilci.config.GameConfig;
import si.um.feri.gasilci.input.MapClickListener;
import si.um.feri.gasilci.input.MapInputProcessor;
import si.um.feri.gasilci.map.MapRenderer;

public class GasilskiSimulator extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private MapRenderer mapRenderer;
    private Assets assets;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        assets.load();
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        camera.position.set(GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 2, 0);
        // Start more zoomed-in (about 4 scroll steps): 1.0 - 0.4 = 0.6
        camera.zoom = 0.6f;
        camera.update();
        mapRenderer = new MapRenderer(assets.getAtlas());
        MapInputProcessor inputProcessor = new MapInputProcessor(camera);
        inputProcessor.setClickListener(new MapClickListener() {
            @Override
            public void onMapClick(float worldX, float worldY) {
                mapRenderer.onMapClick(worldX, worldY);
            }
        });
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        camera.update();
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        mapRenderer.render(batch, camera);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapRenderer.dispose();
        assets.dispose();
    }
}
