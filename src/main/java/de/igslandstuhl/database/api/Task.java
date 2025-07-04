package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

/**
 * Represents a task in the student database.
 * Tasks are associated with topics and have different levels of difficulty.
 */
public class Task {
    /**
     * SQL fields for the Task table.
     * Used for database queries to retrieve task information.
     */
    private static final String[] SQL_FIELDS = {"id", "topic", "name", "niveau"};
    /**
     * A map to cache tasks by their unique identifier.
     * This helps avoid repeated database queries for the same task.
     */
    private static final Map<Integer, Task> tasks = new HashMap<>();
    /**
     * The unique identifier for the task.
     */
    private final int id;
    /**
     * The topic associated with the task.
     * This is the subject area to which the task belongs.
     */
    private final Topic topic;
    /**
     * The name of the task.
     * This is a human-readable name for the task.
     */
    private final String name;
    /**
     * The level of difficulty for the task.
     * This indicates how challenging the task is, such as LEVEL1, LEVEL2, or LEVEL3.
     * It is also used to calculate the task's ratio in relation to the topic.
     */
    private final Level niveau;

    /**
     * Constructs a new Task.
     *
     * @param id    the unique identifier for the task
     * @param topic the topic associated with the task
     * @param name  the name of the task
     * @param niveau the level of difficulty for the task
     */
    private Task(int id, Topic topic, String name, Level niveau) {
        this.id = id;
        this.topic = topic;
        this.name = name;
        this.niveau = niveau;
    }
    
    /**
     * Returns the unique identifier of the task.
     * This is used to identify the task in various operations.
     *
     * @return the unique identifier of the task
     */
    public int getId() {
        return id;
    }
    /**
     * Returns the topic associated with the task.
     * This is the subject area to which the task belongs.
     *
     * @return the topic associated with the task
     */
    public Topic getTopic() {
        return topic;
    }
    /**
     * Returns the name of the task.
     * This is a human-readable name for the task.
     *
     * @return the name of the task
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the level of difficulty for the task.
     * This indicates how challenging the task is, such as LEVEL1, LEVEL2, or LEVEL3.
     *
     * @return the level of difficulty for the task
     */
    public Level getNiveau() {
        return niveau;
    }
    /**
     * Returns the number of the task in relation to its topic and level.
     * The number is formatted as "topicNumber.level.taskIndex".
     * For example, if the topic number is 1, and this is the first task at level 1, it would return "1.1.1".
     *
     * @return the formatted number of the task
     */
    public String getNumber() {
        switch (niveau) {
            case LEVEL1:
                return topic.getNumber() + ".1." + (topic.getTasksLevel1().indexOf(this) + 1);
            case LEVEL2:
                return topic.getNumber() + ".2." + (topic.getTasksLevel2().indexOf(this) + 1 + topic.getTasksLevel1().size());
            case LEVEL3:
                return topic.getNumber() + ".3." + (topic.getTasksLevel3().indexOf(this) + 1 + topic.getTasksLevel1().size() + topic.getTasksLevel2().size());
            default:
                throw new IllegalStateException("Unknown level: " + niveau);
        }
    }
    /**
     * Returns the ratio of the task in relation to its topic and level.
     * The ratio is calculated based on the topic's ratio and the number of tasks at the same level.
     *
     * @return the ratio of the task
     */
    public double getRatio() {
        return niveau.getRatio() * topic.getRatio() / (100.0 * topic.getTasksByLevel(niveau).size());
    }

    /**
     * Creates a Task object from SQL query result fields.
     *
     * @param fields the SQL fields retrieved from the database
     * @return a Task object populated with the retrieved data
     */
    private static Task fromSQLFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        Topic topic = Topic.get(Integer.parseInt(fields[1]));
        String name = fields[2];
        Level niveau = Level.get(Integer.parseInt(fields[3]));
        return new Task(id, topic, name, niveau);
    }
    /**
     * Retrieves a Task by its unique identifier.
     * If the task is cached, it returns the cached version.
     * Otherwise, it queries the database for the task.
     *
     * @param id the unique identifier of the task
     * @return the Task object if found, or null if not found
     */
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
    /**
     * Retrieves a list of tasks by their unique identifiers.
     * This method queries the database for each task ID and returns a list of Task objects.
     *
     * @param ids the list of unique identifiers for the tasks
     * @return a list of Task objects corresponding to the provided IDs
     */
    public static List<Task> getTasksByIds(List<Integer> ids) {
        List<Task> tasks = new ArrayList<>();
        for (Integer id : ids) {
            Task task = Task.get(id);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    @Override
    public String toString() {
        return "{\"id\": " + id + ", \"topic\": " + topic + ", \"name\": \"" + name + "\", \"niveau\": " + niveau + ", \"number\": \"" + getNumber() + "\", \"ratio\": " + getRatio() + "}";
    }

    /**
     * Adds a new task to the database.
     * This method creates a new task associated with a specific topic and level of difficulty.
     *
     * @param topic the topic to which the task belongs, or null if not associated with any topic
     * @param name  the name of the task
     * @param niveau the level of difficulty for the task
     * @throws SQLException if there is an error accessing the database
     */
    public static void addTask(Topic topic, String name, Level niveau) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("task", topic == null ? "-1" : String.valueOf(topic.getId()), name, String.valueOf(niveau)));
    }
}
