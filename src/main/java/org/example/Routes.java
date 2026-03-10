package org.example;

/**
 * This class uniformly store the path (URL) for all HTTP requests
 * It standardizes the routing rules of the entire project
 */
public class Routes {

    // Project root path
    // When accessing http://localhost:8080/, the corresponding home page is displayed
    public static final String ROOT = "/";

    // Path of the student registration form page
    // The front end accesses this path to display the registration form
    // Corresponding to create-student.html
    public static final String CREATE_STUDENT = "/create-student";

    //Path of student registration submission
    //When submitting form data, request this path
    //Corresponding to SaveStudentHandler
    public static final String SAVE_STUDENT = "/save-student";

    // Path of the teacher registration form page
    public static final String CREATE_TEACHER = "/create-teacher";

    //Path of teacher registration submission
    public static final String SAVE_TEACHER = "/save-teacher";

   // Path of the login page
   // When the front end accesses this path, the login form is displayed
    public static final String STUDENT_LOGIN = "/student-login";
    public static final String TEACHER_LOGIN = "/teacher-login";

    // Path to process the login form submission
    public static final String DO_LOGIN = "/do-login";

    // Path for the student's personal timetable page
    public static final String TIMETABLE = "/timetable";

    // Path to process the manual saving of the timetable
    public static final String SAVE_TIMETABLE = "/save-timetable";

    // Paths for the teacher's dashboard
    public static final String TEACHER_DASHBOARD = "/teacher-dashboard";


   // Path of the error page
   // When an error occurs during operation, jump to this page
    public static final String ERROR = "/error";


}
