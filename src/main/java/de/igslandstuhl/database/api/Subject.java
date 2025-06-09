package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class Subject {
    private static final Map<Integer, Subject> subjects = new HashMap<>();
    static final String[] SQL_FIELDS = {"id", "name"};
    private final int id;
    private final String name;
    Subject(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    static Subject fromSQLFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[1];
        return new Subject(id, name);
    }

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
    public void addToGrade(int grade) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("subject_to_grade", String.valueOf(grade), String.valueOf(id))
        );
    }
    @Override
    public String toString() {
        return "{\"id\": " + id + ", \"name\": \"" + name + "\"}";
    }
    public static void addSubject(String name) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("subject", name));
    }

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
