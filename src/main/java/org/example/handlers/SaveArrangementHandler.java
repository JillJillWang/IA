package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;

import java.io.IOException;


/**
 * This class handles saving the arrangement when a teacher clicks the "Arrange" button.
 * It uses HTML form submission. After the submission, it redirects to the view-matches page.
 */
public class SaveArrangementHandler implements HttpHandler {
    private final Dao<Arrangement, Integer> arrangementDao;

    public SaveArrangementHandler(Dao<Arrangement, Integer> arrangementDao) {
        this.arrangementDao = arrangementDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only process POST request
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            return;
        }

        // Get the token and check who it is in the HashMap
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        // Authentication & Authorization
        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied");
            return;
        }

        try {
            // Get the HashMap
            java.util.Map<String, String> formData = HttpUtils.parseFormData(exchange);
            // Extract data from the HashMap
            // Note: Since HttpUtils converts all keys to lowercase,
            // here I must use "studentemail"
            String studentEmail;
            if (formData.containsKey("studentemail")) {
                studentEmail = formData.get("studentemail");
            } else {
                studentEmail = "";
            }

            // Set a flag to determine whether the data extraction is successful
            int periodIndex = -1;
            // Note: Since HttpUtils converts all keys to lowercase,
            // here I must us "periodindex"
            if (formData.containsKey("periodindex")) {
                // Convert the extracted String numbers into int
                periodIndex = Integer.parseInt(formData.get("periodindex"));
            }

            // Save the new arrangement to the database
            // Make sure the email is not empty and the class schedule index is correct
            if (!studentEmail.isEmpty() && periodIndex != -1) {
                // Create an Arrangement object
                Arrangement arrangement = new Arrangement(currentUser.getEmail(), studentEmail, periodIndex);
                // Call the create method of ORMLite to store the object in the database
                arrangementDao.create(arrangement);
            }

            // Redirect back to view-matches page by adding '?success=true' to the URL (query string)
            exchange.getResponseHeaders().add("Location", Routes.VIEW_MATCHES + "?success=true");
            exchange.sendResponseHeaders(302, -1);

            // Show the error
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Failed to save arrangement.");
        }
    }
}