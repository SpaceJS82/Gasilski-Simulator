package si.um.feri.gasilci.renderers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import si.um.feri.gasilci.data.FirePoint;

public class FireExtinguishingAnimation {
    private final FirePoint firePoint;
    private final MapTileRenderer mapTileRenderer;
    private final Animation<TextureRegion> firefighterAnimation;
    private final Animation<TextureRegion> waterStreamAnimation;
    private float stateTime;
    private static final float OFFSET_DISTANCE = 0.3f;

    public FireExtinguishingAnimation(TextureAtlas atlas, FirePoint firePoint, MapTileRenderer mapTileRenderer, RouteRenderer routeRenderer) {
        this.firePoint = firePoint;
        this.mapTileRenderer = mapTileRenderer;
        this.stateTime = 0;

        // Load firefighter frames
        Array<TextureRegion> firefighterFrames = new Array<>();
        for (int i = 1; i <= 6; i++) {
            TextureRegion region = atlas.findRegion("images/fighter-holding-hydrant-" + i);
            if (region != null) {
                firefighterFrames.add(region);
            }
        }
        firefighterAnimation = new Animation<>(0.15f, firefighterFrames, Animation.PlayMode.LOOP);

        // Load water stream frames
        Array<TextureRegion> streamFrames = new Array<>();
        for (int i = 1; i <= 4; i++) {
            TextureRegion region = atlas.findRegion("images/stream-" + i);
            if (region != null) {
                streamFrames.add(region);
            }
        }
        waterStreamAnimation = new Animation<>(0.1f, streamFrames, Animation.PlayMode.LOOP);
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public boolean isComplete() {
        // Complete when fire is no longer active (extinguished by DispatchManager)
        return !firePoint.isActive();
    }

    public FirePoint getFirePoint() {
        return firePoint;
    }

    public void render(SpriteBatch batch) {
        if (isComplete()) return;

        float[] fireWorld = mapTileRenderer.latLonToWorld(firePoint.lat, firePoint.lon);

        // Position firefighter below the fire
        float firefighterX = fireWorld[0];
        float firefighterY = fireWorld[1] - OFFSET_DISTANCE;

        // Draw firefighter
        TextureRegion firefighterFrame = firefighterAnimation.getKeyFrame(stateTime);
        float firefighterWidth = 0.15f;
        float firefighterHeight = 0.21f;
        batch.draw(firefighterFrame,
            firefighterX - firefighterWidth / 2,
            firefighterY - firefighterHeight / 2,
            firefighterWidth,
            firefighterHeight);

        // Position water stream between firefighter and fire
        float streamX = fireWorld[0] + 0.058f;
        float streamY = fireWorld[1] - OFFSET_DISTANCE * 0.5f;

        // Draw water stream
        TextureRegion streamFrame = waterStreamAnimation.getKeyFrame(stateTime);
        float streamWidth = 0.15f;
        float streamHeight = 0.24f;
        batch.draw(streamFrame,
            streamX - streamWidth / 2,
            streamY - streamHeight / 2,
            streamWidth,
            streamHeight);
    }
}
