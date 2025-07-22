package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

/**
 * Represents a student in the system.
 * Inherits from {@link User} and provides student-specific data and logic.
 */
public class Student extends User {
    private static final String[] SQL_FIELDS = new String[] {"id", "first_name", "last_name", "email", "password", "class", "graduation_level"};
    private static final String[] INTERESTING_TASKSTAT_FIELDS = {"task"};
    private static final Map<Integer, Student> students = new HashMap<>();

    /**
     * The unique ID of the student.
     */
    private final int id;

    /**
     * The first name of the student.
     */
    private final String firstName;

    /**
     * The last name of the student.
     */
    private final String lastName;

    /**
     * The email address of the student.
     */
    private final String email;

    /**
     * The hashed password of the student.
     */
    private final String passwordHash;

    /**
     * The class the student belongs to.
     */
    private final SchoolClass schoolClass;

    /**
     * The graduation level of the student.
     */
    private final int graduationLevel;

    /**
     * The set of tasks currently selected by the student.
     */
    private final Set<Task> selectedTasks = new HashSet<>();

    /**
     * The set of tasks completed by the student.
     */
    private final Set<Task> completedTasks = new HashSet<>();

    /**
     * The current requests of the student, mapped by subject ID.
     */
    private final Map<Integer, String> currentRequests = new ConcurrentHashMap<>();

    /**
     * The current topics of the student, mapped by subject.
     */
    private final Map<Subject, Topic> currentTopics = new ConcurrentHashMap<>();

    /**
     * The current room of the student.
     */
    private Room currentRoom = null;

    /**
     * Constructs a new Student.
     *
     * @param id The student ID.
     * @param firstName The first name.
     * @param lastName The last name.
     * @param email The email address.
     * @param passwordHash The hashed password.
     * @param schoolClass The school class.
     * @param graduationLevel The graduation level.
     */
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

    /**
     * Creates a Student instance from SQL data.
     *
     * @param fields The fields retrieved from the database.
     * @return A new Student instance.
     */
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

