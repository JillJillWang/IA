package org.example;

/**
 * This class uniformly store the path (URL) for all HTTP requests
 * It standardizes the routing rules of the entire project
 */
public class Routes {
    /*
    Project root path
    When accessing http://localhost:8080/, the corresponding home page is displayed
     */
    public static final String ROOT = "/";

    /*
    Path of the student registration form page
    The front end accesses this path to display the registration form
    Corresponding to create-student.html
     */
    public static final String CREATE_STUDENT = "/create-student";

    /*
    Path of student registration submission
    When submitting form data, request this path
    Corresponding to SaveStudentHandler
     */
    public static final String SAVE_STUDENT = "/save-student";

    /*
   Path of the login page
   When the front end accesses this path, the login form is displayed
    */
    public static final String LOGIN = "/login";

    /*
   Path of the error page
   When an error occurs during operation, jump to this page
    */
    public static final String ERROR = "/error";


}
