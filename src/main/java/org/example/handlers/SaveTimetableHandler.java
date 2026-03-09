package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.HttpUtils;
import org.example.Routes;
import org.example.SessionManager;
import org.example.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This class handles the manual saving of the timetable form.
 * It reads the 45-character availability string from the hidden input and updates the database.
 */
public class SaveTimetableHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;

    public SaveTimetableHandler(Dao<Student, String> studentDao) {
        this.studentDao = studentDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Security Check
        // Ensure the user is logged in
        String token = HttpUtils.getSessionToken(exchange);
        Student student = SessionManager.getStudent(token);

        if (student == null) {
            exchange.getResponseHeaders().add("Location", Routes.LOGIN);
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Only process POST requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            HttpUtils.showError(exchange, "Invalid request method.");
            return;
        }

        try {
            // Parse the form data from the HTTP request body
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String query = br.readLine();

            if (query != null && !query.isEmpty()) {
                // The query looks like: availabilityData=0010101000...
                String[] keyvalue = query.split("=");
                if (keyvalue.length == 2 && keyvalue[0].equals("availabilityData")) {
                    // Extract the 45-character string
                    String newAvailability = URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8);

                    // Update the student object and save to database
                    // Basic validation: ensure it's exactly 45 characters long
                    if (newAvailability.length() == 45) {
                        student.setAvailability(newAvailability);
                        studentDao.update(student); // Save changes to the H2 database
                    }
                }
            }

            // Redirect the user back to the timetable page to see their saved changes
            // This follows the classic Post/Redirect/Get (PRG) pattern for web development
            exchange.getResponseHeaders().add("Location", Routes.TIMETABLE);
            exchange.sendResponseHeaders(302, -1);

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Failed to save timetable: " + e.getMessage());
        }
    }
}