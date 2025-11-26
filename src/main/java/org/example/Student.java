package org.example;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Student")
public class Student {
    @DatabaseField(id=true, canBeNull = false)
    private String email;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String password;
    @DatabaseField(canBeNull = false)
    private int classNum;

    // Non-arg constructor
    public Student() {
    }
    // Constructor
    public Student(String email, String name, String password, int classNum){
        this.email = email;
        this.name = name;
        this.password = password;
        this.classNum = classNum;
    }
    // Checks if the password of this user account is the given password
    public boolean hasPassword(String password) {
        return this.password.equals(password);
    }

    // Getters and setters
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setClassNum(int classNum) {
        this.classNum = classNum;
    }
    public int getClassNum() {
        return classNum;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
}
