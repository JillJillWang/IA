package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles canceling an existing arrangement when a teacher clicks the "Cancel" button.
 * Uses standard HTML form submission and redirects back to the view-matches page.
 */
public class CancelArrangementHandler implements HttpHandler {
    private final Dao<Arrangement, Integer> arrangementDao;

    public CancelArrangementHandler(Dao<Arrangement, Integer> arrangementDao) {
        this.arrangementDao = arrangementDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only process POST requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) return;

        // Security check: Ensure the user is logged in AND is a Teacher
        String token = HttpUtils.getSessionToken(exchange);
        User currentUser = SessionManager.getUser(token);

        if (currentUser == null || !(currentUser instanceof Teacher)) {
            HttpUtils.showError(exchange, "Access Denied");
            return;
        }

        try {
            // Read data from the HTML form submitted by the Cancel button
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String query = br.readLine();

            String studentEmail = "";
            int periodIndex = -1;

            // Parse key-value pairs (format: studentEmail=...&periodIndex=...)
            if (query != null) {
                String[] vars = query.split("&");
                for (String var : vars) {
                    String[] keyvalue = var.split("=");
                    if (keyvalue.length < 2) continue;
                    String key = keyvalue[0];
                    String value = URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8);

                    if (key.equalsIgnoreCase("studentEmail")) {
                        studentEmail = value;
                    } else if (key.equalsIgnoreCase("periodIndex")) {
                        periodIndex = Integer.parseInt(value);
                    }
                }
            }

            // Delete the matching arrangement from the database
            if (!studentEmail.isEmpty() && periodIndex != -1) {
                // DeleteBuilder allows us to delete rows based on specific conditions
                DeleteBuilder<Arrangement, Integer> deleteBuilder = arrangementDao.deleteBuilder();
                deleteBuilder.where()
                        .eq("teacherEmail", currentUser.getEmail())
                        .and()
                        .eq("studentEmail", studentEmail)
                        .and()
                        .eq("periodIndex", periodIndex);

                // Execute the deletion
                deleteBuilder.delete();
            }

            // Redirect back to view-matches page, and add "?canceled=true" to the URL
            exchange.getResponseHeaders().add("Location", Routes.VIEW_MATCHES + "?canceled=true");
            exchange.sendResponseHeaders(302, -1);

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Failed to cancel arrangement.");
        }
    }
}