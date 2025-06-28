package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class Student extends User {
    private static final String[] SQL_FIELDS = new String[] {"id", "first_name", "last_name", "email", "password", "class", "graduation_level"};
    private static final String[] INTERESTING_TASKSTAT_FIELDS = {"task"};
    private static final Map<Integer, Student> students = new HashMap<>();

    private final int id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String passwordHash;
    private final SchoolClass schoolClass;
    private final int graduationLevel;

    private final Set<Task> selectedTasks = new HashSet<>();
    private final Set<Task> completedTasks = new HashSet<>();
    private final Map<Integer, String> currentRequests = new ConcurrentHashMap<>();
    private final Map<Subject, Topic> currentTopics = new ConcurrentHashMap<>();

    private Room currentRoom = null;

    private Student(int id, String firstName, String lastName, String email, String passwordHash, SchoolClass schoolClass,
            int graduationLevel) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.schoolClass = schoolClass;
        this.graduationLevel = graduationLevel;
    }

    private static Student fromSQL(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String firstName = fields[1];
        String lastName = fields[2];
        String email = fields[3];
        String password = fields[4];
        SchoolClass schoolClass = SchoolClass.get(Integer.parseInt(fields[5]));
        int graduationLevel = Integer.parseInt(fields[6]);
        Student student = new Student(id, firstName, lastName, email, password, schoolClass, graduationLevel);
        student.loadCurrentTopics();
        return student;
    }

    public static Student get(int id) {
        if (students.keySet().contains(id)) return students.get(id);
        try {
            Student student = Server.getInstance().processSingleRequest(Student::fromSQL, "get_student_by_id", SQL_FIELDS, String.valueOf(id));
            students.put(id, student);
            Server.getInstance().processRequest((t) -> student.selectedTasks.add(Task.get(Integer.parseInt(t[0]))), "get_selected_tasks_by_student", INTERESTING_TASKSTAT_FIELDS, String.valueOf(id));
            Server.getInstance().processRequest((t) -> student.completedTasks.add(Task.get(Integer.parseInt(t[0]))), "get_completed_tasks_by_student", INTERESTING_TASKSTAT_FIELDS, String.valueOf(id));
            return student;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Student fromEmail(String email) {
        try {
            Student student = Server.getInstance().processSingleRequest(Student::fromSQL, "get_student_by_email", SQL_FIELDS, email);
            if (student == null) return null;
            if (students.containsKey(student.getId())) return students.get(student.getId());
            students.put(student.getId(), student);
            Server.getInstance().processRequest((t) -> student.selectedTasks.add(Task.get(Integer.parseInt(t[0]))), "get_selected_tasks_by_student", INTERESTING_TASKSTAT_FIELDS, String.valueOf(student.getId()));
            Server.getInstance().processRequest((t) -> student.completedTasks.add(Task.get(Integer.parseInt(t[0]))), "get_completed_tasks_by_student", INTERESTING_TASKSTAT_FIELDS, String.valueOf(student.getId()));
            return student;
        } catch (NullPointerException e) {
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void registerStudentWithPassword(int id, String firstName, String lastName, String email, String password, SchoolClass schoolClass, int graduationLevel) throws SQLException {
        Student student = new Student(id, firstName, lastName, email, User.passHash(password), schoolClass, graduationLevel);
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("student", String.valueOf(id), firstName, lastName, email, User.passHash(password), schoolClass != null ? String.valueOf(schoolClass.getId()) : "-1", String.valueOf(graduationLevel)));
        students.put(id, student);
    }

    public int getId() {
        return id;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    @Override
    public String getPasswordHash() {
        return passwordHash;
    }
    public SchoolClass getSchoolClass() {
        return schoolClass;
    }
    public int getGraduationLevel() {
        return graduationLevel;
    }
    public Set<Task> getSelectedTasks() {
        return new HashSet<>(selectedTasks);
    }
    public Set<Task> getCompletedTasks() {
        return new HashSet<>(completedTasks);
    }
    @Override
    public String toString() {
        return toJSON();
    }
    @Override
    public boolean isTeacher() {
        return false;
    }
    @Override
    public boolean isStudent() {
        return true;
    }
    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    @Override
    public String toJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("\"id\": ");builder.append(id);builder.append(",\n");
        builder.append("\"firstName\": \"");builder.append(firstName);builder.append("\",\n");
        builder.append("\"lastName\": \"");builder.append(lastName);builder.append("\",\n");
        builder.append("\"email\": \"");builder.append(email);builder.append("\",\n");
        builder.append("\"schoolClass\": ");builder.append(String.valueOf(schoolClass));builder.append(",\n");
        builder.append("\"graduationLevel\": ");builder.append(graduationLevel);builder.append(",\n");
        builder.append("\"selectedTasks\": ");builder.append(selectedTasks);builder.append(",\n");
        builder.append("\"completedTasks\": ");builder.append(completedTasks);builder.append(",\n");
        builder.append("\"currentRoom\": ");builder.append(String.valueOf(currentRoom));builder.append(",\n");
        builder.append("\"currentRequests\": {");builder.append(currentRequests.entrySet().stream()
            .map(entry -> "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"")
            .reduce((a, b) -> a + ", " + b).orElse("")).append("}\n");
        builder.append("}");
        return builder.toString();
    }

    public void addSubjectRequest(int subjectId, String type) {
        currentRequests.put(subjectId, type);
    }

    public Map<Integer, String> getCurrentRequests() {
        return currentRequests;
    }

    // Optionally, a method to clear requests
    public void clearSubjectRequest(int subjectId) {
        currentRequests.remove(subjectId);
    }

    private void loadCurrentTopics() {
        currentTopics.clear();
        try {
            Server.getInstance().processRequest(
                fields -> {
                    int subjectId = Integer.parseInt(fields[1]);
                    int topicId = Integer.parseInt(fields[2]);
                    Subject subject = Subject.get(subjectId);
                    Topic topic = Topic.get(topicId);
                    if (subject != null && topic != null) {
                        currentTopics.put(subject, topic);
                    }
                },
                "get_current_topics_by_student",
                new String[] {"student_id", "subject_id", "topic_id"},
                String.valueOf(id)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentTopic(Subject subject, Topic topic) throws SQLException {
        // Update in DB
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("topic_to_student",
                String.valueOf(id),
                String.valueOf(topic.getId())
            )
        );
        // Update in memory
        currentTopics.put(subject, topic);
    }
    public void assignTopic(Topic topic) throws SQLException {
        if (topic == null) {
            throw new IllegalArgumentException("Topic cannot be null");
        }
        Subject subject = topic.getSubject();
        if (subject == null) {
            throw new IllegalArgumentException("Topic must have a subject");
        }
        setCurrentTopic(subject, topic);
    }

    public Map<Subject, Topic> getCurrentTopics() {
        // Optionally reload from DB if needed
        return new HashMap<>(currentTopics);
    }

    public Topic getCurrentTopic(Subject subject) {
        return currentTopics.get(subject);
    }

    public double getCurrentProgress(Subject subject) {
        List<Topic> topics = subject.getTopics(schoolClass.getGrade());
        return completedTasks.stream()
            .filter(task -> topics.stream()
                .anyMatch(topic -> task.getTopic() != null && task.getTopic().getId() == topic.getId()))
            .mapToDouble(Task::getRatio)
            .sum();
    }
    public int getCurrentlyAchievedGrade(Subject subject) {
        double progress = getCurrentProgress(subject);
        if (progress >= 0.85) {
            return 1; // Excellent
        } else if (progress >= 0.70) {
            return 2; // Good
        } else if (progress >= 0.55) {
            return 3; // Satisfactory
        } else if (progress >= 0.40) {
            return 4; // Sufficient
        } else if (progress >= 0.20) {
            return 5; // Deficient
        } else {
            return 6; // Insufficient
        }
    }
    public double getPredictedProgress(Subject subject) {
        double progress = getCurrentProgress(subject);
        SchoolYear currentYear = SchoolYear.getCurrentYear();
        if (currentYear == null) {
            return 0; // No current year available
        }
        return progress * currentYear.getWeekCount() / currentYear.getCurrentWeek();
    }
    public int getPredictedGrade(Subject subject) {
        double predictedProgress = getPredictedProgress(subject);
        if (predictedProgress >= 0.85) {
            return 1; // Excellent
        } else if (predictedProgress >= 0.70) {
            return 2; // Good
        } else if (predictedProgress >= 0.55) {
            return 3; // Satisfactory
        } else if (predictedProgress >= 0.40) {
            return 4; // Sufficient
        } else if (predictedProgress >= 0.20) {
            return 5; // Deficient
        } else {
            return 6; // Insufficient
        }
    }
}
