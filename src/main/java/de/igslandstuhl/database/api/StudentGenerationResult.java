package de.igslandstuhl.database.api;

public class StudentGenerationResult {
    private final Student student;
    private final String password;

    public StudentGenerationResult(Student student, String password) {
        this.student = student;
        this.password = password;
    }

    public Student getStudent() {
        return student;
    }

    public String getPassword() {
        return password;
    }
}
