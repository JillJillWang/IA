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
 * Handles login requests for Student/Teacher roles with strict role-table separation.
 * Uses hidden 'role' form field to query only the relevant database table.
 */
public class LoginHandler implements HttpHandler {
    // DAO for Student table (primary key: String - email)
    private final Dao<Student, String> studentDao;
    // DAO for Teacher table (primary key: String - email)
    private final Dao<Teacher, String> teacherDao;

    /**
     * Inject DAO dependencies for database operations.
     * @param studentDao DAO for Student table interactions
     * @param teacherDao DAO for Teacher table interactions
     */
    public LoginHandler(Dao<Student, String> studentDao, Dao<Teacher, String> teacherDao) {
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
    }

    /**
     * Core login logic: validate credentials & manage session.
     * @param exchange Wrapper for HTTP request/response data
     * @throws IOException If IO error occurs during request handling
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only accept POST (secure for credential submission)
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) return;

        try {
            // Read & decode form data from request body (UTF-8 to avoid encoding issues)
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String query = br.readLine();

            // Initialize variables for parsed form fields
            String email = "";
            String password = "";
            String role = ""; // Track user role (student/teacher) from hidden form field

            // Parse key-value pairs from form data (format: key=value&key=value)
            String[] vars = query.split("&");
            for (String var : vars) {
                String[] keyvalue = var.split("=");
                if (keyvalue.length < 2) continue; // Skip malformed key-value pairs
                String key = keyvalue[0];
                String value = URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8); // Decode URL-encoded values

                // Normalize email to lowercase (avoid case sensitivity issues)
                if (key.equalsIgnoreCase("email")) email = value.toLowerCase();
                else if (key.equalsIgnoreCase("password")) password = value;
                else if (key.equalsIgnoreCase("role")) role = value; // Extract role from hidden field
            }

            User loggedInUser = null;

            // Strict role check: query ONLY the table matching the requested role
            if (role.equalsIgnoreCase("student")) {
                loggedInUser = studentDao.queryForId(email); // Query Student table by email (PK)
            } else if (role.equalsIgnoreCase("teacher")) {
                loggedInUser = teacherDao.queryForId(email); // Query Teacher table by email (PK)
            }

            // Validate user existence and password match
            if (loggedInUser != null && loggedInUser.hasPassword(password)) {
                // Create session token & associate with user
                String token = SessionManager.createSession(loggedInUser);

                // Set HttpOnly cookie (mitigate XSS attacks) - valid for entire application
                exchange.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");

                // Role-based redirection (extra safety: check actual object type)
                if (loggedInUser instanceof Teacher) {
                    exchange.getResponseHeaders().add("Location", Routes.TEACHER_DASHBOARD);
                } else {
                    exchange.getResponseHeaders().add("Location", Routes.TIMETABLE);
                }
                // Send 302 redirect (no response body needed)
                exchange.sendResponseHeaders(302, -1);
            } else {
                // Handle invalid credentials/role mismatch
                HttpUtils.showError(exchange, "Invalid email, password, or role mismatch.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Debug: print stack trace
            // Handle unexpected errors (database issues, IO errors, etc.)
            HttpUtils.showError(exchange, "Login Error: " + e.getMessage());
        }
    }
}