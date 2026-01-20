package si.um.feri.gasilci;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import si.um.feri.gasilci.assets.Assets;
import si.um.feri.gasilci.config.GameConfig;
import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.data.FireStation;
import si.um.feri.gasilci.input.MapInputProcessor;
import si.um.feri.gasilci.renderers.MapObjectRenderer;
import si.um.feri.gasilci.renderers.MapTileRenderer;
import si.um.feri.gasilci.renderers.RouteRenderer;
import si.um.feri.gasilci.ui.FirePopupWindow;
import si.um.feri.gasilci.ui.NotificationManager;
import si.um.feri.gasilci.ui.StationPopupWindow;

public class GasilskiSimulator extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Assets assets;
    private MapTileRenderer mapTileRenderer;
    private MapObjectRenderer gameObjectRenderer;
    private RouteRenderer routeRenderer;
    private GameWorld gameWorld;
    private Stage uiStage;
    private Skin skin;
    private FirePopupWindow currentPopup;
    private StationPopupWindow currentStationPopup;
    private NotificationManager notificationManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        assets.load();
        camera = new OrthographicCamera();
        mapTileRenderer = new MapTileRenderer();
        routeRenderer = new RouteRenderer();
        gameWorld = new GameWorld(mapTileRenderer, routeRenderer, assets.getAtlas());
        gameObjectRenderer = new MapObjectRenderer(assets.getAtlas(), mapTileRenderer, routeRenderer);

        // Connect extinguish animation listener
        gameWorld.setExtinguishAnimationListener(fire -> {
            gameObjectRenderer.startExtinguishAnimation(fire);
        });

        // Setup UI
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        uiStage = new Stage(new ScreenViewport());
        notificationManager = new NotificationManager(skin);

        // Position camera at fire station
        float[] stationPos = gameWorld.getStationWorldPosition();
        camera.position.set(stationPos[0], stationPos[1], 0);
        camera.zoom = 0.3f;

        // Create viewport after setting camera position
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        camera.update();

        MapInputProcessor inputProcessor = new MapInputProcessor(camera);
        inputProcessor.setClickListener((worldX, worldY, screenX, screenY) ->
            gameWorld.handleMapClick(worldX, worldY, screenX, screenY));

        // Setup fire click listener
        gameWorld.setFireClickListener((fire, screenX, screenY) -> showFirePopup(fire, screenX, screenY));

        // Setup station click listener
        gameWorld.setStationClickListener((station, screenX, screenY) -> showStationPopup(station, screenX, screenY));

        gameWorld.setExtinguishCompleteListener(fire -> {
            notificationManager.showExtinguishedNotification(fire.name);
        });

        // Use InputMultiplexer to handle both UI and map input
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(inputProcessor);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void showFirePopup(FirePoint fire, float screenX, float screenY) {
        // Close existing popups
        if (currentPopup != null) {
            currentPopup.remove();
        }
        if (currentStationPopup != null) {
            currentStationPopup.remove();
        }

        // Convert fire world position to screen coordinates
        float[] fireWorld = mapTileRenderer.latLonToWorld(fire.lat, fire.lon);
        com.badlogic.gdx.math.Vector3 fireScreenPos = new com.badlogic.gdx.math.Vector3(fireWorld[0], fireWorld[1], 0);
        camera.project(fireScreenPos, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        // Create and show new popup
        int availableTrucks = gameWorld.getDispatchManager().getStation().getAvailableTrucks();
        currentPopup = new FirePopupWindow(fire, skin, availableTrucks);
        currentPopup.setOnPutOut(() -> {
            int numTrucks = currentPopup.getSelectedTrucks();
            gameWorld.dispatchToFire(fire, numTrucks);
            // Remove this line - animation should start when trucks arrive, not on click
            // gameObjectRenderer.startExtinguishAnimation(fire);
            currentPopup = null;
        });
        currentPopup.show(fireScreenPos.x, fireScreenPos.y, uiStage.getWidth(), uiStage.getHeight());
        uiStage.addActor(currentPopup);
    }

    private void showStationPopup(FireStation station, float screenX, float screenY) {
        // Close existing popups
        if (currentPopup != null) {
            currentPopup.remove();
        }
        if (currentStationPopup != null) {
            currentStationPopup.remove();
        }

        // Convert station world position to screen coordinates
        float[] stationWorld = mapTileRenderer.latLonToWorld(station.lat, station.lon);
        com.badlogic.gdx.math.Vector3 stationScreenPos = new com.badlogic.gdx.math.Vector3(stationWorld[0], stationWorld[1], 0);
        camera.project(stationScreenPos, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        // Create and show station popup
        currentStationPopup = new StationPopupWindow(station, skin);
        currentStationPopup.show(stationScreenPos.x, stationScreenPos.y, uiStage.getWidth(), uiStage.getHeight());
        uiStage.addActor(currentStationPopup);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f);

        // Update animations
        gameObjectRenderer.update(delta);
        gameWorld.update(delta);
        notificationManager.update(delta);

        // Render game world
        camera.update();
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        mapTileRenderer.render(batch);
        gameObjectRenderer.render(batch, camera, gameWorld.getFires(), gameWorld.getStation());
        gameObjectRenderer.renderTrucks(batch, gameWorld.getDispatchManager().getActiveTrucks());
        batch.end();

        routeRenderer.render(camera);

        // Render UI
        uiStage.act(delta);
        uiStage.draw();
        notificationManager.render();
    }


    @Override
    public void resize(int width, int height) {
        float camX = camera.position.x;
        float camY = camera.position.y;
        uiStage.getViewport().update(width, height, true);
        viewport.update(width, height, false);
        notificationManager.resize(width, height);
        camera.position.set(camX, camY, 0);
        camera.update();
    }


    @Override
    public void dispose() {
        // Close any open popups
        if (currentPopup != null) {
            currentPopup.remove();
            currentPopup = null;
        }
        if (currentStationPopup != null) {
            currentStationPopup.remove();
            currentStationPopup = null;
        }
        
        // Dispose all resources
        batch.dispose();
        uiStage.dispose();
        skin.dispose();
        mapTileRenderer.dispose();
        routeRenderer.dispose();
        assets.dispose();
        notificationManager.dispose();
        
        // Log for debugging
        System.out.println("GasilskiSimulator: All resources disposed");
    }

}
