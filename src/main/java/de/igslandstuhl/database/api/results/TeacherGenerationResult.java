package de.igslandstuhl.database.api.results;

import de.igslandstuhl.database.api.Teacher;

public class TeacherGenerationResult extends GenerationResult<Teacher> {
    public TeacherGenerationResult(Teacher teacher, String password) {
        super(teacher, password);
    }

    public Teacher getTeacher() {
        return getEntity();
    }
    
    public int getId() {
        return getTeacher().getId();
    }
    public String getFirstName() {
        return getTeacher().getFirstName();
    }
    public String getLastName() {
        return getTeacher().getLastName();
    }
    public String getEmail() {
        return getTeacher().getEmail();
    }
}
