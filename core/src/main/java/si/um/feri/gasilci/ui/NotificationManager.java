package si.um.feri.gasilci.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class NotificationManager {
    private final Stage stage;
    private final Skin skin;
    private Window popup;
    private float displayTime = 0;
    private boolean persistent = false;

    public NotificationManager(Skin skin) {
        this.stage = new Stage(new ScreenViewport());
        this.skin = skin;
    }

    public void showFireNotification(String address, boolean persistent) {
        if (popup != null) popup.remove();

        this.persistent = persistent;
        popup = new Window("New Fire!", skin);
        Label label1 = new Label("Fire at: " + address, skin);
        label1.setColor(Color.RED);
        popup.add(label1).pad(10);
        popup.pack();
        popup.setPosition(
            (stage.getWidth() - popup.getWidth()) / 2,
            stage.getHeight() - popup.getHeight() - 20
        );
        stage.addActor(popup);
        displayTime = persistent ? Float.MAX_VALUE : 4f;
    }

    public void showFireNotification(String address) {
        showFireNotification(address, false);
    }

    public void showExtinguishedNotification(String address) {
        if (popup != null) popup.remove();

        this.persistent = false;
        popup = new Window("Fire Extinguished!", skin);
        Label label = new Label("Extinguished: " + address, skin);
        label.setColor(Color.GREEN);
        popup.add(label).pad(10);
        popup.pack();
        popup.setPosition(
            (stage.getWidth() - popup.getWidth()) / 2,
            stage.getHeight() - popup.getHeight() - 20
        );
        stage.addActor(popup);
        displayTime = 4f;
    }

    public void update(float delta) {
        if (!persistent && displayTime > 0) {
            displayTime -= delta;
            if (displayTime <= 0 && popup != null) {
                popup.remove();
                popup = null;
            }
        }
        stage.act(delta);
    }

    public void render() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (popup != null) {
            popup.setPosition(
                (stage.getWidth() - popup.getWidth()) / 2,
                stage.getHeight() - popup.getHeight() - 20
            );
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
    }
}
