package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;


import java.io.IOException;


/**
 * This class handles canceling an existing arrangement when a teacher clicks the "Cancel" button.
 * It uses HTML form submission. After the submission, it redirects to the view-matches page.
 */
/*
 * Reference List (Self-Taught Resources):
 * ORMLite DeleteBuilder: ORMLite Official Documentation
 * (https://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/stmt/DeleteBuilder.html)
 */
public class CancelArrangementHandler implements HttpHandler {
    private final Dao<Arrangement, Integer> arrangementDao;

    public CancelArrangementHandler(Dao<Arrangement, Integer> arrangementDao) {
        this.arrangementDao = arrangementDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only process POST requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            return;
        }

        // Security check: Ensure the user is logged in AND is a Teacher
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied");
            return;
        }

        try {
            // Get HashMap
            // Note: HttpUtils has already converted all the keys to lowercase
            java.util.Map<String, String> formData = HttpUtils.parseFormData(exchange);

            // Extract student email addresses
            String studentEmail;
            if (formData.containsKey("studentemail")) {
                studentEmail = formData.get("studentemail");
            } else {
                studentEmail = "";
            }

            // Extract the time index and convert it from String to int
            int periodIndex = -1;
            if (formData.containsKey("periodindex")) {
                periodIndex = Integer.parseInt(formData.get("periodindex"));
            }

            // Delete the matching arrangement from the database
            if (!studentEmail.isEmpty() && periodIndex != -1) {
                // I choose to use DeleteBuilder because the deletion must satisfy
                // three conditions at the same time: correct teacher email, student email and time period
                // DeleteBuilder (a method from ORMLite) allows us to delete rows based on specific conditions
                DeleteBuilder<Arrangement, Integer> deleteBuilder = arrangementDao.deleteBuilder();
                // Set three conditions that must all be met simultaneously
                deleteBuilder.where()
                        // Condition 1: The teacher must be the one currently logged in
                        .eq("teacherEmail", currentUser.getEmail())
                        .and()
                        // Condition 2: The student must be the one passed along in the form
                        .eq("studentEmail", studentEmail)
                        .and()
                        // Condition 3: The time period must be accurate
                        .eq("periodIndex", periodIndex);

                // Execute the deletion if all conditions are met
                deleteBuilder.delete();
            }

            // Redirect back to view-matches page by adding "?canceled=true" to the URL
            exchange.getResponseHeaders().add("Location", Routes.VIEW_MATCHES + "?canceled=true");
            exchange.sendResponseHeaders(302, -1);

            // Show the error
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Failed to cancel arrangement.");
        }
    }
}