package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Routes;
import org.example.Teacher;
import org.example.HttpUtils;

import java.io.IOException;
import java.util.Map;


/**
 * This class is the HTTP handler for processing teacher registration form submissions
 * It is responsible for parsing form data, validating inputs, saving student to database,
 * and handling redirect/error responses
 */
public class SaveTeacherHandler implements HttpHandler {
    private final Dao<Teacher, String> teacherDao;

    public SaveTeacherHandler(Dao<Teacher, String> teacherDao) {
        this.teacherDao = teacherDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Security check: Only process POST requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            return;
        }

        try {
            // Retrieve the parsed form data as a HashMap
            Map<String, String> formData = HttpUtils.parseFormData(exchange);
            // Create a new Teacher object
            Teacher newTeacher = new Teacher();

            // Extract specific values from the HashMap using keys
            if (formData.containsKey("email")) {
                // Data Normalization: Force emails to lowercase to prevent case-sensitivity login issues
                newTeacher.setEmail(formData.get("email").toLowerCase());
            }
            if (formData.containsKey("name")) {
                newTeacher.setName(formData.get("name"));
            }
            if (formData.containsKey("password")) {
                newTeacher.setPassword(formData.get("password"));
            }

            if (newTeacher.getEmail() == null || newTeacher.getName() == null
                    || newTeacher.getPassword() == null) {
                throw new IOException("All fields (email, name, password) are required");
            }

            // Primary Key Uniqueness Check
            // Similar to SaveStudentHandler.java
            if (teacherDao.queryForId(newTeacher.getEmail()) != null) {
                HttpUtils.showError(exchange, "This email address already exists.");
                return;
            }

            // If there's no same email, the Teacher object will be saved in database
            teacherDao.create(newTeacher);

            // Redirect the user to the login page upon successful registration
            exchange.getResponseHeaders().add("Location", Routes.TEACHER_LOGIN);
            exchange.sendResponseHeaders(302, -1);

        } catch (Exception e) {
            // Print exception stack trace for debugging
            e.printStackTrace();
            HttpUtils.showError(exchange, e.getMessage());
        }
    }
}