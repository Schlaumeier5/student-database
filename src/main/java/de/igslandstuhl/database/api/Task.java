package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.Server;

public class Task {
    private static final String[] SQL_FIELDS = {"id", "topic", "name", "niveau"};
    private static final Map<Integer, Task> tasks = new HashMap<>();
    private final int id;
    private final Topic topic;
    private final String name;
    private final Level niveau;

    private Task(int id, Topic topic, String name, Level niveau) {
        this.id = id;
        this.topic = topic;
        this.name = name;
        this.niveau = niveau;
    }
    
    public int getId() {
        return id;
    }
    public Topic getTopic() {
        return topic;
    }
    public String getName() {
        return name;
    }
    public Level getNiveau() {
        return niveau;
    }

    private static Task fromSQLFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        Topic topic = Topic.get(Integer.parseInt(fields[1]));
        String name = fields[2];
        Level niveau = Level.get(Integer.parseInt(fields[3]));
        return new Task(id, topic, name, niveau);
    }

    public static Task get(int id) {
        if (tasks.keySet().contains(id)) return tasks.get(id);
        try {
            Task task = Server.getInstance().processSingleRequest(Task::fromSQLFields, "get_task_by_id", SQL_FIELDS, String.valueOf(id));
            tasks.put(id, task);
            return task;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{\"id\": " + id + ", \"topic\": " + topic + ", \"name\": \"" + name + "\", \"niveau\": " + niveau + "}";
    }
}
