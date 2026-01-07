package si.um.feri.gasilci.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class MapInputProcessor extends InputAdapter {
    private final OrthographicCamera camera;
    private boolean isDragging = false;
    private final Vector3 lastTouchPos = new Vector3();
    private final Vector3 downPos = new Vector3();
    private MapClickListener clickListener;

    public MapInputProcessor(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void setClickListener(MapClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            isDragging = false;
            downPos.set(screenX, screenY, 0);
            lastTouchPos.set(screenX, screenY, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Start dragging on first actual movement
        if (!isDragging) {
            float dx0 = screenX - downPos.x;
            float dy0 = screenY - downPos.y;
            if (Math.abs(dx0) > 2 || Math.abs(dy0) > 2) {
                isDragging = true;
            } else {
                return true; // swallow tiny movement
            }
        }

        float deltaX = screenX - lastTouchPos.x;
        float deltaY = screenY - lastTouchPos.y;

        camera.position.x -= deltaX * camera.zoom * 0.05f;
        camera.position.y += deltaY * camera.zoom * 0.05f;
        clampCamera();

        lastTouchPos.set(screenX, screenY, 0);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            boolean wasDragging = isDragging;
            isDragging = false;
            // treat as click if finger/mouse didn't move much
            float dx = screenX - downPos.x;
            float dy = screenY - downPos.y;
            boolean isClick = Math.abs(dx) <= 2 && Math.abs(dy) <= 2;
            if (isClick && clickListener != null) {
                Vector3 world = new Vector3(screenX, screenY, 0);
                camera.unproject(world);
                clickListener.onMapClick(world.x, world.y);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);

        float oldZoom = camera.zoom;
        camera.zoom += amountY * 0.1f;

        float minZoom = 0.1f;
        float maxZoom = 1.0f;
        if (camera.zoom < minZoom) camera.zoom = minZoom;
        if (camera.zoom > maxZoom) camera.zoom = maxZoom;

        float zoomDiff = camera.zoom - oldZoom;
        camera.position.x += (mousePos.x - camera.position.x) * (zoomDiff / oldZoom);
        camera.position.y += (mousePos.y - camera.position.y) * (zoomDiff / oldZoom);

        return true;
    }

    private void clampCamera() {
        float cameraHalfWidth = camera.viewportWidth * camera.zoom / 2;
        float cameraHalfHeight = camera.viewportHeight * camera.zoom / 2;
        float MAP_MIN_X = 0;
        float MAP_MAX_X = 16;
        float MAP_MIN_Y = 0;
        float MAP_MAX_Y = 9;

        float minX = MAP_MIN_X + cameraHalfWidth;
        float maxX = MAP_MAX_X - cameraHalfWidth;
        float minY = MAP_MIN_Y + cameraHalfHeight;
        float maxY = MAP_MAX_Y - cameraHalfHeight;

        camera.position.x = Math.max(minX, Math.min(maxX, camera.position.x));
        camera.position.y = Math.max(minY, Math.min(maxY, camera.position.y));
    }
}
