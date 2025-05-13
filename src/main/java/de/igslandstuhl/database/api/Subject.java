package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.Server;

public class Subject {
    private static final Map<Integer, Subject> subjects = new HashMap<>();
    private static final String[] SQL_FIELDS = {"id", "name"};
    private final int id;
    private final String name;
    private Subject(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    private static Subject fromSQLFields(String[] fields) {
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
    @Override
    public String toString() {
        return "{\"id\": " + id + ", \"name\": \"" + name + "\"}";
    }

    
}
