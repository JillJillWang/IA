package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Routes;
import org.example.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
     * This method is used to parses data from HTTP into a Student object
     * @param exchange HttpExchange object, the carrier of HTTP requests and responses
     * @return Return validated Student object
     * @throws IOException The exception to inputs and outputs
     */
    private static Student getStudent(HttpExchange exchange) throws IOException {
        // Read the data from the database using BufferedReader
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        // Create a new object to store parsed form data
        Student newStudent = new Student();
        // Read data as a single string (format: email=user@example.com&name=John&classNum=1&password=123)
        String query = br.readLine();

        // Split the query string by "&"
        String[] vars = query.split("&");
        // Iterate over each keyvalue pair to extract and map data to Student object
        for (String var : vars) {
            // Split each pair by "=" to separate key (field name) and value (user input)
            String[] keyvalue = var.split("=");

            // Map parsed value to corresponding Student field,
            // assign the value input by the user to the Student object
            // Validate that the field is one of the expected registration fields
            if (keyvalue[0].equalsIgnoreCase("email")) {
                newStudent.setEmail(keyvalue[1]);
            } else if (keyvalue[0].equalsIgnoreCase("name")) {
                newStudent.setName(keyvalue[1]);
            } else if (keyvalue[0].equalsIgnoreCase("password")) {
                newStudent.setPassword(keyvalue[1]);
            } else if (keyvalue[0].equalsIgnoreCase("classnum")) {
                newStudent.setClassNum(Integer.parseInt(keyvalue[1]));
            } else {
                throw new IOException("Input is not valid: " + keyvalue[0]);
            }
        }


        // Make sure that all required fields are not empty
        if (newStudent.getEmail() == null || newStudent.getName() == null
                || newStudent.getPassword() == null) {
            throw new IOException("All fields (email, name, password, classNum) are required");
        }
        return newStudent;

    }


}
