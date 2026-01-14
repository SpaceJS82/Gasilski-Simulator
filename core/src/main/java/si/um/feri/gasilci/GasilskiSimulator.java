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
import si.um.feri.gasilci.renderers.MapObjectRenderer;
import si.um.feri.gasilci.input.MapInputProcessor;
import si.um.feri.gasilci.renderers.MapTileRenderer;
import si.um.feri.gasilci.renderers.RouteRenderer;

public class GasilskiSimulator extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Assets assets;
    private MapTileRenderer mapTileRenderer;
    private MapObjectRenderer gameObjectRenderer;
    private RouteRenderer routeRenderer;
    private GameWorld gameWorld;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        assets.load();
        camera = new OrthographicCamera();
        mapTileRenderer = new MapTileRenderer();
        routeRenderer = new RouteRenderer();
        gameWorld = new GameWorld(mapTileRenderer, routeRenderer);
        gameObjectRenderer = new MapObjectRenderer(assets.getAtlas(), mapTileRenderer);

        // Position camera at fire station
        float[] stationPos = gameWorld.getStationWorldPosition();
        camera.position.set(stationPos[0], stationPos[1], 0);
        camera.zoom = 0.3f;

        // Create viewport after setting camera position
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        camera.update();

        MapInputProcessor inputProcessor = new MapInputProcessor(camera);
        inputProcessor.setClickListener((worldX, worldY) -> gameWorld.handleMapClick(worldX, worldY));
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        camera.update();
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        mapTileRenderer.render(batch);
        gameObjectRenderer.render(batch, camera, gameWorld.getFires(), gameWorld.getStation());
        batch.end();
        routeRenderer.render(camera);
    }

    @Override
    public void resize(int width, int height) {
        // Save current camera position
        float camX = camera.position.x;
        float camY = camera.position.y;
        viewport.update(width, height, false);

        // Restore camera position
        camera.position.set(camX, camY, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapTileRenderer.dispose();
        routeRenderer.dispose();
        assets.dispose();
    }
}
