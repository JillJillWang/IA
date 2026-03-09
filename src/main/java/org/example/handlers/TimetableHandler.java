package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        // POLYMORPHISM IN ACTION: Fetch as generic User, not specific Student
        User currentUser = SessionManager.getUser(token);

        // If not logged in, redirect to login page
        if (currentUser == null) {
            exchange.getResponseHeaders().add("Location", Routes.LOGIN);
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Load the HTML template
        InputStream in = getClass().getResourceAsStream(FilePaths.TIMETABLE);
        if (in == null) {
            HttpUtils.showError(exchange, "Timetable template not found.");
            return;
        }
        String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();

        // Generate the Dynamic Table Body
        // The getAvailability() method is defined in the User base class,
        // so it works seamlessly for both teachers and students.
        String availability = currentUser.getAvailability();
        StringBuilder tableHtml = new StringBuilder();

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        final int DAYS_IN_WEEK = 5;
        final int PERIODS_PER_DAY = 9;

        for (int p = 0; p < PERIODS_PER_DAY; p++) {
            tableHtml.append("<tr>");
            tableHtml.append("<td>Period ").append(p + 1).append("</td>");

            for (int d = 0; d < DAYS_IN_WEEK; d++) {
                int index = d * PERIODS_PER_DAY + p;
                char status = availability.charAt(index);

                String colorClass = (status == '1') ? "table-success" : "";
                String text = (status == '1') ? "Available" : "Busy";

                tableHtml.append(String.format(
                        "<td class='%s' onclick='toggleCell(%d)' style='cursor:pointer' id='cell-%d'>%s</td>",
                        colorClass, index, index, text
                ));
            }
            tableHtml.append("</tr>");
        }

        // Replace placeholders in the HTML
        // getName() is also inherited from the User base class
        html = html.replace("{{STUDENT_NAME}}", currentUser.getName());
        html = html.replace("{{TABLE_BODY}}", tableHtml.toString());
        html = html.replace("{{AVAILABILITY_STRING}}", availability);

        // Send Response
        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}