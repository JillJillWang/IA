package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Routes;
import org.example.SessionManager;
import org.example.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.example.HttpUtils.showError;

/**
 * This class handles the login process.
 * It verifies credentials and issues a session cookie to maintain the user's state.
 */
public class LoginHandler implements HttpHandler {
    private final Dao<Student, String> studentDao;

    // Constructor to pass the database access object
    public LoginHandler(Dao<Student, String> studentDao) {
        this.studentDao = studentDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only allow POST requests for security and because it's a form submission
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            showError(exchange, "Method not allowed. Please use the login form.");
            return;
        }

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

                if (key.equalsIgnoreCase("email")) {
                    email = value;
                } else if (key.equalsIgnoreCase("password")) {
                    password = value;
                }
            }

            // Query the student from the database
            Student student = studentDao.queryForId(email);

            // Check if student exists and the password matches
            if (student != null && student.hasPassword(password)) {
                // Create a session and get the token
                String token = SessionManager.createSession(student);

                // Set the "Set-Cookie" header to store the session on the user's browser
                // Path=/ means the cookie is valid for the whole website
                exchange.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");

                // Redirect the student to the main page after successful login
                exchange.getResponseHeaders().add("Location", Routes.TIMETABLE);
                exchange.sendResponseHeaders(302, -1);
            } else {
                // If login fails, show an error on the error page
                showError(exchange, "Invalid email or password. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError(exchange, "Login Error: " + e.getMessage());
        }
    }
}