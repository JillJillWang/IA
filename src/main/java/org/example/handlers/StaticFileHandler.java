package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is the HTTP Handler for serving static files
 * It includes null check for missing resources, 404 error handling, and proper stream management
 * to avoid resource leaks and runtime exceptions
 */
public class StaticFileHandler implements HttpHandler {
    protected String filePath;

    // Constructor
    // Assign target file path to instance variable
    public StaticFileHandler(String filePath) {
        this.filePath = filePath;
    }

    /**
     * This is the core method to handle incoming HTTP requests for static files
     * It locates the static file, handles missing files (return 404),
     * reads file content and send as HTTP response,
     * amd ensure proper resource (stream) closure to avoid leaks
     * @param exchange HttpExchange object, the carrier of HTTP requests and responses
     * @throws IOException The exception to inputs and outputs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get input stream for the target static file
        InputStream in = getClass().getResourceAsStream(filePath);

        // Display error page if file is not found
        if (in == null) {
            String errorMsg = "File not found: " + filePath;
            // Call HttpUtils.showError (error.html)
            HttpUtils.showError(exchange, errorMsg);
            return;
        }

        // It ensures that files are read and responses are sent
        // Also ensures that resources are not leaked
        try {
            // Read file content into byte array
            byte[] contents = in.readAllBytes();

            // Send 200 response with file content
            exchange.sendResponseHeaders(200, contents.length);
            OutputStream os = exchange.getResponseBody();
            os.write(contents);
            os.close(); // Close the response output stream and complete the request

        /*
        finally: an optional sub-block of the try block in Java
        Regardless of whether the code in the try block is executed normally
        or an exception/error is thrown, the code in the finally block will definitely be executed
         */

        } finally {
            // Regardless of whether the try block is successfully executed or not,
            // the input stream in must be closed
            in.close();
        }
    }
}
