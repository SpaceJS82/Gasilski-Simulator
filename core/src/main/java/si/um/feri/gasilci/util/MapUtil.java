package si.um.feri.gasilci.util;

public class MapUtil {
    public static int[] latLonToTile(double lat, double lon, int zoom) {
        int tileX = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int tileY = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        return new int[]{
            tileX,
            tileY
        };
    }
}
