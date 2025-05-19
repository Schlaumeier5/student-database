package de.igslandstuhl.database.api;

import java.sql.SQLException;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class SchoolClass {
    private static final String[] SQL_FIELDS = {"id", "label", "grade"};
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

    private static SchoolClass fromSQL(String[] sqlResult) {
        int id = Integer.parseInt(sqlResult[0]);
        String label = sqlResult[1];
        int grade = Integer.parseInt(sqlResult[3]);
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
        return "{\"id\": " + id + ", \"label\": \"" + label + ", \"grade\": " + grade + "}";
    }

    public static void addClass(String label, int grade) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("class", label, String.valueOf(grade)));
    }
}
