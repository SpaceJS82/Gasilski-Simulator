package si.um.feri.gasilci.config;

public class GeoapifyConfig {
    private static final String API_KEY = "73be8fd9d9f04f4f99249a6e46061205";  //effb1e44b0734096aa9f87b1bc62a6f8
    public static final String MAP_TILES_BASE_URL = "https://maps.geoapify.com/v1/tile";
    public static final String ROUTING_API_URL = "https://api.geoapify.com/v1/routing";
    public static final String MAP_STYLE = "maptiler-3d";

    public static final double MARIBOR_LAT = 46.547798;
    public static final double MARIBOR_LON = 15.646747;

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getTileUrl(int z, int x, int y) {
        return String.format("%s/%s/%d/%d/%d%s.png?apiKey=%s", MAP_TILES_BASE_URL, MAP_STYLE, z, x, y, "@2x", getApiKey());
    }

    public static String getRoutingUrl(double fromLat, double fromLon, double toLat, double toLon, String mode) {
        return String.format("%s?waypoints=%f,%f|%f,%f&mode=%s&apiKey=%s", ROUTING_API_URL, fromLat, fromLon, toLat, toLon, mode, getApiKey());
    }
}
