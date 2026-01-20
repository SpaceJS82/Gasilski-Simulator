package si.um.feri.gasilci.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    public static InputStream getStream(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);  // 10 seconds
        conn.setReadTimeout(10000);     // 10 seconds
        
        // Read all bytes immediately and disconnect
        try (InputStream stream = conn.getInputStream()) {
            byte[] data = stream.readAllBytes();
            return new ByteArrayInputStream(data);
        } finally {
            conn.disconnect();
        }
    }
}
