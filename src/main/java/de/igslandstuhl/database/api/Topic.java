package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.Server;

public class Topic {
    private static final Map<Integer, Topic> topics = new HashMap<>();
    private static final String[] SQL_FIELDS = {"id", "name", "subject", "ratio", "grade"};
    private final int id;
    private final String name;
    private final Subject subject;
    private final int ratio;
    private final int grade;

    public Topic(int id, String name, Subject subject, int ratio, int grade) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.ratio = ratio;
        this.grade = grade;
    }

    private static Topic fromSQLFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[1];
        Subject subject = Subject.get(Integer.parseInt(fields[2]));
        int ratio = Integer.parseInt(fields[3]);
        int grade = Integer.parseInt(fields[4]);
        return new Topic(id, name, subject, ratio, grade);
    }

    public static Topic get(int id) {
        if (topics.keySet().contains(id)) return topics.get(id);
        try {
            Topic topic = Server.getInstance().processSingleRequest(Topic::fromSQLFields, "get_topic_by_id", SQL_FIELDS, String.valueOf(id));
            topics.put(id, topic);
            return topic;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Subject getSubject() {
        return subject;
    }

    public int getRatio() {
        return ratio;
    }

    public int getGrade() {
        return grade;
    }

    @Override
    public String toString() {
        return "{\"id\":" + id + ", \"name\": \"" + name + "\", \"subject\": " + subject + ", \"ratio\": " + ratio + ", \"grade\": " + grade
                + "}";
    }
    
    
}
