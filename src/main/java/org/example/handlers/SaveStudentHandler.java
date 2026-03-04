package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Routes;
import org.example.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.example.HttpUtils.showError;

/**
 * This class is the HTTP handler for processing student registration form submissions
 * It is responsible for parsing form data, validating inputs, saving student to database,
 * and handling redirect/error responses
 */
public class SaveStudentHandler implements HttpHandler{
    private final Dao<Student, String> studentDao; // add DAO (Data Access Object)


    /**
     * The constructor that receives Student DAO
     * @param studentDao ORMLite DAO instance for Student entity. It manages database interactions.
     */
    public SaveStudentHandler(Dao<Student, String> studentDao) {
        this.studentDao = studentDao;
    }

    /**
     * This is the core method to handle incoming HTTP requests
     * @param exchange HttpExchange object: Wraps request/response details
     * @throws IOException The exception to inputs and outputs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // Convert data from HTML form into a Student object
            Student student = getStudent(exchange);

            /*
            Check whether the email address has been registered
            Send error response and show error message if the email has been registered

            queryForId:
            A query method provided by ORMLite. By giving it a unique identifier (e.g., an email address),
            it will search for the corresponding piece of data in the database.
            If it finds it, it will return that data; if not, it will return null.
             */
            Student existingEmail = studentDao.queryForId(student.getEmail());
            if (existingEmail != null) {
                showError(exchange, "This email address already exists");
            }

            // Save the Student object to the database using ORMLite DAO
            this.studentDao.create(student);
            // Create the initialization setting - home page - for redirect
            exchange.getResponseHeaders().add("Location", Routes.ROOT);
            exchange.sendResponseHeaders(302, -1); // 302: redirect code, -1: no response body



        } catch (Exception e) {
            // Print exception stack trace for debugging
            e.printStackTrace();
            // Send error response and show error message
            showError(exchange, e.getMessage());
        }
    }


    /**
     * This method parses data from HTTP into a Student object and decodes URL entities.
     * I used URLDecoder to ensure special characters like '@' are stored correctly.
     * @param exchange HttpExchange object, the carrier of HTTP requests and responses
     * @return Return validated Student object
     * @throws IOException The exception to inputs and outputs
     */
    private static Student getStudent(HttpExchange exchange) throws IOException {
        // Read the data from the request body
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        Student newStudent = new Student();
        String query = br.readLine();

        if (query == null || query.isEmpty()) {
            throw new IOException("Request body is empty");
        }

        // Split the query string by "&"
        String[] vars = query.split("&");
        for (String var : vars) {
            String[] keyvalue = var.split("=");
            if (keyvalue.length < 2) continue;

            // Decode the value to handle special characters like @ in emails
            String key = keyvalue[0];
            String value = java.net.URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8);

            if (key.equalsIgnoreCase("email")) {
                newStudent.setEmail(value);
            } else if (key.equalsIgnoreCase("name")) {
                newStudent.setName(value);
            } else if (key.equalsIgnoreCase("password")) {
                newStudent.setPassword(value);
            } else if (key.equalsIgnoreCase("classnum")) {
                newStudent.setClassNum(Integer.parseInt(value));
            }
        }

        // Basic validation to ensure all fields are present
        if (newStudent.getEmail() == null || newStudent.getName() == null
                || newStudent.getPassword() == null) {
            throw new IOException("All fields (email, name, password, classNum) are required");
        }
        return newStudent;
    }


}
