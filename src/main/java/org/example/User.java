package org.example;

import com.j256.ormlite.field.DatabaseField;

/**
 * This is the abstract Base Class for both Student and Teacher.
 * It demonstrates OOP Inheritance and Abstraction for the IA project.
 * It holds all common attributes shared by any user of the system.
 */
public abstract class User {
    // Protected access modifier allows subclasses to access these fields directly
    @DatabaseField(id = true, canBeNull = false)
    protected String email;

    @DatabaseField(canBeNull = false)
    protected String name;

    @DatabaseField(canBeNull = false)
    protected String password;

    // Default availability is an empty timetable (45 zeros)
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

    // Common Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
}