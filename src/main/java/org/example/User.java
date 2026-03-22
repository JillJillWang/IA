package org.example;

import com.j256.ormlite.field.DatabaseField;

/**
 * This is the abstract Base Class for both Student and Teacher.
 * It demonstrates OOP Inheritance and Abstraction for the IA project.
 * It holds all common attributes shared by any user of the system.
 */

/*
 * "Abstract" means that you cannot directly create a "User";
 * instead, you can only create its subclasses (such as "Student" or "Teacher").
 * I discovered that if User is just a regular class, I might accidentally instantiate
 * a generic user without a specific role (student or teacher).
 * To prevent this logical flaw, I self-studied, and expanded my IA to
 * include the use of 'abstract'.
 * By making User an abstract class, the compiler can prevent such errors
 * from occurring at the compilation level, significantly enhancing the rigor of the code.
 */
public abstract class User {
    // Protected access modifier allows subclasses to access these fields directly

    // When id = true, it indicates that email address is
    // the sole identification number (primary key) of this person
    @DatabaseField(id = true, canBeNull = false)
    protected String email;

    @DatabaseField(canBeNull = false)
    protected String name;

    @DatabaseField(canBeNull = false)
    protected String password;


    // I Use 45 zeros to represent the initial idle schedule (5 days a week * 9 classes per day = 45).
    // 0 represents busy, 1 represents idle.
    @DatabaseField(defaultValue = "000000000000000000000000000000000000000000000")
    protected String availability;

    // Default constructor required by ORMLite
    public User() {}

    /**
     * Checks if the inputted password matches the database record.
     * @param password The raw password string to check
     * @return true if password is correct
     */
    public boolean hasPassword(String password) {
        return this.password != null && this.password.equals(password);
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvailability() {
        return availability;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
    }
}