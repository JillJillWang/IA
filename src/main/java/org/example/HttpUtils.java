package org.example;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpUtils {

    /**
     * Uniformly handle error responses by rendering error.html with a custom message
     * @param exchange The carrier of HTTP requests and responses
     * @param message The specific error text to display
     */
    public static void showError(HttpExchange exchange, String message) throws IOException {
        InputStream in = HttpUtils.class.getResourceAsStream(FilePaths.ERROR);
        if (in == null) return;

        String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();

        // Replace placeholders defined in Templates class
        text = text.replace(Templates.MESSAGE, message);
        text = text.replace(Templates.OK, Routes.ROOT);

        byte[] contents = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, contents.length);
        OutputStream os = exchange.getResponseBody();
        os.write(contents);
        os.close();
    }

    /**
     * Helper method to extract the Session Token from the HTTP 'Cookie' header
     * Example header: Cookie: session=1234-abcd-...
     * @param exchange HttpExchange object
     * @return The token string, or null if not found
     */
    public static String getSessionToken(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] pair = cookie.trim().split("=");
                if (pair.length == 2 && pair[0].equalsIgnoreCase("session")) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}