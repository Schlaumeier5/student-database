package de.igslandstuhl.database.api.results;

import de.igslandstuhl.database.api.Student;

public class StudentGenerationResult extends GenerationResult<Student> {
    public StudentGenerationResult(Student student, String password) {
        super(student, password);
    }
    public Student getStudent() {
        return getEntity();
    }
    @Override
    public String toCSVRow() {
        return new StringBuilder().append(this.getStudent().getId()).append(",")
            .append(this.getStudent().getFirstName()).append(",")
            .append(this.getStudent().getLastName()).append(",")
            .append(this.getStudent().getEmail()).append(",")
            .append(this.getPassword()).toString();
    }
}