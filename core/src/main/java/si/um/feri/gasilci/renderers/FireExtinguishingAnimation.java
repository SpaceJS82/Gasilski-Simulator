package si.um.feri.gasilci.renderers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import si.um.feri.gasilci.data.FirePoint;

public class FireExtinguishingAnimation {
    private final Animation<TextureRegion> firefighterAnimation;
    private final Animation<TextureRegion> waterStreamAnimation;
    private final FirePoint firePoint;
    private final MapTileRenderer mapTileRenderer;
    private final RouteRenderer routeRenderer;
    private float stateTime;
    private boolean isComplete;
    private static final float ANIMATION_DURATION = 3f;
    private static final float OFFSET_DISTANCE = 0.4f; // Distance from fire

    public FireExtinguishingAnimation(TextureAtlas atlas, FirePoint firePoint, MapTileRenderer mapTileRenderer, RouteRenderer routeRenderer) {
        this.firePoint = firePoint;
        this.mapTileRenderer = mapTileRenderer;
        this.routeRenderer = routeRenderer;
        this.stateTime = 0;
        this.isComplete = false;

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
        if (stateTime >= ANIMATION_DURATION) {
            isComplete = true;
            firePoint.putOut();
            routeRenderer.clearRoute();
        }
    }

    public void render(SpriteBatch batch) {
        if (isComplete) return;

        float[] fireWorld = mapTileRenderer.latLonToWorld(firePoint.lat, firePoint.lon);
        
        // Position firefighter below the fire (perpendicular)
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
        
        // Position water stream between firefighter and fire (vertically)
        // Slight offset to the right to align with firefighter's hose
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

    public boolean isComplete() {
        return isComplete;
    }
}
