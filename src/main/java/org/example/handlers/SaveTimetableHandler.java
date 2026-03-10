package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles saving the 45-character availability string for both Students and Teachers.
 * It uses 'instanceof' to decide which database table to update.
 * instanceof: Check if the object is an instance of the specified class,
 * return true if yes, false otherwise
 */
public class SaveTimetableHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;
    private final Dao<Teacher, String> teacherDao;

    // Constructor takes both DAOs to handle both user types
    public SaveTimetableHandler(Dao<Student, String> studentDao, Dao<Teacher, String> teacherDao) {
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the current logged-in user (could be Student or Teacher)
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        // Redirect to login if session is invalid
        if (currentUser == null) {
            exchange.getResponseHeaders().add("Location", Routes.LOGIN);
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Only process POST request
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) return;

        try {
            // 2. Read the 45-character string from the form
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String query = br.readLine();
            String availabilityData = "";

            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2 && kv[0].equals("availabilityData")) {
                        availabilityData = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    }
                }
            }

            // Update the object and the corresponding database table
            if (availabilityData.length() == 45) {
                currentUser.setAvailability(availabilityData);

                // Use instanceof to choose the correct DAO
                if (currentUser instanceof Student) {
                    studentDao.update((Student) currentUser);
                } else if (currentUser instanceof Teacher) {
                    teacherDao.update((Teacher) currentUser);
                }
            }

            // Redirect back to the timetable page to show the saved state
            exchange.getResponseHeaders().add("Location", Routes.TIMETABLE);
            exchange.sendResponseHeaders(302, -1);

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Failed to save: " + e.getMessage());
        }
    }
}