    /**
     * Retrieves a Student by its unique identifier from the database.
     *
     * @param id the unique identifier of the student
     * @return a Student object if found, or null if not found
     */
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
    /**
     * Retrieves a Student by its email address from the database.
     *
     * @param email the email address of the student
     * @return a Student object if found, or null if not found
     */
    public static Student getByEmail(String email) {
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
    /**
     * Registers a new student with a password.
     * This method creates a new student in the database and returns the created Student object.
     * 
     * @param id the unique identifier for the student
     * @param firstName the first name of the student
     * @param lastName the last name of the student
     * @param email the email address of the student
     * @param password the password for the student
     * @param schoolClass the school class of the student
     * @param graduationLevel the graduation level of the student
     * @return the created Student object
     * @throws SQLException if there is an error creating the student
     */
    public static Student registerStudentWithPassword(int id, String firstName, String lastName, String email, String password, SchoolClass schoolClass, int graduationLevel) throws SQLException {
        Student student = new Student(id, firstName, lastName, email, User.passHash(password), schoolClass, graduationLevel);
        Server.getInstance().getConnection().executeVoidProcessSecure(SQLHelper.getAddObjectProcess("student", String.valueOf(id), firstName, lastName, email, User.passHash(password), schoolClass != null ? String.valueOf(schoolClass.getId()) : "-1", String.valueOf(graduationLevel)));
        students.put(id, student);
        return student;
    }

    /**
     * Returns the student's ID.
     * @return the ID
     */
    public int getId() { return id; }

    /**
     * Returns the student's first name.
     * @return the first name
     */
    public String getFirstName() { return firstName; }

    /**
     * Returns the student's last name.
     * @return the last name
     */
    public String getLastName() { return lastName; }

    /**
     * Returns the student's email address.
     * @return the email
     */
    public String getEmail() { return email; }

    /**
     * Returns the student's graduation level.
     * @return the graduation level
     */
    public int getGraduationLevel() { return graduationLevel; }

    /**
     * Returns the student's current room.
     * @return the current room
     */
    public Room getCurrentRoom() { return currentRoom; }

    /**
     * Sets the student's current room.
     * @param currentRoom the new room
     */
    public void setCurrentRoom(Room currentRoom) { this.currentRoom = currentRoom; }

    /**
     * Returns the set of selected tasks.
     * @return selected tasks
     */
    public Set<Task> getSelectedTasks() { return new HashSet<>(selectedTasks); }

    /**
     * Returns the set of completed tasks.
     * @return completed tasks
     */
    public Set<Task> getCompletedTasks() { return new HashSet<>(completedTasks); }

    /**
     * Returns the current requests.
     * @return current requests
     */
    public Map<Integer, String> getCurrentRequests() { return currentRequests; }

    /**
     * Returns the student's password hash.
     * @return password hash
     */
    public String getPasswordHash() { return passwordHash; }

    /**
     * Returns the student's school class.
     * @return school class
     */
    public SchoolClass getSchoolClass() { return schoolClass; }

    /**
     * Adds a subject request for this student.
     * @param subjectId the subject ID
     * @param type the request type
     */
    public void addSubjectRequest(int subjectId, String type) {
        currentRequests.put(subjectId, type);
    }

    /**
     * Removes a subject request for this student.
     * @param subjectId the subject ID
     */
    public void clearSubjectRequest(int subjectId) {
        currentRequests.remove(subjectId);
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
            .reduce((a, b) -> a + ", " + b).orElse("")).append("},\n");
        builder.append("\"currentProgress\": {");
        for (Map.Entry<Subject, Topic> entry : currentTopics.entrySet()) {
            builder.append("\"").append(entry.getKey().getName()).append("\": ");
            builder.append("{\"topic\": ").append(entry.getValue().getId()).append(", ");
            builder.append("\"progress\": ").append(getCurrentProgress(entry.getKey())).append("}, ");
        }
        if (!currentTopics.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove trailing comma and space
        }
        builder.append("},\n");
        builder.append("\"predictedProgress\": {");
        for (Map.Entry<Subject, Topic> entry : currentTopics.entrySet()) {
            builder.append("\"").append(entry.getKey().getName()).append("\": ");
            builder.append("{\"topic\": ").append(entry.getValue().getId()).append(", ");
            builder.append("\"predictedProgress\": ").append(getPredictedProgress(entry.getKey())).append("}, ");
        }
        if (!currentTopics.isEmpty()) {
            builder.setLength(builder.length() - 2); // Remove trailing comma and space
        }
        builder.append("}\n");
        builder.append("}");
        return builder.toString();
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

    /**
     * Sets the current topic for a given subject.
     * @param subject the subject
     * @param topic the topic to set
     * @throws SQLException if an error occurs while updating the database
     */
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

    /**
     * Assigns a topic to the student.
     * @param topic the topic to assign
     * @throws SQLException if an error occurs while updating the database
     */
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

    /**
     * Returns the current topics of the student, mapped by subject.
     * @return a map of subjects to their current topics
     */
    public Map<Subject, Topic> getCurrentTopics() {
        // Optionally reload from DB if needed
        return new HashMap<>(currentTopics);
    }

    /**
     * Returns the current topic for a given subject.
     * @param subject the subject to get the current topic for
     * @return the current topic, or null if not set
     */
    public Topic getCurrentTopic(Subject subject) {
        return currentTopics.get(subject);
    }

    /**
     * Returns the current progress of the student for a given subject.
     * @param subject the subject to get the current progress for
     * @return the current progress as a percentage (0-100)
     */
    public double getCurrentProgress(Subject subject) {
        return completedTasks.stream()
            .filter(task -> task.getSubject() != null && task.getSubject().equals(subject))
            .mapToDouble(Task::getRatio)
            .sum();
    }
    /**
     * Returns the currently achieved grade for a given subject based on the current progress.
     * @param subject the subject to evaluate
     * @return the grade as an integer (1-6)
     */
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
    /**
     * Predicts the student's progress for a given subject based on the current week of the school year.
     * @param subject the subject to predict progress for
     * @return the predicted progress as a percentage (0-100)
     */
    public double getPredictedProgress(Subject subject) {
        double progress = getCurrentProgress(subject);
        SchoolYear currentYear = SchoolYear.getCurrentYear();
        if (currentYear == null) {
            return 0; // No current year available
        }
        return progress * currentYear.getWeekCount() / currentYear.getCurrentWeek();
    }
    /**
     * Predicts the student's grade for a given subject based on the predicted progress.
     * @param subject the subject to predict the grade for
     * @return the predicted grade as an integer (1-6)
     */
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
