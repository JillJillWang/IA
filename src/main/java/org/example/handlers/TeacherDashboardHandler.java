package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class handles the Teacher's Dashboard.
 * It ensures security by checking if the user object is an instance of Teacher.
 */
/*
 * Reference List:
 * 1. HTTP Response Status Codes: Mozilla Developer Network
 * (https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
 * 2. Java I/O Streams and Resource Management: Oracle Java Documentation
 * (https://docs.oracle.com/javase/tutorial/essential/io/streams.html)
 */
public class TeacherDashboardHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the token and check who it is in the HashMap
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        // Double security check:
        // 1. Authentication: Has the user logged in?
        // 2. Authorization： Is the user a teacher? (Only Teachers allowed）
        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied: Only teachers can view this page.");
            return;
        }

        // Load the dashboard template
        InputStream in = getClass().getResourceAsStream(FilePaths.WWW_ROOT + "teacher-dashboard.html");
        // If it can't find the dashboard: output the error message
        if (in == null) {
            HttpUtils.showError(exchange, "Teacher dashboard template file not found.");
            return;
        }
        String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();

        // Inject the teacher's name for welcoming
        html = html.replace("{{TEACHER_NAME}}", currentUser.getName());

        // In order to send the dynamically generated HTML string to the client,
        // I have to handle the underlying byte stream conversion:
        // First, I encode the String into byte[] using the UTF-8 character set to prevent encoding errors
        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        // Then, I configure the HTTP response headers (Response Headers) to explicitly send
        // the 200 OK status code and the length of the payload
        exchange.sendResponseHeaders(200, response.length);
        // Finally, I write the data through the OutputStream
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}