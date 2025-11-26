package org.example;

/**
 * This class is used to create unified replacement tags for the back-end code and HTML template conventions
 * to avoid hardcoding and ensure consistency of placeholders
 *
 * Definitions:
 * Hardcoding:
 * refers to a programming approach that directly embeds fixed values (such as strings,
 * numbers, paths, etc.) into the source code, rather than managing them uniformly through constants,
 * databases, and other means.
 * It is prone to errors and with poor scalability
 *
 * Placeholder:
 * Occupy a position with a string of a specific format,
 * and then replace it with actual dynamic data/message later.
 */
public class Templates {
    // A placeholder that corresponds to the messages (error/success information, etc.)
    public static final String MESSAGE = "{{MESSAGE}}";
    // A placeholder that corresponds to the jump address of the Confirm/Return button in the HTML template
    public static final String OK = "{{OK_URL}}";


}
