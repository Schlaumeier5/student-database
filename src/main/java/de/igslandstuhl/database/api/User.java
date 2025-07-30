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
     * Checks if the user is an admin.
     * This method should be implemented by subclasses to determine if the user is an admin.
     * @return true if the user is an admin, false otherwise
     */
    public abstract boolean isAdmin();

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
        Student student = Student.getByEmail(username);
        if (student != null) return student;
        Teacher teacher = Teacher.fromEmail(username);
        if (teacher != null) return teacher;
        Admin admin = Admin.get(username);
        if (admin != null) return admin;
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

    /**
     * Converts the user to a Teacher object if possible.
     * @return the Teacher object if the user is a teacher, or null if not
     */
    public Teacher asTeacher() {
        if (this instanceof Teacher) {
            return (Teacher) this;
        }
        return null;
    }
    /**
     * Converts the user to a Student object if possible.
     * This method checks if the user is an instance of Student and returns it.
     * @return the Student object if the user is a student, or null if not
     */
    public Student asStudent() {
        if (this instanceof Student) {
            return (Student) this;
        }
        return null;
    }
    /**
     * Converts the user to an Admin object if possible.
     * This method checks if the user is an instance of Admin and returns it.
     * @return the Admin object if the user is an admin, or null if not
     */
    public Admin asAdmin() {
        if (this instanceof Admin) {
            return (Admin) this;
        }
        return null;
    }
}
