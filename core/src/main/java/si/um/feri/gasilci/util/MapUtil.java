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

    public static double[] latLonToTileDouble(double lat, double lon, int zoom) {
        double n = Math.pow(2.0, zoom);
        double x = (lon + 180.0) / 360.0 * n;
        double latRad = Math.toRadians(lat);
        double y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n;
        return new double[]{x, y};
    }

    public static double[] tileToLatLon(double tileX, double tileY, int zoom) {
        double n = Math.pow(2.0, zoom);
        double lon = tileX / n * 360.0 - 180.0;
        double latRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * tileY / n)));
        double lat = Math.toDegrees(latRad);
        return new double[]{lat, lon};
    }
}
