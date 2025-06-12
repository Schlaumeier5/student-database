package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class Topic {
    private static final Map<Integer, Topic> topics = new HashMap<>();
    private static final String[] SQL_FIELDS = {"id", "name", "subject", "ratio", "grade", "number"};
    private final int id;
    private final String name;
    private final Subject subject;
    private final int ratio;
    private final int grade;
    private final int number;
    private List<Task> tasks = new ArrayList<>();
    private List<Task> tasksLevel1 = new ArrayList<>();
    private List<Task> tasksLevel2 = new ArrayList<>();
    private List<Task> tasksLevel3 = new ArrayList<>();

    public Topic(int id, String name, Subject subject, int ratio, int grade, int number) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.ratio = ratio;
        this.grade = grade;
        this.number = number;
    }

    private static Topic fromSQLFields(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String name = fields[1];
        Subject subject = Subject.get(Integer.parseInt(fields[2]));
        int ratio = Integer.parseInt(fields[3]);
        int grade = Integer.parseInt(fields[4]);
        int number = Integer.parseInt(fields[5]);
        return new Topic(id, name, subject, ratio, grade, number);
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

    public int getNumber() {
        return number;
    }

    public List<Task> getTasks() {
        if (tasks.isEmpty()) {
            loadTasks();
        }
        return tasks;
    }

    public List<Integer> getTaskIds() {
        List<Integer> taskIds = new ArrayList<>();
        for (Task task : getTasks()) {
            taskIds.add(task.getId());
        }
        return taskIds;
    }

    public List<Task> getTasksLevel1() {
        if (tasks.isEmpty()) {
            loadTasks();
        }
        return tasksLevel1;
    }
    public List<Task> getTasksLevel2() {
        if (tasks.isEmpty()) {
            loadTasks();
        }
        return tasksLevel2;
    }
    public List<Task> getTasksLevel3() {
        if (tasks.isEmpty()) {
            loadTasks();
        }
        return tasksLevel3;
    }
    public List<Task> getTasksByLevel(Level level) {
        if (tasks.isEmpty()) {
            loadTasks();
        }
        switch (level) {
            case LEVEL1:
                return getTasksLevel1();
            case LEVEL2:
                return getTasksLevel2();
            case LEVEL3:
                return getTasksLevel3();
            default:
                throw new IllegalArgumentException("Invalid level: " + level);
        }
    }
    private List<Task> getTasksByLevel(List<Task> tasks, Level level) {
        return tasks.stream()
            .filter(task -> task.getNiveau() == level)
            .toList();
    }
    private void loadTasks() {
        tasks.clear();
        try {
            Server.getInstance().processRequest(
                fields -> {
                    Task task = Task.get(Integer.parseInt(fields[0]));
                    if (task != null) tasks.add(task);
                },
                "get_tasks_by_topic", new String[] {"id"}, String.valueOf(id)
            );
            tasksLevel1 = getTasksByLevel(tasks, Level.LEVEL1);
            tasksLevel2 = getTasksByLevel(tasks, Level.LEVEL2);
            tasksLevel3 = getTasksByLevel(tasks, Level.LEVEL3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "{\"id\":" + id + ", \"name\": \"" + name + "\", \"subject\": " + subject + ", \"ratio\": " + ratio + ", \"grade\": " + grade
                + ", \"tasks\": " + getTaskIds() + ", \"number\": " + number + "}";
    }
    
    public static void addTopic(String name, Subject subject, int ratio, int grade, int number) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("topic", name, subject == null ? "-1" : String.valueOf(subject.getId()), String.valueOf(ratio), String.valueOf(grade), String.valueOf(number)));
    }
}
