package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class manually manages user sessions because the native HttpServer
 * doesn't provide a built-in session mechanism like Tomcat or Spark.
 * It maps a unique Token (stored in browser cookie) to a Student object.
 */

/*
 * Reference List:
 * Generating Secure Tokens: Oracle Java Documentation for UUID
 * (https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html)
 */
public class SessionManager {
    // A thread-safe-like approach to store active sessions
    // Key: Session ID (UUID), Value: The logged-in Student object
    // I use Polymorphism here: A Map that accepts any object extending the User class
    private static final Map<String, User> sessions = new HashMap<>();

    /**
     * Creates a new session for a user and returns the Token
     * @param user The user who just logged in
     * @return A unique session token string
     */
    public static String createSession(User user) {
        // Generate a string of random characters that do not repeat
        // UUID: A class representing an immutable universally unique identifier
        // A UUID represents a 128-bit value
        String token = UUID.randomUUID().toString();
        // Write the correspondence between the token and the user
        sessions.put(token, user);
        // Return the token back to the LoginHandler
        // (so that it can be stuffed into the Cookie)
        return token;
    }

    /**
     * Retrieves the user associated with a specific token
     * @param token The token from the HTTP Cookie header
     * @return User object if found, otherwise null
     */
    public static User getUser(String token) {
        // If the browser doesn't have any cookies, reject it
        if (token == null) {
            return null;
        }
        // Otherwise, look through the HashMap sessions to find out which person
        // this token corresponds to. If not found, return null.
        return sessions.get(token);
    }

    /**
     * Removes the session (Log out logic)
     * @param token The token to invalidate
     */
    public static void logout(String token) {
        // Remove the token after the user log out
        sessions.remove(token);
    }
}