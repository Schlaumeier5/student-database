package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

/**
 * Represents a school class with its associated subjects and students.
 * Provides methods to fetch subjects and students from the database.
 */
public class SchoolClass {
    /**
     * SQL fields for the SchoolClass table.
     * Used for database queries to retrieve class information.
     */
    private static final String[] SQL_FIELDS = {"id", "label", "grade"};
    /**
     * A list to store subjects associated with this class.
     * Subjects are fetched from the database when needed.
     */
    private final List<Subject> subjects = new ArrayList<>();
    /**
     * The unique identifier for the class.
     */
    private final int id;
    /**
     * The label of the class, which is a human-readable name.
     */
    private final String label;
    /**
     * The grade level of the class, indicating the year or level of education.
     */
    private final int grade;

    /**
     * Constructs a SchoolClass instance with the specified id, label, and grade.
     *
     * @param id    The unique identifier for the class.
     * @param label The label of the class.
     * @param grade The grade level of the class.
     */
    private SchoolClass(int id, String label, int grade) {
        this.id = id;
        this.label = label;
        this.grade = grade;
    }

    /**
     * Returns the unique identifier of the class.
     * This is used to identify the class in various operations.
     *
     * @return the id of the class
     */
    public int getId() {
        return id;
    }
    /**
     * Returns the label of the class.
     * This is used to display the class name in user interfaces.
     *
     * @return the label of the class
     */
    public String getLabel() {
        return label;
    }
    /**
     * Returns the grade level of the class.
     * This is used to determine the educational level of the class.
     *
     * @return the grade level of the class
     */
    public int getGrade() {
        return grade;
    }

    /**
     * Fetches all subjects associated with this class from the database.
     * This method retrieves subjects based on the class's grade level.
     *
     * @throws SQLException if there is an error accessing the database
     */
    public void fetchAllSubjects() throws SQLException {
        Server.getInstance().processRequest((fields) -> {
            Subject subject = Subject.fromSQLFields(fields);
            subjects.add(subject);
        }, "get_subjects_by_grade", Subject.SQL_FIELDS, String.valueOf(grade));
    }
    /**
     * Checks if subjects have already been fetched; if not, fetches them.
     * This method ensures that subjects are loaded only once and avoids unnecessary database calls.
     *
     * @throws SQLException if there is an error accessing the database
     */
    public void fetchAllSubjectsIfNotExists() throws SQLException {
        if (subjects.size() == 0) {
            fetchAllSubjects();
        }
    }
    /**
     * Returns a list of subjects associated with this class.
     * If subjects have not been fetched yet, it fetches them from the database.
     *
     * @return a list of subjects for this class
     */
    public List<Subject> getSubjects() {
        try {
            fetchAllSubjectsIfNotExists();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not fetch subjects", e);
        }
        return subjects;
    }

    /**
     * Retrieves a list of students enrolled in this class from the database.
     * This method queries the database for students associated with the class's id.
     *
     * @return a list of students in this class
     */
    public List<Student> getStudents() {
        List<Student> students = new ArrayList<>();
        try {
            Server.getInstance().processRequest(
                fields -> {
                    Student student = Student.get(Integer.parseInt(fields[0]));
                    if (student != null) students.add(student);
                },
                "get_students_by_class", new String[] {"id"}, String.valueOf(id)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public void delete() throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getDeleteObjectProcess("class", String.valueOf(id)));
    }

    /**
     * Updates the label and grade of this SchoolClass in the database.
     * Returns a new SchoolClass instance with the updated values.
     *
     * @param name  the new label for the class
     * @param grade the new grade level for the class
     * @return the updated SchoolClass instance
     * @throws SQLException when a database error occurs
     */
    public SchoolClass edit(String name, int grade) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("class_with_id", String.valueOf(id), name, String.valueOf(grade))
        );
        return get(id);
    }

    /**
     * Converts a SQL result array into a SchoolClass object.
     * This method is used to create a SchoolClass instance from the database query results.
     *
     * @param sqlResult the result of a SQL query as an array of strings
     * @return a SchoolClass object constructed from the SQL result
     */
    private static SchoolClass fromSQL(String[] sqlResult) {
        int id = Integer.parseInt(sqlResult[0]);
        String label = sqlResult[1];
        int grade = Integer.parseInt(sqlResult[2]);
        return new SchoolClass(id, label, grade);
    }
    /**
     * Retrieves a SchoolClass by its unique identifier from the database.
     * This method queries the database for a class with the specified id.
     *
     * @param id the unique identifier of the class
     * @return a SchoolClass object if found, or null if not found
     */
    public static SchoolClass get(int id) {
        try {
            return Server.getInstance().processSingleRequest(SchoolClass::fromSQL, "get_class_by_id", SQL_FIELDS, String.valueOf(id));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Retrieves a SchoolClass by its label from the database.
     * This method queries the database for a class with the specified label.
     *
     * @param label the label of the class
     * @return a SchoolClass object if found, or null if not found
     */
    public static SchoolClass get(String label) {
        try {
            return Server.getInstance().processSingleRequest(SchoolClass::fromSQL, "get_class_by_label", SQL_FIELDS, label);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static SchoolClass getOrCreate(String label) {
        // remove leading zeros from label
        label = label.replaceFirst("^0+", "").toLowerCase();
        // grade is the first characters of the label that are digits
        int grade = 0;
        StringBuilder gradeStr = new StringBuilder();
        for (char c : label.toCharArray()) {
            if (Character.isDigit(c)) {
                gradeStr.append(c);
            } else {
                break;
            }
        }
        if (gradeStr.length() > 0) {
            grade = Integer.parseInt(gradeStr.toString());
        }
        SchoolClass schoolClass = get(label);
        if (schoolClass == null) {
            try {
                schoolClass = addClass(label, grade);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return schoolClass;
    }

    public static List<SchoolClass> getAll() {
        List<Integer> ids = new ArrayList<>();
        try {
            Server.getInstance().processRequest(
                fields -> ids.add(Integer.parseInt(fields[0])),
                "get_all_classes", new String[] {"id"}
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids.stream()
            .map(SchoolClass::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "{\"id\": " + id + ", \"label\": \"" + label + "\", \"grade\": " + grade + "}";
    }
/**
     * Adds a new class to the database with the specified label and grade.
     * This method constructs a SQL insert statement to add a new class record.
     *
     * @param label the label of the class
     * @param grade the grade level of the class
     * @throws SQLException if there is an error accessing the database
     */
    public static SchoolClass addClass(String label, int grade) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("class", label, String.valueOf(grade)));
        return get(label);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + grade;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SchoolClass other = (SchoolClass) obj;
        if (id != other.id)
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (grade != other.grade)
            return false;
        return true;
    }
    
}
