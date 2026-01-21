package si.um.feri.gasilci.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import si.um.feri.gasilci.GasilskiSimulator;
import si.um.feri.gasilci.data.CityData;
import si.um.feri.gasilci.data.CityManager;

public class MenuScreen implements Screen {
    private final GasilskiSimulator game;
    private Stage stage;
    private Skin skin;
    private CityManager cityManager;
    private SelectBox<CityData> citySelectBox;

    public MenuScreen(GasilskiSimulator game) {
        this.game = game;
        this.cityManager = new CityManager();
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Create main table for layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Title label
        Label titleLabel = new Label("Gasilski Simulator", skin, "title-white");
        titleLabel.setColor(Color.ORANGE);

        // City selection label
        Label cityLabel = new Label("Select Starting Location:", skin);

        // Create city dropdown
        citySelectBox = new SelectBox<>(skin);
        Array<CityData> citiesArray = new Array<>();
        for (CityData city : cityManager.getCities()) {
            citiesArray.add(city);
        }
        citySelectBox.setItems(citiesArray);

        // Set previously selected city
        String selectedCityName = cityManager.getSelectedCity();
        for (CityData city : citiesArray) {
            if (city.name.equals(selectedCityName)) {
                citySelectBox.setSelected(city);
                break;
            }
        }

        // Create buttons with menu-item style
        TextButton playButton = new TextButton("PLAY", skin, "menu-item");
        TextButton exitButton = new TextButton("EXIT", skin, "menu-item");

        // Add click listeners
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CityData selectedCity = citySelectBox.getSelected();
                cityManager.setSelectedCity(selectedCity.name);
                game.setScreen(new GameScreen(game, selectedCity));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Layout
        table.add(titleLabel).padBottom(50).row();
        table.add(cityLabel).padBottom(10).row();
        table.add(citySelectBox).width(250).height(40).padBottom(30).row();
        table.add(playButton).width(200).height(60).padBottom(20).row();
        table.add(exitButton).width(200).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

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
    }
}
