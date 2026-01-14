package si.um.feri.gasilci.renderers;

import java.util.List;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import si.um.feri.gasilci.assets.RegionNames;
import si.um.feri.gasilci.data.PointsLoader.Point;

public class MapObjectRenderer {
    private final TextureRegion fireIcon;
    private final TextureRegion stationIcon;
    private final MapTileRenderer mapTileRenderer;

    public MapObjectRenderer(TextureAtlas atlas, MapTileRenderer mapTileRenderer) {
        this.mapTileRenderer = mapTileRenderer;
        TextureRegion fire = atlas.findRegion(RegionNames.FIRE_PRIMARY);
        TextureRegion tr = atlas.findRegion(RegionNames.STATION_PRIMARY);

        fireIcon = (fire != null) ? fire : atlas.findRegion(RegionNames.FIRE_FALLBACK);
        stationIcon = (tr != null) ? tr : atlas.findRegion(RegionNames.STATION_FALLBACK);
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, List<Point> fires, Point station) {
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

        // Draw fire icons
        if (fires != null && fireIcon != null) {
            for (Point p : fires) {
                float[] w = mapTileRenderer.latLonToWorld(p.lat, p.lon);
                batch.draw(fireIcon, w[0] - fireSize/2f, w[1] - fireSize/2f, fireSize, fireSize);
            }
        }
    }
}
