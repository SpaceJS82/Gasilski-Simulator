package si.um.feri.gasilci.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

public class Truck {
    private float x, y;
    private float rotation = 0; // Angle in degrees
    private List<float[]> route;
    private int currentWaypoint = 0;
    private float speed = 2f;
    private boolean arrived = false;
    private float stateTime = 0;

    private TextureRegion staticTexture;
    private Animation<TextureRegion> truckAnim;
    private boolean hasAnimations = false;

    public Truck(TextureAtlas atlas, float startX, float startY) {
        this.x = startX;
        this.y = startY;

        float frameDuration = 0.1f;

        // Use only the "up" frames and rotate them dynamically
        TextureRegion[] frames = new TextureRegion[6];
        boolean allLoaded = true;

        for (int i = 0; i < 6; i++) {
            frames[i] = atlas.findRegion("images/truck-up-" + (i + 1));
            if (frames[i] == null) {
                allLoaded = false;
                break;
            }
        }

        if (allLoaded) {
            truckAnim = new Animation<>(frameDuration, frames);
            truckAnim.setPlayMode(Animation.PlayMode.LOOP);
            hasAnimations = true;
        } else {
            staticTexture = atlas.findRegion("images/fire-station");
            hasAnimations = false;
        }
    }

    public void setRoute(List<float[]> route) {
        this.route = route;
        this.currentWaypoint = 0;
        this.arrived = false;
        if (route != null && !route.isEmpty()) {
            this.x = route.get(0)[0];
            this.y = route.get(0)[1];
        }
    }

    public void update(float delta) {
        if (route == null || arrived || currentWaypoint >= route.size()) {
            return;
        }

        stateTime += delta;

        float[] target = route.get(currentWaypoint);
        float dx = target[0] - x;
        float dy = target[1] - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Calculate rotation angle (atan2 returns radians, convert to degrees)
        // atan2 gives 0 = right, 90 = up, so adjust for "up" sprite base
        float targetRotation = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees - 90f;

        // Smooth rotation interpolation
        rotation = lerpAngle(rotation, targetRotation, delta * 10f);

        float moveAmount = speed * delta;

        if (dist <= moveAmount) {
            x = target[0];
            y = target[1];
            currentWaypoint++;

            if (currentWaypoint >= route.size()) {
                arrived = true;
            }
        } else {
            x += (dx / dist) * moveAmount;
            y += (dy / dist) * moveAmount;
        }
    }

    private float lerpAngle(float from, float to, float t) {
        float diff = ((to - from + 180) % 360) - 180;
        if (diff < -180) diff += 360;
        return from + diff * Math.min(t, 1f);
    }

    public TextureRegion getCurrentFrame() {
        if (!hasAnimations) {
            return staticTexture;
        }
        return truckAnim.getKeyFrame(stateTime);
    }

    public float getRotation() {
        return rotation;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean hasArrived() { return arrived; }
    public boolean isActive() { return route != null && !arrived; }
}
