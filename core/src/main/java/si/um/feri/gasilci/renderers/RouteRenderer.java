package si.um.feri.gasilci.renderers;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class RouteRenderer {
    private final ShapeRenderer shapeRenderer;
    private List<float[]> routeWorldPoints = new ArrayList<>();

    public RouteRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }

    public void setRoute(List<float[]> worldPoints) {
        this.routeWorldPoints = new ArrayList<>(worldPoints);
    }

    public void render(OrthographicCamera camera) {
        if (routeWorldPoints.isEmpty()) {
            return;
        }
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.YELLOW);

        for (int i = 0; i < routeWorldPoints.size() - 1; i++) {
            float[] a = routeWorldPoints.get(i);
            float[] b = routeWorldPoints.get(i + 1);
            shapeRenderer.rectLine(a[0], a[1], b[0], b[1], 0.03f);
        }
        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
