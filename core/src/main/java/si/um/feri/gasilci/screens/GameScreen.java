package si.um.feri.gasilci.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import si.um.feri.gasilci.GameWorld;
import si.um.feri.gasilci.GasilskiSimulator;
import si.um.feri.gasilci.assets.Assets;
import si.um.feri.gasilci.config.GameConfig;
import si.um.feri.gasilci.data.CityData;
import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.data.FireStation;
import si.um.feri.gasilci.input.MapInputProcessor;
import si.um.feri.gasilci.renderers.MapObjectRenderer;
import si.um.feri.gasilci.renderers.MapTileRenderer;
import si.um.feri.gasilci.renderers.RouteRenderer;
import si.um.feri.gasilci.ui.FirePopupWindow;
import si.um.feri.gasilci.ui.NotificationManager;
import si.um.feri.gasilci.ui.StationPopupWindow;
import si.um.feri.gasilci.util.SoundManager;

public class GameScreen implements Screen {
    private final GasilskiSimulator game;
    private final CityData selectedCity;
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
    private Label scoreLabel;
    private TextButton exitButton;

    public GameScreen(GasilskiSimulator game, CityData selectedCity) {
        this.game = game;
        this.selectedCity = selectedCity;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        assets = new Assets();
        assets.load();
        SoundManager.setButtonClickSound(assets.getButtonClickSound());
        camera = new OrthographicCamera();
        mapTileRenderer = new MapTileRenderer(selectedCity.lat, selectedCity.lon);
        routeRenderer = new RouteRenderer();
        gameWorld = new GameWorld(mapTileRenderer, routeRenderer, assets.getAtlas(), selectedCity.lat, selectedCity.lon);
        gameWorld.setTruckDrivingSound(assets.getTruckDrivingSound());
        gameWorld.setTruckSirenSound(assets.getTruckSirenSound());
        gameWorld.setWaterExtinguishingSound(assets.getWaterExtinguishingSound());
        gameWorld.setFireAmbientSound(assets.getFireAmbientSound());
        gameObjectRenderer = new MapObjectRenderer(assets.getAtlas(), mapTileRenderer, routeRenderer);

        // Connect extinguish animation listener
        gameWorld.setExtinguishAnimationListener(fire -> {
            gameObjectRenderer.startExtinguishAnimation(fire);
        });

        // Setup UI
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        uiStage = new Stage(new ScreenViewport());
        notificationManager = new NotificationManager(skin);

        // Create score display (top left)
        scoreLabel = new Label("Fires: 0", skin);
        scoreLabel.setColor(Color.YELLOW);

        // Create exit button (top right)
        exitButton = new TextButton("EXIT", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
                // Stop all game sounds before switching screens
                stopAllGameSounds();
                game.setScreen(new MenuScreen(game));
            }
        });

        // Layout UI elements
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.add(scoreLabel).expandX().left().pad(10);
        topTable.add(exitButton).right().pad(10).width(80).height(40);
        uiStage.addActor(topTable);

        // Position camera at selected city
        float[] cityWorldPos = mapTileRenderer.latLonToWorld(selectedCity.lat, selectedCity.lon);
        camera.position.set(cityWorldPos[0], cityWorldPos[1], 0);
        camera.zoom = 0.3f;

        // Create viewport after setting camera position
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        camera.update();

        MapInputProcessor inputProcessor = new MapInputProcessor(camera);
        inputProcessor.setClickListener((worldX, worldY, screenX, screenY) -> {
            // Check if click is outside popups, then close them
            boolean clickedInsidePopup = false;

            if (currentPopup != null) {
                float px = currentPopup.getX();
                float py = currentPopup.getY();
                float pw = currentPopup.getWidth();
                float ph = currentPopup.getHeight();
                if (screenX >= px && screenX <= px + pw && screenY >= py && screenY <= py + ph) {
                    clickedInsidePopup = true;
                }
            }

            if (currentStationPopup != null && !clickedInsidePopup) {
                float px = currentStationPopup.getX();
                float py = currentStationPopup.getY();
                float pw = currentStationPopup.getWidth();
                float ph = currentStationPopup.getHeight();
                if (screenX >= px && screenX <= px + pw && screenY >= py && screenY <= py + ph) {
                    clickedInsidePopup = true;
                }
            }

            // Close popups only if clicked outside
            if (!clickedInsidePopup) {
                if (currentPopup != null) {
                    currentPopup.remove();
                    currentPopup = null;
                }
                if (currentStationPopup != null) {
                    currentStationPopup.remove();
                    currentStationPopup = null;
                }
                gameWorld.handleMapClick(worldX, worldY, screenX, screenY);
            }
        });

        // Setup fire click listener
        gameWorld.setFireClickListener((fire, screenX, screenY) -> showFirePopup(fire, screenX, screenY));

        // Setup station click listener
        gameWorld.setStationClickListener((station, screenX, screenY) -> showStationPopup(station, screenX, screenY));

        gameWorld.setExtinguishCompleteListener(fire -> {
            notificationManager.showExtinguishedNotification(fire.name);
            routeRenderer.clearRoute();
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
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        delta = Math.min(delta, 1 / 30f);

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

        // Update score display
        int score = gameWorld.getStation().getFiresExtinguished();
        scoreLabel.setText("Fires: " + score);

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
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    private void stopAllGameSounds() {
        // Stop all game world sounds (trucks, water, fire)
        gameWorld.stopAllSounds();
        // Stop button click sounds and music
        SoundManager.stopAllSounds();
    }

    @Override
    public void dispose() {
        // Stop all sounds when disposing
        stopAllGameSounds();
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
    }
}
