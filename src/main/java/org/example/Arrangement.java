package org.example;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * This class represents a scheduled meeting between a Teacher and a Student.
 * It maps to the "Arrangement" table in the database.
 */
@DatabaseTable(tableName = "Arrangement")
public class Arrangement {

    // Auto-generated ID for each arrangement (Primary Key)
    @DatabaseField(generatedId = true)
    private int id;

    // The email of the teacher who arranged the meeting
    @DatabaseField(canBeNull = false)
    private String teacherEmail;

    // The email of the student
    @DatabaseField(canBeNull = false)
    private String studentEmail;

    // The index of the period (0-44)
    @DatabaseField(canBeNull = false)
    private int periodIndex;

    // Non-arg constructor required by ORMLite
    public Arrangement() {}

    public Arrangement(String teacherEmail, String studentEmail, int periodIndex) {
        this.teacherEmail = teacherEmail;
        this.studentEmail = studentEmail;
        this.periodIndex = periodIndex;
    }

    // Getters
    public String getTeacherEmail() {
        return teacherEmail;
    }
    public String getStudentEmail() {
        return studentEmail;
    }
    public int getPeriodIndex() {
        return periodIndex;
    }
}