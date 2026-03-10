package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This handler implements the grid-based matching algorithm.
 * It builds a 9x5 timetable. If the teacher is available in a slot,
 * it searches all students to see who is also available, and lists their names.
 */
public class ViewMatchesHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;

    public ViewMatchesHandler(Dao<Student, String> studentDao) {
        this.studentDao = studentDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Security check: Ensure the user is logged in AND is a Teacher
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied: Only teachers can view matches.");
            return;
        }

        try {
            // Load the HTML webpage
            InputStream in = getClass().getResourceAsStream(FilePaths.VIEW_MATCHES);
            if (in == null) {
                HttpUtils.showError(exchange, "Template not found.");
                return;
            }
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            in.close();

            // This is the core matching algorithm
            // I used the Grid-centric approach
            // Time dimension is divided into fixed grid (5 days × 9 periods = 45 slots)
            // Each slot is indexed as a char in a string ('1' = available, '0' = busy) for state representation
            // Matching logic compares the same grid index of teacher/student strings to
            // find overlapping free periods
            String teacherAvail = currentUser.getAvailability();

            // Fetch all students from the database to compare
            List<Student> allStudents = studentDao.queryForAll();
            StringBuilder tableBody = new StringBuilder();

            final int DAYS_IN_WEEK = 5;
            final int PERIODS_PER_DAY = 9;

            // Loop through the grid: rows (periods) and columns (days)
            for (int p = 0; p < PERIODS_PER_DAY; p++) {
                tableBody.append("<tr>");
                tableBody.append("<td>Period ").append(p + 1).append("</td>");

                for (int d = 0; d < DAYS_IN_WEEK; d++) {
                    int index = d * PERIODS_PER_DAY + p; // Calculate 1D index
                    char teacherStatus = teacherAvail.charAt(index);

                    if (teacherStatus == '1') {
                        // Teacher is available: Mark cell green and align text to top
                        tableBody.append("<td style='background-color:lightgreen; vertical-align:top;'>");

                        // Flag to check if anyone is available in this period
                        boolean hasStudents = false;

                        // Inner Loop: Check every student for this specific time slot
                        for (Student student : allStudents) {
                            if (student.getAvailability().charAt(index) == '1') {
                                // Student matches! Add their name and a placeholder button to the cell
                                tableBody.append(student.getName())
                                        .append(" <button onclick=\"alert('Email coming soon!')\">Arrange</button><br>");
                                hasStudents = true;
                            }
                        }

                        // If the teacher is free but no students are free
                        if (!hasStudents) {
                            tableBody.append("<em>No students</em>");
                        }

                        tableBody.append("</td>");
                    } else {
                        // Teacher is busy: Leave the cell blank
                        tableBody.append("<td></td>");
                    }
                }
                tableBody.append("</tr>");
            }

            // Inject the generated table rows into the HTML template
            html = html.replace("{{TABLE_BODY}}", tableBody.toString());

            // Send the HTTP response
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Algorithm Error: " + e.getMessage());
        }
    }
}