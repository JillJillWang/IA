package org.example;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*
 * Reference List:
 * 1. Using HashMap for Key-Value Data Structuring:
 * W3Schools "Java HashMap"
 * (https://www.w3schools.com/java/java_methods.asp)
 * AND
 * Oracle Official Documentation
 * (https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html)
 *
 * 2. URL Decoding in Java (Handling special characters like '@' in emails):
 * Oracle Official Documentation
 * (https://docs.oracle.com/javase/8/docs/api/java/net/URLDecoder.html)
 * AND
 * Guide to Java URL Encoding/Decoding
 * (https://www.baeldung.com/java-url-encoding-decoding)
 *
 * 3. Reading Files from Resources in Java: Mkyong "Java – Read a file from resources folder"
 * (https://mkyong.com/java/java-read-a-file-from-resources-folder/)
 */

public class HttpUtils {

    /**
     * Uniformly handle error responses by rendering error.html with a custom message
     * @param exchange The carrier of HTTP requests and responses
     * @param message The specific error text to display
     */
    public static void showError(HttpExchange exchange, String message) throws IOException {
        // Read the file error.html by InputStream
        InputStream in = HttpUtils.class.getResourceAsStream(FilePaths.ERROR);
        if (in == null) {
            return;
        }
        String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();

        // Replace placeholders defined in Templates class
        text = text.replace(Templates.MESSAGE, message);
        text = text.replace(Templates.OK, Routes.ROOT);

        // Network transmission cannot directly transmit Java String;
        // it must be converted into byte[] (byte array) that the computer can understand
        // Convert the string that was just replaced into a byte array
        // Send the byte array to the browser
        byte[] contents = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, contents.length);
        OutputStream os = exchange.getResponseBody();
        os.write(contents);
        os.close();
    }

    /**
     * Generic Form Data Parser:
     * This method demonstrates Modularity and Abstraction. Instead of writing the
     * same string manipulation algorithm in multiple handlers, this centralized
     * method parses HTTP POST request bodies into a structured Map.
     * @param exchange The HttpExchange object containing the raw request.
     * @return A Map (HashMap) containing the extracted key-value pairs from the form.
     * @throws IOException If the input stream fails to read.
     */
    public static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        // Create a HashMap
        Map<String, String> formData = new HashMap<>();

        // Read the data from the request body
        // InputStreamReader + StandardCharsets.UTF_8: Prevent garbled characters
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        // E.g., read this line: email=alice%40b.com&name=Alice...
        String query = br.readLine();

        // If no data is found, return an empty map
        if (query == null || query.isEmpty()) {
            return formData;
        }

        // String Manipulation Algorithm:
        // Split the query string by '&'
        // E.g., "email=alice%40b.com", "name=Alice", ...
        String[] vars = query.split("&");
        // Iterate each variable
        for (int i = 0; i < vars.length; i++) {
            String var = vars[i];
            // Split each variable by '='
            // E.g., "name=Alice" --> "name", "Alice"
            String[] keyvalue = var.split("=");

            // If someone fails to fill in a value (E.g., "name="),
            // skip the process to prevent an error
            if (keyvalue.length < 2) {
                continue;
            }

            // Convert keys to lowercase to ensure the consistency
            String key = keyvalue[0].toLowerCase();

            // Decode the value to handle special characters like @ in emails
            // E.g.，When the browser sends the '@' symbol, it will convert it to '%40'.
            // If not decoded, the email address stored in the database will be incorrect.
            // The URLDecoder will convert '%40' back to '@'
            String value = java.net.URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8);

            // Store the processed key and value into the HashMap
            formData.put(key, value);
        }

        return formData;
    }

    /**
     * Helper method to extract the Session Token (from the HTTP 'Cookie' header)
     * Example header: Cookie: session=1234-abcd-...
     * @param exchange HttpExchange object
     * @return The token string, or null if not found
     */
    public static String getSessionToken(HttpExchange exchange) {
        // Request the Cookies from the browser
        // The result obtained by getFirst("Cookie") is a long string of text,
        // such as: "theme=dark; session=1234-abcd; lang=zh"
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");

        // Make sure that the browser actually sent the Cookie,
        // rather than just sending an empty one
        if (cookieHeader != null) {
            // Since the Cookies are separated by ';',
            // I use .split(";") to split them into individual chunks and put them into an array
            // E.g., The result becomes: "theme=dark", " session=1234-abcd", " lang=zh"
            String[] cookies = cookieHeader.split(";");
            // Iterate each chunk
            for (int j = 0; j < cookies.length; j++) {
                String cookie = cookies[j];
                // .trim() can remove the spaces at the beginning and end of the string
                // .split("=") separates the name and value
                // E.g., The result becomes an array: pair[0] is "session", and pair[1] is "1234-abcd"
                String[] pair = cookie.trim().split("=");

                // Final Check:
                // 1. The condition pair.length == 2 ensures that the string has been split into two halves
                // 2. pair[0].equalsIgnoreCase("session") checks if the name of this Cookie is "session"
                // If the check passes, pass the right half (the actual string of random-looking characters) over
                if (pair.length == 2 && pair[0].equalsIgnoreCase("session")) {
                    return pair[1];
                }
            }
        }
        return null;
    }
}