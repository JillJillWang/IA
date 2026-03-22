package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This handler builds the matching timetable for the teacher.
 * The teacher can view a comprehensive time schedule,
 * which will display the names of all available students at each free time slot.
 * It also allows teacher to click on specific buttons to arrange or cancel a meeting.
 * It marks different colors for students in the reserved state (blue) and
 * non-reserved state (black) so the client can distinguish them.
 */
/*
 * Reference List:
 * 1. Algorithmic Complexity and Big-O Notation: GeeksforGeeks
 * (https://www.geeksforgeeks.org/analysis-algorithms-big-o-analysis/)
 * 2. Using Hidden Input Fields in HTML Forms: MDN Web Docs
 * (https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/hidden)
 * 3. Parsing URL Query Strings in Java: StackOverflow
 * (https://stackoverflow.com/questions/11640025/how-to-obtain-the-query-string-from-a-get-with-java-httpserver)
 */
public class ViewMatchesHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;
    private final Dao<Arrangement, Integer> arrangementDao;

    // Constructor
    public ViewMatchesHandler(Dao<Student, String> studentDao, Dao<Arrangement, Integer> arrangementDao) {
        this.studentDao = studentDao;
        this.arrangementDao = arrangementDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the token and check who it is in the HashMap
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        // Authentication & Authorization
        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied: Only teachers can view matches.");
            return;
        }

        try {
            // Read the HTML template
            InputStream in = getClass().getResourceAsStream(FilePaths.VIEW_MATCHES);
            // If the file cannot be found, display an error message
            if (in == null) {
                HttpUtils.showError(exchange, "Template not found.");
                return;
            }
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            in.close();

            // Get the user (teacher)'s current available
            String teacherAvail = currentUser.getAvailability();
            // Get all students
            List<Student> allStudents = studentDao.queryForAll();
            // Get all existing arrangements for this teacher
            // Search for the column named "teacherEmail" in the table,
            // only take out the row where the value inside is equal to the current teacher's email
            List<Arrangement> teacherArrangements = arrangementDao.queryForEq("teacherEmail", currentUser.getEmail());

            // Create a StringBuilder to build the final table
            StringBuilder tableBody = new StringBuilder();

            final int DAYS_IN_WEEK = 5;
            final int PERIODS_PER_DAY = 9;
            // The first layer loop: 9 classes every day
            for (int p = 0; p < PERIODS_PER_DAY; p++) {
                // Build the dynamic table (similar as TimetableHandler.java)
                tableBody.append("<tr>");
                tableBody.append("<td>Period ").append(p + 1).append("</td>");

                // The second layer loop: 5 days each week (up to this point, it has looped 9 x 5 = 45 times)
                for (int d = 0; d < DAYS_IN_WEEK; d++) {
                    // Calculate the position of this lesson within the 45-character string and check
                    // the teacher's availability
                    int index = d * PERIODS_PER_DAY + p;
                    char teacherStatus = teacherAvail.charAt(index);
                    // If the teacher is available, set the time slot to green
                    if (teacherStatus == '1') {
                        tableBody.append("<td style='background-color:lightgreen; vertical-align:top;'>");
                        // Set a flag: there's no student in this available period
                        boolean hasStudents = false;

                        // The third layer loop: iterate through the List allStudents,
                        // find all available students in this period
                        // 【修改标注】替换增强for循环为传统for循环（List遍历的基础写法）
                        for (int s = 0; s < allStudents.size(); s++) {
                            Student student = allStudents.get(s);
                            if (student.getAvailability().charAt(index) == '1') {
                                hasStudents = true;
                                // Check if this meeting is already arranged in the database
                                boolean isArranged = false;

                                // The fourth layer loop: iterate through the List teacherArrangement,
                                // check whether this student is in a reserved state
                                for (int a = 0; a < teacherArrangements.size(); a++) {
                                    Arrangement arr = teacherArrangements.get(a);
                                    if (arr.getStudentEmail().equals(student.getEmail()) && arr.getPeriodIndex() == index) {
                                        isArranged = true;
                                        // Once the search is completed, exit this fourth level loop
                                        break;
                                    }
                                }

                                if (isArranged) {
                                    // If the student is in the reserved state (is arranged):
                                    // The student's name's color is blue, button says "Cancel" and posts to /cancel-arrangement
                                    tableBody.append("<span style='color:blue;'>")
                                            .append(student.getName()).append("</span> ");

                                    tableBody.append("<form method='POST' action='/cancel-arrangement' style='display:inline;'>")
                                            .append("<input type='hidden' name='studentEmail' value='").append(student.getEmail()).append("'>")
                                            .append("<input type='hidden' name='periodIndex' value='").append(index).append("'>")
                                            .append("<button type='submit'>Cancel</button>")
                                            .append("</form><br>");
                                } else {
                                    // If the student is not in the reserved state:
                                    // The student's name's color is black, button says "Arrange" and posts to /save-arrangement
                                    tableBody.append("<span style='color:black;'>")
                                            .append(student.getName()).append("</span> ");

                                    tableBody.append("<form method='POST' action='/save-arrangement' style='display:inline;'>")
                                            .append("<input type='hidden' name='studentEmail' value='").append(student.getEmail()).append("'>")
                                            .append("<input type='hidden' name='periodIndex' value='").append(index).append("'>")
                                            .append("<button type='submit'>Arrange</button>")
                                            .append("</form><br>");
                                }
                            }
                        }

                        // The third layer loop has ended (move on to the next student)
                        // After iterate through allStudents, if there's no available student
                        // in this period, write "No students" in the table
                        if (!hasStudents) {
                            tableBody.append("<em>No students</em>");
                        }
                        // End this green cell
                        tableBody.append("</td>");
                    } else {
                        // If the teacher is not available for this period, just draw an empty cell and do nothing
                        tableBody.append("<td></td>");
                    }
                }
                // The second layer loop has ended (all periods of one day has been completed)
                tableBody.append("</tr>");
            } // The first layer loop has ended (all 45 classes have been completed)

            // Insert the above <tr> and <td> elements into the HTML template
            html = html.replace("{{TABLE_BODY}}", tableBody.toString());

            // Check URL for success or canceled messages
            // exchange.getRequestURI().getQuery() will retrieve the characters after the '?' in the URL bar
            // E.g., http://localhost:8080/view-matches?success=true
            String queryUrl = exchange.getRequestURI().getQuery();
            // Based on queryUrl, replace {{ALERT_SCRIPT}}
            if (queryUrl != null && queryUrl.contains("success=true")) {
                html = html.replace("{{ALERT_SCRIPT}}", "<script>alert('successfully arranged');</script>");
            } else if (queryUrl != null && queryUrl.contains("canceled=true")) {
                html = html.replace("{{ALERT_SCRIPT}}", "<script>alert('successfully canceled');</script>");
            } else {
                html = html.replace("{{ALERT_SCRIPT}}", "");
            }

            // Convert the final formed web page into a byte stream and send it to the browser
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();

            // Show the error
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Algorithm Error: " + e.getMessage());
        }
    }
}