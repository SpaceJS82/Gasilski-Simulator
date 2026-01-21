package si.um.feri.gasilci.renderers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import si.um.feri.gasilci.assets.RegionNames;
import si.um.feri.gasilci.data.FirePoint;
import si.um.feri.gasilci.data.PointsLoader.Point;
import si.um.feri.gasilci.entities.Truck;

public class MapObjectRenderer {
    private final Animation<TextureRegion> fireAnimation;
    private final TextureRegion stationIcon;
    private final MapTileRenderer mapTileRenderer;
    private final RouteRenderer routeRenderer;
    private final TextureAtlas atlas;
    private float stateTime;
    private final List<FireExtinguishingAnimation> activeExtinguishAnimations;

    public MapObjectRenderer(TextureAtlas atlas, MapTileRenderer mapTileRenderer, RouteRenderer routeRenderer) {
        this.mapTileRenderer = mapTileRenderer;
        this.routeRenderer = routeRenderer;
        this.atlas = atlas;
        this.stateTime = 0;
        this.activeExtinguishAnimations = new ArrayList<>();

        // Load fire animation frames (flame4-1 to flame4-5)
        Array<TextureRegion> fireFrames = new Array<>();
        for (int i = 1; i <= 6; i++) {
            TextureRegion region = atlas.findRegion("images/fire-1-" + i);
            if (region != null) {
                fireFrames.add(region);
            }
        }
        fireAnimation = new Animation<>(0.1f, fireFrames, Animation.PlayMode.LOOP);

        TextureRegion tr = atlas.findRegion(RegionNames.STATION_PRIMARY);
        stationIcon = (tr != null) ? tr : atlas.findRegion(RegionNames.STATION_FALLBACK);
    }

    public void update(float delta) {
        stateTime += delta;

        // Update extinguishing animations
        Iterator<FireExtinguishingAnimation> iterator = activeExtinguishAnimations.iterator();
        while (iterator.hasNext()) {
            FireExtinguishingAnimation anim = iterator.next();
            anim.update(delta);
            if (anim.isComplete()) {
                iterator.remove();
            }
        }
    }

    public void startExtinguishAnimation(FirePoint firePoint) {
        // Check if animation already exists for THIS specific fire
        for (FireExtinguishingAnimation anim : activeExtinguishAnimations) {
            if (anim.getFirePoint() == firePoint && !anim.isComplete()) {
                return; // Already animating this fire
            }
        }
        // Add new animation for this fire
        activeExtinguishAnimations.add(new FireExtinguishingAnimation(atlas, firePoint, mapTileRenderer, routeRenderer));
    }

    public void renderTrucks(SpriteBatch batch, Map<Truck, FirePoint> activeTrucks) {
        for (Truck truck : activeTrucks.keySet()) {
            if (truck.hasStarted() && truck.isVisible()) {
                truck.getSprite().draw(batch);
            }
        }
    }


    public void render(SpriteBatch batch, OrthographicCamera camera, List<FirePoint> fires, Point station) {
        float stationBase = 0.5f;
        float fireBase = 0.25f;
        float t = (camera.zoom - 0.1f) / 0.9f;
        t = Math.max(0f, Math.min(1f, t));
        float sizeFactor = 0.5f + 0.5f * t;
        float stationSize = stationBase * sizeFactor;
        float fireSize = fireBase * sizeFactor;

        // Draw station icon
        if (station != null && stationIcon != null) {
            float[] sWorld = mapTileRenderer.latLonToWorld(station.lat, station.lon);
            batch.draw(stationIcon, sWorld[0] - stationSize/2f, sWorld[1] - stationSize/2f, stationSize, stationSize);
        }

        // Draw fire animations (only active fires)
        if (fires != null && fireAnimation != null) {
            TextureRegion currentFrame = fireAnimation.getKeyFrame(stateTime);
            for (FirePoint p : fires) {
                if (p.isActive()) {
                    float[] w = mapTileRenderer.latLonToWorld(p.lat, p.lon);
                    // Calculate proper aspect ratio
                    float aspectRatio = (float) currentFrame.getRegionWidth() / currentFrame.getRegionHeight();
                    float fireHeight = fireSize;
                    float fireWidth = fireSize * aspectRatio;
                    batch.draw(currentFrame, w[0] - fireWidth/2f, w[1] - fireHeight/2f, fireWidth, fireHeight);
                }
            }
        }



        // Draw extinguishing animations
        for (FireExtinguishingAnimation anim : activeExtinguishAnimations) {
            anim.render(batch);
        }
    }
}
