package de.igslandstuhl.database.api;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Abstract class representing a user in the system.
 * This class provides methods to check user roles, retrieve password hashes, and convert user data to JSON format.
 */
public abstract class User {
    /**
     * Checks if the user is a teacher.
     * This method should be implemented by subclasses to determine if the user is a teacher.
     * @return true if the user is a teacher, false otherwise
     */
    public abstract boolean isTeacher();

    /**
     * Checks if the user is a student.
     * This method should be implemented by subclasses to determine if the user is a student.
     * @return true if the user is a student, false otherwise
     */
    public abstract boolean isStudent();

    /**
     * Returns the password hash of the user.
     * This method should be implemented by subclasses to provide the user's password hash.
     * @return the password hash of the user
     */
    public abstract String getPasswordHash();
    /**
     * Converts the user data to a JSON string.
     * This method should be implemented by subclasses to provide a JSON representation of the user.
     * @return a JSON string representing the user
     */
    public abstract String toJSON();

    /**
     * Retrieves a user by their username.
     * This method searches for a user in the database using the provided username.
     * @param username the username of the user to retrieve
     * @return the User object if found, or null if not found
     */
    public static User getUser(String username) {
        username = username.replace("%40", "@");
        Student student = Student.fromEmail(username);
        if (student != null) return student;
        Teacher teacher = Teacher.fromEmail(username);
        if (teacher != null) return teacher;
        return null;
    }

    /**
     * Returns the password hash of the user.
     * This method should be implemented by subclasses to provide the user's password hash.
     * @return the password hash of the user
     */
    public static String passHash(String password) {
        return DigestUtils.sha1Hex(password);
    }
}
