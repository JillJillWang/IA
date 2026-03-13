package org.example;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Student class extends the User base class.
 * It inherits common fields and adds student-specific attributes.
 */
@DatabaseTable(tableName = "Student")
public class Student extends User {

    // Student specific field
    @DatabaseField(canBeNull = false)
    private int classNum;

    // Non-arg constructor required by ORMLite
    public Student() {
        super();
    }

    public Student(String email, String name, String password, int classNum) {
        // Assign inherited fields
        this.email = email;
        this.name = name;
        this.password = password;
        this.availability = "000000000000000000000000000000000000000000000";

        // Assign specific fields
        this.classNum = classNum;
    }

    public int getClassNum() { return classNum; }
    public void setClassNum(int classNum) { this.classNum = classNum; }
}