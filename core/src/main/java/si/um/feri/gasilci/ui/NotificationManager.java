package si.um.feri.gasilci.ui;


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
    private static final float DISPLAY_DURATION = 4f;

    public NotificationManager(Skin skin) {
        this.stage = new Stage(new ScreenViewport());
        this.skin = skin;
    }

    public void showFireNotification(String address) {
        if (popup != null) popup.remove();

        popup = new Window("New Fire!", skin);
        popup.add(new Label("Fire at: " + address, skin)).pad(10);
        popup.pack();
        popup.setPosition(
            (stage.getWidth() - popup.getWidth()) / 2,
            stage.getHeight() - popup.getHeight() - 20
        );
        stage.addActor(popup);
        displayTime = DISPLAY_DURATION;
    }

    public void update(float delta) {
        if (displayTime > 0) {
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

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
    }
}

