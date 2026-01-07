package si.um.feri.gasilci.config;

public class GeoapifyConfig {
    private static final String API_KEY = "effb1e44b0734096aa9f87b1bc62a6f8";
    public static final String MAP_TILES_BASE_URL = "https://maps.geoapify.com/v1/tile";
    public static final String ROUTING_API_URL = "https://api.geoapify.com/v1/routing";
    public static final String MAP_STYLE = "carto";
    public static final int TILE_SCALE = 2; // 1=256px, 2=512px (retina)

    public static final double MARIBOR_LAT = 46.547798;
    public static final double MARIBOR_LON = 15.646747;

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getTileUrl(int z, int x, int y) {
        // Use retina tiles for sharper rendering (@2x → 512px)
        String suffix = TILE_SCALE == 2 ? "@2x" : "";
        return String.format("%s/%s/%d/%d/%d%s.png?apiKey=%s", MAP_TILES_BASE_URL, MAP_STYLE, z, x, y, suffix, getApiKey());
    }

    public static String getTileCachePath(int z, int x, int y) {
        String suffix = TILE_SCALE == 2 ? "@2x" : "";
        return String.format("tiles/%d/%d/%d%s.png", z, x, y, suffix);
    }

    public static String getRoutingUrl(double fromLat, double fromLon, double toLat, double toLon, String mode) {
        return String.format("%s?waypoints=%f,%f|%f,%f&mode=%s&apiKey=%s", ROUTING_API_URL, fromLat, fromLon, toLat, toLon, mode, getApiKey());
    }
}
