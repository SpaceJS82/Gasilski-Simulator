package si.um.feri.gasilci.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import si.um.feri.gasilci.GasilskiSimulator;
import si.um.feri.gasilci.assets.Assets;
import si.um.feri.gasilci.data.CityData;
import si.um.feri.gasilci.data.CityManager;
import si.um.feri.gasilci.ui.SettingsWindow;
import si.um.feri.gasilci.util.SoundManager;

public class MenuScreen implements Screen {
    private final GasilskiSimulator game;
    private Stage stage;
    private Skin skin;
    private CityManager cityManager;
    private SelectBox<CityData> citySelectBox;
    private Texture backgroundTexture;
    private SpriteBatch batch;
    private Texture menuBackgroundTexture;
    private Assets assets;
    private SettingsWindow settingsWindow;

    public MenuScreen(GasilskiSimulator game) {
        this.game = game;
        this.cityManager = new CityManager();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Load audio assets
        assets = new Assets();
        assets.load();
        
        // Load sound settings
        SettingsWindow.loadSettings();
        
        SoundManager.setButtonClickSound(assets.getButtonClickSound());

        // Create semi-transparent background for menu
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0, 0, 0, 0.7f); // Black with 70% opacity
        bgPixmap.fill();
        menuBackgroundTexture = new Texture(bgPixmap);
        TextureRegionDrawable menuBackground = new TextureRegionDrawable(menuBackgroundTexture);
        bgPixmap.dispose();

        // Create main table for layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.setBackground(menuBackground);
        table.pad(40); // Add padding inside the background box

        // Title label
        Label titleLabel = new Label("GaSim", skin, "title-white");
        titleLabel.setColor(Color.ORANGE);

        // City selection label
        Label cityLabel = new Label("Select Starting Location:", skin);

        // Create city dropdown with custom style for padding
        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle(skin.get(SelectBox.SelectBoxStyle.class));

        // Add padding to the SelectBox background (selected item display)
        if (selectBoxStyle.background != null) {
            selectBoxStyle.background.setLeftWidth(10);
            selectBoxStyle.background.setRightWidth(10);
        }

        // Create custom list style with padding
        List.ListStyle customListStyle = new List.ListStyle(selectBoxStyle.listStyle);

        // Add padding to the list item drawables
        if (customListStyle.selection != null) {
            customListStyle.selection.setLeftWidth(10);
            customListStyle.selection.setRightWidth(10);
        }
        if (customListStyle.background != null) {
            customListStyle.background.setLeftWidth(10);
            customListStyle.background.setRightWidth(10);
        }

        selectBoxStyle.listStyle = customListStyle;

        citySelectBox = new SelectBox<>(selectBoxStyle);
        Array<CityData> citiesArray = new Array<>();
        for (CityData city : cityManager.getCities()) {
            citiesArray.add(city);
        }
        citySelectBox.setItems(citiesArray);

        // Add click listener to dropdown
        citySelectBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
            }
        });

        // Add change listener for when dropdown items are selected
        citySelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundManager.playButtonClick();
            }
        });

        // Set previously selected city
        String selectedCityName = cityManager.getSelectedCity();
        for (CityData city : citiesArray) {
            if (city.name.equals(selectedCityName)) {
                citySelectBox.setSelected(city);
                break;
            }
        }

        // Create buttons with default style (rounded corners like game EXIT button)
        TextButton playButton = new TextButton("PLAY", skin);
        TextButton settingsButton = new TextButton("SETTINGS", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        // Add click listeners
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
                CityData selectedCity = citySelectBox.getSelected();
                cityManager.setSelectedCity(selectedCity.name);
                game.setScreen(new GameScreen(game, selectedCity));
            }
        });
        
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
                showSettingsWindow();
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SoundManager.playButtonClick();
                SoundManager.stopAllSounds();
                Gdx.app.exit();
            }
        });

        // Layout
        table.add(titleLabel).padBottom(50).row();
        table.add(cityLabel).padBottom(10).row();
        table.add(citySelectBox).width(200).height(40).padBottom(30).row();
        table.add(playButton).width(200).height(60).padBottom(20).row();
        table.add(settingsButton).width(200).height(60).padBottom(20).row();
        table.add(exitButton).width(200).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }
    
    private void showSettingsWindow() {
        if (settingsWindow != null) {
            settingsWindow.remove();
        }
        settingsWindow = new SettingsWindow(skin);
        settingsWindow.show(stage.getWidth(), stage.getHeight());
        stage.addActor(settingsWindow);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Draw background
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
        menuBackgroundTexture.dispose();
        batch.dispose();
        if (assets != null) {
            assets.dispose();
        }
    }
}
