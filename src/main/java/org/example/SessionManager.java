package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class manually manages user sessions because the native HttpServer
 * doesn't provide a built-in session mechanism like Tomcat or Spark.
 * It maps a unique Token (stored in browser cookie) to a Student object.
 */
public class SessionManager {
    // A thread-safe-like approach (simplified for IA) to store active sessions
    // Key: Session ID (UUID), Value: The logged-in Student object
    private static final Map<String, Student> sessions = new HashMap<>();

    /**
     * Creates a new session for a user and returns the Token
     * @param student The student who just logged in
     * @return A unique session token string
     */
    public static String createSession(Student student) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, student);
        return token;
    }

    /**
     * Retrieves the student associated with a specific token
     * @param token The token from the HTTP Cookie header
     * @return Student object if found, otherwise null
     */
    public static Student getStudent(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    /**
     * Removes the session (the Log out logic)
     * @param token The token to invalidate
     */
    public static void logout(String token) {
        sessions.remove(token);
    }
}