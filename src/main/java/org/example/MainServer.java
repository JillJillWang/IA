package org.example;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.example.handlers.*;
import org.h2.tools.Server;

import java.net.InetSocketAddress;


/**
 * MainServer class is responsible for initializing database connection, creating database tables,
 * and starting the HTTP server to handle client requests
 */
public class MainServer {

    /**
     * This method is the entry point of the application
     * It initializes core components and starts the server
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Connect the database with URL（H2 database）
        String databaseUrl = "jdbc:h2:file:./database/student.db"; //how to write the url?
        // ORMLite ConnectionSource: Manages the database connection lifecycle
        ConnectionSource connectionSource;

        {
            try {
                // Start H2 database's web management console
                Server.createWebServer().start();
                connectionSource = new JdbcConnectionSource(databaseUrl);
                // Create database for StudentAccounts
                // Initialize JDBC connection source using the H2 database URL
                TableUtils.createTableIfNotExists(connectionSource, Student.class);

                // Instantiate the DAO to handle the database
                Dao<Student, String> studentDao = DaoManager.createDao(connectionSource, Student.class);

                // Create the table if it doesn't exist
                TableUtils.createTableIfNotExists(connectionSource, Student.class);

                // Create Teacher table
                TableUtils.createTableIfNotExists(connectionSource, Teacher.class);
                Dao<Teacher, String> teacherDao = DaoManager.createDao(connectionSource, Teacher.class);


                // Initialize the HTTP server
                // http://localhost:8080
                HttpServer server = HttpServer.create(new InetSocketAddress(8080),0);

                // Create the context
                HttpContext IndexCtx = server.createContext(Routes.ROOT, new StaticFileHandler(FilePaths.INDEX));
                // Context of creating student's account page
                server.createContext(Routes.CREATE_STUDENT, new StaticFileHandler(FilePaths.CREATE_STUDENT));
                // Save student's account
                server.createContext(Routes.SAVE_STUDENT, new SaveStudentHandler(studentDao));
                // Create teacher's account page
                server.createContext(Routes.CREATE_TEACHER, new StaticFileHandler(FilePaths.CREATE_TEACHER));
                // Save teacher's account
                server.createContext(Routes.SAVE_TEACHER, new SaveTeacherHandler(teacherDao));
                // Log in
                server.createContext(Routes.LOGIN, new StaticFileHandler(FilePaths.LOGIN));
                // Login process (handles the form submission)
                server.createContext(Routes.DO_LOGIN, new LoginHandler(studentDao, teacherDao));
                // Timetable page
                server.createContext(Routes.TIMETABLE, new TimetableHandler());
                // Save timetable process
                server.createContext(Routes.SAVE_TIMETABLE, new SaveTimetableHandler(studentDao, teacherDao));
                // Error page: Return to the error page when handling an exception
                server.createContext(Routes.ERROR, new StaticFileHandler(FilePaths.ERROR));

                // Create a default executor
                server.setExecutor(null);
                // Start the HTTP server
                server.start();
                System.out.println("The server has successfully started：http://localhost:8080");




            } catch (Exception e) {
                // Print detailed exception stack trace for debugging
                e.printStackTrace();
                // Wrap checked exception into RuntimeException and rethrow to terminate application
                throw new RuntimeException("Server startup failed" + e);
            }
        }
    }

}
