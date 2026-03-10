package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Handler for the Teacher's Dashboard.
 * Ensures security by checking if the user object is an instance of Teacher.
 */
public class TeacherDashboardHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        // Security check: Only Teachers allowed
        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied: Only teachers can view this page.");
            return;
        }

        // Load the simple dashboard template
        InputStream in = getClass().getResourceAsStream(FilePaths.WWW_ROOT + "teacher-dashboard.html");
        String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();

        // Inject the teacher's name
        html = html.replace("{{TEACHER_NAME}}", currentUser.getName());

        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}