package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This handler dynamically generates the 9x5 timetable HTML.
 * It demonstrates Polymorphism: it works for BOTH Student and Teacher
 * by treating them as the abstract 'User' type.
 */
public class TimetableHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Security Check: Get the session token from cookie
        String token = HttpUtils.getSessionToken(exchange);

        // Fetch as generic User, not specific Student/Teacher (Polymorphism)
        User currentUser = SessionManager.getUser(token);

        // If not logged in, redirect to login page
        if (currentUser == null) {
            exchange.getResponseHeaders().add("Location", Routes.ROOT);
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Load the HTML template
        InputStream in = getClass().getResourceAsStream(FilePaths.TIMETABLE);
        // Check if template file exists
        if (in == null) {
            HttpUtils.showError(exchange, "Timetable template not found.");
            return;
        }
        String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();

        // Identify user role for display
        String role = (currentUser instanceof Teacher) ? "Teacher" : "Student";

        // Generate dynamic Back button URL and Text based on Polymorphism (User type)
        String backUrl = (currentUser instanceof Teacher) ? Routes.TEACHER_DASHBOARD : Routes.ROOT;
        String backText = (currentUser instanceof Teacher) ? "Back to Dashboard" : "Back to Home";

        // Generate table body with 5 days * 9 periods
        StringBuilder tableBody = new StringBuilder();
        // The getAvailability() method is defined in the User base class,
        // so it works seamlessly for both teachers and students.
        String availability = currentUser.getAvailability();

        // Define constants for week days and daily periods
        final int DAYS_IN_WEEK = 5;
        final int PERIODS_PER_DAY = 9;

        for (int p = 0; p < PERIODS_PER_DAY; p++) { // 9 periods per day
            tableBody.append("<tr>");
            tableBody.append("<td>Period ").append(p + 1).append("</td>");
            for (int d = 0; d < DAYS_IN_WEEK; d++) { // 5 days per week
                int index = d * PERIODS_PER_DAY + p;
                char status = availability.charAt(index);
                String bgColor = (status == '1') ? "style='background-color:lightgreen;'" : "";

                tableBody.append(String.format(
                        "<td id='cell-%d' onclick='toggleCell(%d)' %s>%s</td>",
                        index, index, bgColor, (status == '1' ? "Available" : "Busy")
                ));
            }
            tableBody.append("</tr>");
        }

        // Basic template replacement
        // getName() is also inherited from the User base class
        html = html.replace("{{USER_NAME}}", currentUser.getName());
        html = html.replace("{{USER_ROLE}}", role);
        html = html.replace("{{AVAILABILITY_STRING}}", availability);
        html = html.replace("{{TABLE_BODY}}", tableBody.toString());

        // Replace the back link placeholders
        html = html.replace("{{BACK_URL}}", backUrl);
        html = html.replace("{{BACK_TEXT}}", backText);

        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}