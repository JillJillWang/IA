package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This handler builds the matching timetable.
 * It checks the database to color matched students blue,
 * switches between "Arrange" and "Cancel" buttons,
 * and handles success popup alerts.
 */
public class ViewMatchesHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;
    private final Dao<Arrangement, Integer> arrangementDao;

    public ViewMatchesHandler(Dao<Student, String> studentDao, Dao<Arrangement, Integer> arrangementDao) {
        this.studentDao = studentDao;
        this.arrangementDao = arrangementDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied: Only teachers can view matches.");
            return;
        }

        try {
            InputStream in = getClass().getResourceAsStream(FilePaths.VIEW_MATCHES);
            if (in == null) {
                HttpUtils.showError(exchange, "Template not found.");
                return;
            }
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            in.close();

            String teacherAvail = currentUser.getAvailability();
            List<Student> allStudents = studentDao.queryForAll();

            // Get all existing arrangements for this teacher
            List<Arrangement> teacherArrangements = arrangementDao.queryForEq("teacherEmail", currentUser.getEmail());

            StringBuilder tableBody = new StringBuilder();
            final int DAYS_IN_WEEK = 5;
            final int PERIODS_PER_DAY = 9;

            for (int p = 0; p < PERIODS_PER_DAY; p++) {
                tableBody.append("<tr>");
                tableBody.append("<td>Period ").append(p + 1).append("</td>");

                for (int d = 0; d < DAYS_IN_WEEK; d++) {
                    int index = d * PERIODS_PER_DAY + p;
                    char teacherStatus = teacherAvail.charAt(index);

                    if (teacherStatus == '1') {
                        tableBody.append("<td style='background-color:lightgreen; vertical-align:top;'>");
                        boolean hasStudents = false;

                        for (Student student : allStudents) {
                            if (student.getAvailability().charAt(index) == '1') {

                                // Check if this meeting is already arranged in the database
                                boolean isArranged = false;
                                for (Arrangement arr : teacherArrangements) {
                                    if (arr.getStudentEmail().equals(student.getEmail()) && arr.getPeriodIndex() == index) {
                                        isArranged = true;
                                        break;
                                    }
                                }

                                if (isArranged) {
                                    // It IS arranged: Text color is blue, button says "Cancel" and posts to /cancel-arrangement
                                    tableBody.append("<span style='color:blue;'>")
                                            .append(student.getName()).append("</span> ");

                                    tableBody.append("<form method='POST' action='/cancel-arrangement' style='display:inline;'>")
                                            .append("<input type='hidden' name='studentEmail' value='").append(student.getEmail()).append("'>")
                                            .append("<input type='hidden' name='periodIndex' value='").append(index).append("'>")
                                            .append("<button type='submit'>Cancel</button>")
                                            .append("</form><br>");
                                } else {
                                    // NOT arranged: Text color is black, button says "Arrange" and posts to /save-arrangement
                                    tableBody.append("<span style='color:black;'>")
                                            .append(student.getName()).append("</span> ");

                                    tableBody.append("<form method='POST' action='/save-arrangement' style='display:inline;'>")
                                            .append("<input type='hidden' name='studentEmail' value='").append(student.getEmail()).append("'>")
                                            .append("<input type='hidden' name='periodIndex' value='").append(index).append("'>")
                                            .append("<button type='submit'>Arrange</button>")
                                            .append("</form><br>");
                                }

                                hasStudents = true;
                            }
                        }

                        if (!hasStudents) {
                            tableBody.append("<em>No students</em>");
                        }
                        tableBody.append("</td>");
                    } else {
                        tableBody.append("<td></td>");
                    }
                }
                tableBody.append("</tr>");
            }

            html = html.replace("{{TABLE_BODY}}", tableBody.toString());

            // Check URL for success or canceled messages
            String queryUrl = exchange.getRequestURI().getQuery();
            if (queryUrl != null && queryUrl.contains("success=true")) {
                html = html.replace("{{ALERT_SCRIPT}}", "<script>alert('successfully arranged');</script>");
            } else if (queryUrl != null && queryUrl.contains("canceled=true")) {
                html = html.replace("{{ALERT_SCRIPT}}", "<script>alert('successfully canceled');</script>");
            } else {
                html = html.replace("{{ALERT_SCRIPT}}", "");
            }

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