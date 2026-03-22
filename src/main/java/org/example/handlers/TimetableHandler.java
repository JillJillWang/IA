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

/*
 * Reference List (Self-Taught Resources):
 * 1. 2D Array to 1D Array Mapping Algorithms: Software Engineering StackExchange
 * (https://softwareengineering.stackexchange.com/questions/212808/treating-a-1d-data-structure-as-2d-grid)
 * 2. Java String Formatting and Manipulation: Oracle Official Documentation
 * (https://docs.oracle.com/javase/8/docs/api/java/lang/String.html)
 * 3. Memory Management: String vs StringBuilder in Java: Baeldung Tutorial
 * (https://www.baeldung.com/java-string-builder-string-buffer)
 */
public class TimetableHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the session token from cookie
        String token = HttpUtils.getSessionToken(exchange);

        // Check who it is in the HashMap of SessionManager
        User currentUser = SessionManager.getUser(token);

        // If the user is not found, log in fails, and redirect to login page
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

        // Generate table body with 5 days * 9 periods
        /*
         * Why I use StringBuilder:
         * In Java, String is immutable. This means that once a string is created,
         * its size and content are permanently fixed in memory.
         * However, the size and content of StringBuilder can be freely changed.
         * When using 'tableBody.append("<tr>")', it directly appends text to the same variable
         * without creating a new variable!
         * After the entire loop is completed, call 'tableBody.toString()' again to assemble the final string.
         * Its execution speed is faster and resource management is more efficient.
         */
        StringBuilder tableBody = new StringBuilder();
        // The getAvailability() method is defined in the User base class,
        // so it works seamlessly for both teachers and students.
        String availability = currentUser.getAvailability();

        // Define constants for week days and daily periods
        // Use final to reduce errors and increase maintainability & extensibility
        final int DAYS_IN_WEEK = 5;
        final int PERIODS_PER_DAY = 9;

        // A double nested loop to create the dynamic timetable
        for (int p = 0; p < PERIODS_PER_DAY; p++) { // 9 periods per day
            tableBody.append("<tr>"); // <tr> represents to create a new row
            // In the first column, write "Period 1",
            // in the second column write "Period 2", and so on
            tableBody.append("<td>Period ").append(p + 1).append("</td>");
            for (int d = 0; d < DAYS_IN_WEEK; d++) { // 5 days per week
                // Convert the two-dimensional coordinates into a one-dimensional index
                int index = d * PERIODS_PER_DAY + p;
                // status can be '1' (idle) or '0' (busy)
                char status = availability.charAt(index);

                // If it is '1' (idle), then add a green background to the table
                // to facilitate users' identification
                String bgColor;
                if (status == '1') {
                    bgColor = "style='background-color:lightgreen;'";
                } else {
                    bgColor = "";
                }

                // If it is '1' (idle), then change the text from "busy" to "Available"
                String cellText;
                if (status == '1') {
                    cellText = "Available";
                } else {
                    cellText = "Busy";
                }

                // Assemble the complete HTML code for the cell
                // and bind this change with the  click event (by "onclick='toggleCell(%d)' %s>%s</td>")
                tableBody.append(String.format(
                        "<td id='cell-%d' onclick='toggleCell(%d)' %s>%s</td>",
                        index, index, bgColor, cellText
                ));
            }
            // This row of table has been completed
            tableBody.append("</tr>");
        }

        // Identify user role for display
        // If the role is "Teacher", the back button will take user to the dashboard;
        // otherwise (student), the back button will take user back to the home page
        String role;
        if (currentUser instanceof Teacher) {
            role = "Teacher";
        } else {
            role = "Student";
        }

        // Generate dynamic Back button URL and Text based on Polymorphism (User type)
        String backUrl;
        if (currentUser instanceof Teacher) {
            backUrl = Routes.TEACHER_DASHBOARD;
        } else {
            backUrl = Routes.ROOT;
        }

        String backText;
        if (currentUser instanceof Teacher) {
            backText = "Back to Dashboard";
        } else {
            backText = "Back to Home";
        }

        // Design the back links:
        String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        in.close();
        // Basic template replacement
        // getName() is also inherited from the User base class
        html = html.replace("{{USER_NAME}}", currentUser.getName());
        html = html.replace("{{USER_ROLE}}", role);
        html = html.replace("{{AVAILABILITY_STRING}}", availability);
        html = html.replace("{{TABLE_BODY}}", tableBody.toString());

        // Replace the back link placeholders
        html = html.replace("{{BACK_URL}}", backUrl);
        html = html.replace("{{BACK_TEXT}}", backText);

        // Similar to SaveTimetableHandler.java:
        // send the dynamically generated HTML string to the client
        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();


    }
}