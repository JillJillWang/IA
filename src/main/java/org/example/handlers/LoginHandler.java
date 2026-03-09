package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Routes;
import org.example.SessionManager;
import org.example.Student;
import org.example.Teacher;
import org.example.User;
import org.example.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This class handles the login process.
 * It verifies credentials and issues a session cookie to maintain the user's state.
 */
public class LoginHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;
    private final Dao<Teacher, String> teacherDao;

    // Constructor to pass the database access objects for Student and Teacher tables
    public LoginHandler(Dao<Student, String> studentDao, Dao<Teacher, String> teacherDao) {
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only allow POST requests for security and because it's a form submission
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) return;

        try {
            // Read form data (email=...&password=...)
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String query = br.readLine();
            String email = "";
            String password = "";

            // Parse the form fields from the query string
            String[] vars = query.split("&");
            for (String var : vars) {
                String[] keyvalue = var.split("=");
                if (keyvalue.length < 2) continue;
                String key = keyvalue[0];
                String value = URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8);

                if (key.equalsIgnoreCase("email")) email = value.toLowerCase();
                else if (key.equalsIgnoreCase("password")) password = value;
            }

            // 1. Try to find the user in the Student table first
            User loggedInUser = studentDao.queryForId(email);

            // 2. If not found, try the Teacher table
            if (loggedInUser == null) {
                loggedInUser = teacherDao.queryForId(email);
            }

            // Check if user exists (student/teacher) and the password matches
            if (loggedInUser != null && loggedInUser.hasPassword(password)) {
                // Create a session and get the token
                String token = SessionManager.createSession(loggedInUser);

                // Set the "Set-Cookie" header to store the session on the user's browser
                // Path=/ means the cookie is valid for the whole website
                exchange.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");

                // 4. Role-based redirection using the 'instanceof' operator
                if (loggedInUser instanceof Teacher) {
                    exchange.getResponseHeaders().add("Location", "/teacher-dashboard"); // We will build this next
                } else {
                    // Redirect the student to the main timetable page after successful login
                    exchange.getResponseHeaders().add("Location", Routes.TIMETABLE);
                }
                exchange.sendResponseHeaders(302, -1);
            } else {
                // If login fails, show an error on the error page
                HttpUtils.showError(exchange, "Invalid email or password. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Login Error: " + e.getMessage());
        }
    }
}