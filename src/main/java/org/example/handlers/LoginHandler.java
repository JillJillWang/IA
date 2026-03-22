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

import java.io.IOException;
import java.util.Map;

/**
 * Handles login requests for Student/Teacher roles with strict role-table separation.
 * Uses hidden 'role' form field to query only the relevant database table.
 */

/*
 * Reference List:
 * 1. Understanding Stateful vs Stateless HTTP and Sessions: MDN Web Docs
 * (https://developer.mozilla.org/en-US/docs/Web/HTTP/Session)
 * 2. Web Security - Securing Cookies with HttpOnly: OWASP Foundation
 * (https://owasp.org/www-community/HttpOnly)
 * 3. In-Memory Session Storage using Maps: Baeldung "Guide to Java HashMap"
 * (https://www.baeldung.com/java-hashmap)
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
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            return;
        }

        try {
            // Retrieve the parsed form data as a HashMap
            Map<String, String> formData = HttpUtils.parseFormData(exchange);

            // Retrieve the data from the HashMap (if the user doesn't fill in,
            // it defaults to an empty string "", to prevent the program from crashing)
            // Note: Since I have converted all the keys to lowercase in HttpUtils,
            // here I directly use the lowercase "email", "password", "role" to retrieve
            String email;
            if (formData.containsKey("email")) {
                email = formData.get("email").toLowerCase();
            } else {
                email = "";
            }

            String password;
            if (formData.containsKey("password")) {
                password = formData.get("password");
            } else {
                password = "";
            }

            // role: In the HTML code at the front end, there is a line:
            // <input type="hidden" name="role" value="student"> (or teacher).
            // This field is invisible on the webpage, but it will be sent to the backend along with the submission.
            // The LoginHandler uses this "role" to decide whether to query the studentDao or the teacherDao.
            String role;
            if (formData.containsKey("role")) {
                role = formData.get("role");
            } else {
                role = "";
            }


            // Declare a User object (can be a student or teacher)
            User loggedInUser = null;

            // Based on the role, retrieve the corresponding subclass objects from the database
            // and place them into the box of the parent class.
            if (role.equalsIgnoreCase("student")) {
                loggedInUser = studentDao.queryForId(email);
            } else if (role.equalsIgnoreCase("teacher")) {
                loggedInUser = teacherDao.queryForId(email);
            }


            /*
             * The following code is used to solve the problems of
             * user login verification and session state maintenance.
             * This is because the HTTP protocol is stateless, which means it has no memory.
             * Each time a user clicks on a new link, it is a completely new request for it.
             * Therefore, by combining session token and cookie, the server can remember the logged-in users.
             */
            // If validate user existence and password match:
            if (loggedInUser != null && loggedInUser.hasPassword(password)) {
                // SessionManager will generate a string of random characters (a session token)
                String token = SessionManager.createSession(loggedInUser);

                // Issue a cookie with a token to the browser (equivalent to giving the browser an "identity bracelet")
                // - Path=/: This cookie is valid throughout the entire website
                // - HttpOnly: Add a protection lock to the cookie, so malicious code in the webpage cannot steal
                // the token and prevent others from impersonating and logging in (defending against XSS attacks)
                exchange.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");

                // Role-based redirection
                if (loggedInUser instanceof Teacher) {
                    exchange.getResponseHeaders().add("Location", Routes.TEACHER_DASHBOARD);
                } else {
                    exchange.getResponseHeaders().add("Location", Routes.TIMETABLE);
                }
                // Send 302 redirect
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