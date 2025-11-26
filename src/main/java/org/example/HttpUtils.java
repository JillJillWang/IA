package org.example;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


/*
This class is used to handle the responses to HTTP requests
 */
public class HttpUtils {

    /**
     * This method is used to uniformly handle error responses to HTTP requests
     * @param exchange HttpExchange object, the carrier of HTTP requests and responses
     * @param message A specific String that describes the error
     * @throws IOException The exception to inputs and outputs
     */
    public static void showError(HttpExchange exchange, String message) throws IOException {
        // Read the error.html file through the class FilePath (/www/error.html)
        // Convert error page files into input streams to facilitate subsequent content reading
        InputStream in = HttpUtils.class.getResourceAsStream(FilePaths.ERROR);
        // Read all the bytes of the input stream, convert them to string format, and specify the UTF-8 encoding format
        String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        // Replace Templates.MESSAGE by @param message
        text = text.replace(Templates.MESSAGE, message);
        // Replace Templates.OK by Routes.ROOT ("/")
        text = text.replace(Templates.OK, Routes.ROOT);

        // Send an error page response to the browser
        byte[] contents = text.getBytes();
        exchange.sendResponseHeaders(200, contents.length); // 200 indicates that the page has successfully returned
        OutputStream os = exchange.getResponseBody();
        os.write(contents);
        os.close();
    }

    // TODO: add the page "Please click here if the page does not redirect automatically ..."
}
