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
 */
public class TimetableHandler implements HttpHandler {

    // One week has five workdays, each workday has nine periods
    public static final int DAYS_IN_WEEK = 5;
    public static final int PERIODS_PER_DAY = 9;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Security Check: Get the session token from cookie
        String token = HttpUtils.getSessionToken(exchange);
        Student currentStudent = SessionManager.getStudent(token);

        // If not logged in, redirect to login page
        if (currentStudent == null) {
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
        // Availability string is 45 chars (5 days * 9 periods)
        String availability = currentStudent.getAvailability();
        StringBuilder tableHtml = new StringBuilder();

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for (int p = 0; p < PERIODS_PER_DAY; p++) { // 9 Periods (Rows)
            tableHtml.append("<tr>");
            tableHtml.append("<td>Period ").append(p + 1).append("</td>");

            for (int d = 0; d < DAYS_IN_WEEK; d++) { // 5 Days (Columns)
                int index = d * PERIODS_PER_DAY + p; // Calculate the 1D index from 2D coordinates
                char status = availability.charAt(index);

                // If status is '1', the cell is green (available), else it's white
                String colorClass = (status == '1') ? "table-success" : "";
                String text = (status == '1') ? "Available" : "Busy";

                // We add an onclick attribute to handle the toggling later via JavaScript
                tableHtml.append(String.format(
                        "<td class='%s' onclick='toggleCell(%d)' style='cursor:pointer' id='cell-%d'>%s</td>",
                        colorClass, index, index, text
                ));
            }
            tableHtml.append("</tr>");
        }

        // Replace placeholders in the HTML
        html = html.replace("{{STUDENT_NAME}}", currentStudent.getName());
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