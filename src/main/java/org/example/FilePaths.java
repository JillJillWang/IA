package org.example;

/**
 * This class is used to uniformly manages the relative paths of static files in the project
 * to avoid hardcoding and ensure consistency of file path references across the project
 */
public class FilePaths {


    // Root directory path for all static files such as HTML, CSS, JS, etc.
    // Serves as the base path for other static file paths
    public static final String WWW_ROOT = "/www/";

    // Path to the index page HTML file
    public static final String INDEX = WWW_ROOT + "index.html";

    // Path to the student registration HTML file
    public static final String CREATE_STUDENT = WWW_ROOT + "create-student.html";

    // Path to the teacher registration HTML file
    public static final String CREATE_TEACHER = WWW_ROOT + "create-teacher.html";

    // Path to the log in HTML file
    public static final String LOGIN = WWW_ROOT + "login.html";

    // Path to the timetable HTML template
    public static final String TIMETABLE = WWW_ROOT + "timetable.html";

    // Path to the error page HTML file
    // Used to display error information
    public static final String ERROR = WWW_ROOT + "error.html";


}
