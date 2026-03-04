package org.example;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Student")
public class Student {
    // Primary key: unique email address for each student
    @DatabaseField(id = true, canBeNull = false)
    private String email;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private String password;

    @DatabaseField(canBeNull = false)
    private int classNum;

    // A string of 45 characters (5 days * 9 periods) to store availability
    // '0' means busy, '1' means available. Default is all busy ('0's)
    @DatabaseField(defaultValue = "000000000000000000000000000000000000000000000")
    private String availability;

    // Unix timestamp (milliseconds) of the last meeting to support the 2-week reminder criteria
    @DatabaseField(defaultValue = "0")
    private long lastMeetingTimestamp;

    // Non-arg constructor required by ORMLite
    public Student() {
    }

    public Student(String email, String name, String password, int classNum) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.classNum = classNum;
        this.lastMeetingTimestamp = System.currentTimeMillis(); // Initialize with current time
    }

    // Logic to check whether the password is correct or not
    public boolean hasPassword(String password) {
        return this.password.equals(password);
    }

    // Getters and Setters
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setClassNum(int classNum) { this.classNum = classNum; }
    public int getClassNum() { return classNum; }

    public void setPassword(String password) { this.password = password; }
    public String getPassword() { return password; }

    public void setAvailability(String availability) { this.availability = availability; }
    public String getAvailability() { return availability; }

    public void setLastMeetingTimestamp(long lastMeetingTimestamp) { this.lastMeetingTimestamp = lastMeetingTimestamp; }
    public long getLastMeetingTimestamp() { return lastMeetingTimestamp; }
}