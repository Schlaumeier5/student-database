package de.igslandstuhl.database.api;

import org.apache.commons.codec.digest.DigestUtils;

public abstract class User {
    public abstract boolean isTeacher();
    public abstract boolean isStudent();
    public abstract String getPasswordHash();
    public abstract String toJSON();

    public static User getUser(String username) {
        username = username.replace("%40", "@");
        Student student = Student.fromEmail(username);
        if (student == null) {
            return null;
        }
        return student;
    }
    public static String passHash(String password) {
        return DigestUtils.sha1Hex(password);
    }
}
