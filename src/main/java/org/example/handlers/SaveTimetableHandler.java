package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;
import java.io.*;

/**
 * This class handles saving the 45-character availability string for both Students and Teachers.
 * It uses 'instanceof' to decide which database table to update.
 * instanceof: Check if the object is an instance of the specified class,
 * return true if yes, false otherwise
 */
/*
 * Reference List:
 * Java 'instanceof': Geeksforgeeks "instanceof Keyword in Java"
 * (https://www.geeksforgeeks.org/java/instanceof-keyword-in-java/)
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

        // Redirect to log in page if session is invalid
        if (currentUser == null) {
            exchange.getResponseHeaders().add("Location", Routes.ROOT);
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Only process POST request
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            return;
        }

        try {
            // Extract the 45-character string from HashMap (by call the code constructed in HttpUtils.java)
            java.util.Map<String, String> formData = HttpUtils.parseFormData(exchange);
            // Note: Since HttpUtils converted the key to lowercase, here we must use "availabilitydata"
            String availabilityData;
            if (formData.containsKey("availabilitydata")) {
                availabilityData = formData.get("availabilitydata");
            } else {
                // If the string is not obtained, provide an empty string to prevent crash
                availabilityData = "";
            }

            // Make sure we get the correct availabilityData by checking its length
            if (availabilityData.length() == 45) {
                // call the set method
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

            // Show the error
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Failed to save: " + e.getMessage());
        }
    }
}