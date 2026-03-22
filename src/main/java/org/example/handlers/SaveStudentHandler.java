package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.HttpUtils;
import org.example.Routes;
import org.example.Student;

import java.io.IOException;
import java.util.Map;

import static org.example.HttpUtils.showError;

/**
 * This class is the HTTP handler for processing student registration form submissions
 * It is responsible for parsing form data, validating inputs, saving student to database,
 * and handling redirect/error responses
 */
public class SaveStudentHandler implements HttpHandler{
    private final Dao<Student, String> studentDao; // add DAO

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
        // Security check: Only process POST requests
        // POST is an HTTP method used to submit data to a server to create or update resources
        // Unlike GET (data exposed in URL), POST sends data in the request body
        // This makes POST far more secure for transmitting sensitive data
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            return;
        }

        try {
            // Retrieve the parsed form data as a HashMap
            Map<String, String> formData = HttpUtils.parseFormData(exchange);
            // Create a new Student object
            Student newStudent = new Student();

            // Extract the values of the Student object
            if (formData.containsKey("email")) {
                newStudent.setEmail(formData.get("email").toLowerCase());
            }
            if (formData.containsKey("name")) {
                newStudent.setName(formData.get("name"));
            }
            if (formData.containsKey("password")) {
                newStudent.setPassword(formData.get("password"));
            }
            // Extract and convert String to Integer
            if (formData.containsKey("classnum")) {
                newStudent.setClassNum(Integer.parseInt(formData.get("classnum")));
            }

            // A basic validation to ensure all fields are present
            // Make sure that no crucial information is omitted;
            // otherwise, the creation will be rejected.
            if ((newStudent.getEmail() == null) || (newStudent.getName() == null)
                    || (newStudent.getPassword() == null) || (Integer.valueOf(newStudent.getClassNum()) == null)) {
                throw new IOException("All fields (email, name, password, classNum) are required");
            }

            /*
             * Check whether the email address has been registered
             * Send error response and show error message if the email has been registered
             *
             * queryForId:
             * A query method provided by ORMLite. By giving it a unique identifier (e.g., an email address),
             * it will search for the corresponding piece of data in the database.
             * If it finds it, it will return that data; if not, it will return null.
             */
            Student existingEmail = studentDao.queryForId(newStudent.getEmail());

            // If there is an existing same email in database, jump to the error page
            if (existingEmail != null) {
                showError(exchange, "This email address already exists");
                return;
            }
            // If there's no same email, the Student object will be saved in database
            this.studentDao.create(newStudent);

            // Redirect to Student Login page after successful registration
            exchange.getResponseHeaders().add("Location", Routes.STUDENT_LOGIN);
            // 302: redirect code, -1: no response body
            exchange.sendResponseHeaders(302, -1);


        } catch (Exception e) {
            // Print exception stack trace for debugging
            e.printStackTrace();
            // Send error response and show error message
            showError(exchange, e.getMessage());
        }
    }
}