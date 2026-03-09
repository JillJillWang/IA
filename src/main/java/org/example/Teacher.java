package org.example;

import com.j256.ormlite.table.DatabaseTable;

/**
 * Teacher class extends the User base class.
 * Mapped to a separate 'Teacher' table in the database.
 */
@DatabaseTable(tableName = "Teacher")
public class Teacher extends User {

    // Non-arg constructor
    public Teacher() {
        super();
    }

    public Teacher(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.availability = "000000000000000000000000000000000000000000000";
    }
}