package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

/**
 * Represents a subject in the student database.
 * Subjects can be added to grades and have associated topics.
 */
public class Subject {
    /**
     * A map to cache subjects by their unique identifier.
     * This helps avoid repeated database queries for the same subject.
     */
    private static final Map<Integer, Subject> subjects = new HashMap<>();
    /**
     * SQL fields for the Subject table.
     * Used for database queries to retrieve subject information.
     */
    static final String[] SQL_FIELDS = {"id", "name"};
    /**
     * The unique identifier for the subject.
     */
    private final int id;
    /**
     * The name of the subject.
     * This is a human-readable name for the subject.
     */
    private final String name;
    
    /**
     * Constructs a new Subject.
     *
     * @param id   the unique identifier for the subject
     * @param name the name of the subject
     */
    Subject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the unique identifier of the subject.
     * This is used to identify the subject in various operations.
     *
     * @return the unique identifier of the subject
     */
    public int getId() {
        return id;
    }
    /**
     * Returns the name of the subject.
     * This is a human-readable name for the subject.
     *
     * @return the name of the subject
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a Subject object from SQL query result fields.
     * This method is used to convert the fields retrieved from the database into a Subject object.
     *
     * @param fields the SQL fields retrieved from the database
     * @return a Subject object populated with the retrieved data
     */
    static Subject fromSQLFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[1];
        return new Subject(id, name);
    }
    /**
     * Retrieves a Subject by its unique identifier.
     * If the subject is cached, it returns the cached version.
     * Otherwise, it queries the database for the subject.
     *
     * @param id the unique identifier of the subject
     * @return the Subject object if found, or null if not found
     */
    public static Subject get(int id) {
        if (subjects.keySet().contains(id)) return subjects.get(id);
        try {
            Subject subject = Server.getInstance().processSingleRequest(Subject::fromSQLFields, "get_subject_by_id", SQL_FIELDS, String.valueOf(id));
            subjects.put(id, subject);
            return subject;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Adds the subject to a specific grade.
     * This method associates the subject with a grade in the database.
     *
     * @param grade the grade to associate the subject with
     * @throws SQLException if there is an error accessing the database
     */
    public void addToGrade(int grade) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("subject_to_grade", String.valueOf(grade), String.valueOf(id))
        );
    }
    @Override
    public String toString() {
        return "{\"id\": " + id + ", \"name\": \"" + name + "\"}";
    }
    /**
     * Adds a new subject to the database.
     * This method creates a new subject with the specified name.
     *
     * @param name the name of the subject to be added
     * @throws SQLException if there is an error accessing the database
     */
    public static void addSubject(String name) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("subject", name));
    }

    /**
     * Retrieves all topics associated with the subject for a specific grade.
     * This method queries the database to find all topics that belong to the subject for the specified grade.
     *
     * @param grade the grade to retrieve topics for
     * @return a list of topics associated with the subject for the specified grade
     */
    public List<Topic> getTopics(int grade) {
        List<Topic> topics = new ArrayList<>();
        try {
            Server.getInstance().processRequest(
                fields -> {
                    Topic topic = Topic.get(Integer.parseInt(fields[0]));
                    if (topic != null) topics.add(topic);
                },
                "get_topics_by_grade",
                new String[] {"id"},
                String.valueOf(grade),
                String.valueOf(id)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topics;
    }
}
