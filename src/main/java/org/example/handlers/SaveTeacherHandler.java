package org.example.handlers;

import com.j256.ormlite.dao.Dao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Routes;
import org.example.Teacher;
import org.example.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles the registration of new Teacher accounts.
 * This class follows the same logic as SaveStudentHandler but targets the Teacher table.
 */
public class SaveTeacherHandler implements HttpHandler {
    private final Dao<Teacher, String> teacherDao;

    public SaveTeacherHandler(Dao<Teacher, String> teacherDao) {
        this.teacherDao = teacherDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) return;

        try {
            // Parse teacher data from form
            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            String query = br.readLine();

            Teacher newTeacher = new Teacher();
            String[] vars = query.split("&");
            for (String var : vars) {
                String[] keyvalue = var.split("=");
                if (keyvalue.length < 2) continue;
                String key = keyvalue[0];
                String value = URLDecoder.decode(keyvalue[1], StandardCharsets.UTF_8);

                if (key.equalsIgnoreCase("email")) newTeacher.setEmail(value.toLowerCase());
                else if (key.equalsIgnoreCase("name")) newTeacher.setName(value);
                else if (key.equalsIgnoreCase("password")) newTeacher.setPassword(value);
            }

            // Check for existing email in Teacher table
            if (teacherDao.queryForId(newTeacher.getEmail()) != null) {
                HttpUtils.showError(exchange, "This teacher email is already registered.");
                return;
            }

            // Save and redirect to the teacher login page
            teacherDao.create(newTeacher);
            exchange.getResponseHeaders().add("Location", Routes.TEACHER_LOGIN);
            exchange.sendResponseHeaders(302, -1);

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.showError(exchange, "Teacher registration failed.");
        }
    }
}