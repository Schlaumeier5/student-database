package de.igslandstuhl.database.api.results;

import de.igslandstuhl.database.api.Student;

public class StudentGenerationResult extends GenerationResult<Student> {
    public StudentGenerationResult(Student student, String password) {
        super(student, password);
    }
    public Student getStudent() {
        return getEntity();
    }
}