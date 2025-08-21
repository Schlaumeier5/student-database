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

    @Override
    public String toCSVRow() {
        return new StringBuilder().append(this.getId()).append(",")
                .append(this.getFirstName()).append(",")
                .append(this.getLastName()).append(",")
                .append(this.getEmail()).append(",")
                .append(this.getPassword()).toString();
    }
}
