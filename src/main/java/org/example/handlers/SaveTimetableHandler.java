package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This class handles the manual saving of the timetable form.
 * It uses 'instanceof' to determine the actual role (Student or Teacher)
 * and updates the correct database table using the respective DAO.
 */
public class SaveTimetableHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;
    private final Dao<Teacher, String> teacherDao; // NEW: Added Teacher DAO

    // Constructor now requires DAOs for both database tables
    public SaveTimetableHandler(Dao<Student, String> studentDao, Dao<Teacher, String> teacherDao) {
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Security Check
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        if (currentUser == null) {
            exchange.getResponseHeaders().add("Location", Routes.LOGIN);
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            HttpUtils.showError(exchange, "Invalid request method.");
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String query = br.readLine();

            if (query != null && !query.isEmpty()) {
                String[] keyvalue = query.split("=");
                if (keyvalue.length == 2 && keyvalue[0].equals("availabilityData")) {
                    String newAvailability = URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8);

                    if (newAvailability.length() == 45) {
                        currentUser.setAvailability(newAvailability);

                        // TYPE CHECKING: Determine the specific class and update the correct database table
                        if (currentUser instanceof Student) {
                            // Cast User to Student and update
                            studentDao.update((Student) currentUser);
                        } else if (currentUser instanceof Teacher) {
                            // Cast User to Teacher and update
                            teacherDao.update((Teacher) currentUser);
                        }
                    }
                }
            }

            // Redirect back to the timetable to see changes
            exchange.getResponseHeaders().add("Location", Routes.TIMETABLE);
            exchange.sendResponseHeaders(302, -1);

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Failed to save timetable: " + e.getMessage());
        }
    }
}