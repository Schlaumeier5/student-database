package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class SchoolClass {
    private static final String[] SQL_FIELDS = {"id", "label", "grade"};
    private final List<Subject> subjects = new ArrayList<>();
    private final int id;
    private final String label;
    private final int grade;

    private SchoolClass(int id, String label, int grade) {
        this.id = id;
        this.label = label;
        this.grade = grade;
    }

    public int getId() {
        return id;
    }
    public String getLabel() {
        return label;
    }
    public int getGrade() {
        return grade;
    }

    public void fetchAllSubjects() throws SQLException {
        Server.getInstance().processRequest((fields) -> {
            Subject subject = Subject.fromSQLFields(fields);
            subjects.add(subject);
        }, "get_subjects_by_grade", SQL_FIELDS, String.valueOf(grade));
    }
    public void fetchAllSubjectsIfNotExists() throws SQLException {
        if (subjects.size() == 0) {
            fetchAllSubjects();
        }
    }
    public List<Subject> getSubjects() {
        try {
            fetchAllSubjectsIfNotExists();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not fetch rooms", e);
        }
        return subjects;
    }

    private static SchoolClass fromSQL(String[] sqlResult) {
        int id = Integer.parseInt(sqlResult[0]);
        String label = sqlResult[1];
        int grade = Integer.parseInt(sqlResult[2]);
        return new SchoolClass(id, label, grade);
    }

    public static SchoolClass get(int id) {
        try {
            return Server.getInstance().processSingleRequest(SchoolClass::fromSQL, "get_class_by_id", SQL_FIELDS, String.valueOf(id));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{\"id\": " + id + ", \"label\": \"" + label + "\", \"grade\": " + grade + "}";
    }

    public static void addClass(String label, int grade) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("class", label, String.valueOf(grade)));
    }
}
