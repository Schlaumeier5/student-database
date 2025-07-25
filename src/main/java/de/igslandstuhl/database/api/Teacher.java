package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.*;
import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

/**
 * Represents a teacher in the student database.
 * Teachers can teach multiple classes and have a unique ID, first name, last name, email, and password hash.
 */
public class Teacher extends User {
    /**
     * SQL fields for the Teacher table.
     * Used for database queries to retrieve teacher information.
     */
    private static final String[] SQL_FIELDS = new String[] {"id", "first_name", "last_name", "email", "password"};
    /**
     * SQL fields for the teacher-to-class relationship.
     * Used to retrieve classes associated with a teacher.
     */
    private static final String[] CLASS_FIELDS = new String[] {"class_id"};
    /**
     * A map to cache teachers by their unique identifier.
     * This helps avoid repeated database queries for the same teacher.
     */
    private static final Map<Integer, Teacher> teachers = new HashMap<>();
    /**
     * A map to cache teachers by their email address.
     * This allows quick retrieval of a teacher by their email.
     */
    private static final Map<String, Teacher> teachersByEmail = new HashMap<>();

    /**
     * The unique identifier for the teacher.
     */
    private int id;
    /**
     * The first name of the teacher.
     */
    private String firstName;
    /**
     * The last name of the teacher.
     */
    private String lastName;
    /**
     * The email address of the teacher.
     */
    private String email;
    /**
     * The password hash for the teacher's account.
     */
    private String passwordHash;
    /**
     * The set of class IDs that this teacher is associated with.
     */
    private Set<Integer> classIds = new HashSet<>(); // IDs of classes this teacher teaches

    /**
     * Constructs a new Teacher.
     *
     * @param id          the unique identifier for the teacher
     * @param firstName   the first name of the teacher
     * @param lastName    the last name of the teacher
     * @param email       the email address of the teacher
     * @param passwordHash the hashed password for the teacher's account
     */
    public Teacher(int id, String firstName, String lastName, String email, String passwordHash) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the unique identifier of the teacher.
     * This is used to identify the teacher in various operations.
     *
     * @return the unique identifier of the teacher
     */
    public int getId() { return id; }
    /**
     * Returns the first name of the teacher.
     *
     * @return the first name of the teacher
     */
    public String getFirstName() { return firstName; }
    /**
     * Returns the last name of the teacher.
     *
     * @return the last name of the teacher
     */
    public String getLastName() { return lastName; }
    /**
     * Returns the email address of the teacher.
     *
     * @return the email address of the teacher
     */
    public String getEmail() { return email; }
    /**
     * Returns the password hash of the teacher.
     * This is used for authentication purposes.
     *
     * @return the password hash of the teacher
     */
    @Override
    public String getPasswordHash() { return passwordHash; }

    /**
     * Returns the set of class IDs that this teacher is associated with.
     * This allows retrieval of all classes taught by the teacher.
     *
     * @return a set of class IDs
     */
    public Set<Integer> getClassIds() { return new HashSet<>(classIds); }
    /**
     * Adds a class ID to the set of classes this teacher teaches.
     * This method is used to associate a new class with the teacher.
     *
     * @param classId the unique identifier of the class to add
     */
    public void addClassId(int classId) { classIds.add(classId); }

    @Override
    public boolean isTeacher() { return true; }
    @Override
    public boolean isStudent() { return false; }

    @Override
    public String toJSON() {
        return String.format(
            "{\"id\":%d,\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\",\"classIds\":%s}",
            id, firstName, lastName, email, classIds.toString()
        );
    }

    /**
     * Retrieves all students taught by this teacher.
     * This method iterates through all classes associated with the teacher
     *
     * @return a list of students taught by this teacher
     */
    public List<Student> getMyStudents() {
        List<Student> students = new ArrayList<>();
        for (int classId : classIds) {
            SchoolClass schoolClass = SchoolClass.get(classId);
            if (schoolClass != null) {
                students.addAll(schoolClass.getStudents());
            }
        }
        return students;
    }

    // --- Static DB logic ---

    /**
     * Creates a Teacher object from SQL query result fields.
     * This method is used to convert database query results into a Teacher object.
     *
     * @param fields the fields retrieved from the database
     * @return a Teacher object constructed from the provided fields
     */
    private static Teacher fromSQL(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String firstName = fields[1];
        String lastName = fields[2];
        String email = fields[3];
        String passwordHash = fields[4];
        return new Teacher(id, firstName, lastName, email, passwordHash);
    }
    /**
     * Retrieves a Teacher by their unique identifier.
     * If the teacher is cached, it returns the cached version.
     * Otherwise, it queries the database for the teacher.
     *
     * @param id the unique identifier of the teacher
     * @return the Teacher object if found, or null if not found
     */
    public static Teacher get(int id) {
        if (teachers.containsKey(id)) return teachers.get(id);
        try {
            Teacher teacher = Server.getInstance().processSingleRequest(Teacher::fromSQL, "get_teacher_by_id", SQL_FIELDS, String.valueOf(id));
            if (teacher == null) return null;
            teacher.loadClasses();
            teachers.put(id, teacher);
            teachersByEmail.put(teacher.getEmail(), teacher);
            return teacher;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Retrieves a Teacher by their email address.
     * If the teacher is cached, it returns the cached version.
     * Otherwise, it queries the database for the teacher.
     *
     * @param email the email address of the teacher
     * @return the Teacher object if found, or null if not found
     */
    public static Teacher fromEmail(String email) {
        if (email == null) return null;
        if (teachersByEmail.containsKey(email)) return teachersByEmail.get(email);
        try {
            Teacher teacher = Server.getInstance().processSingleRequest(Teacher::fromSQL, "get_teacher_by_email", SQL_FIELDS, email);
            if (teacher == null) return null;
            teacher.loadClasses();
            teachers.put(teacher.getId(), teacher);
            teachersByEmail.put(email, teacher);
            return teacher;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Retrieves all teachers from the database.
     * This method queries the database for all teachers and returns a list of Teacher objects.
     *
     * @return a list of all teachers
     */
    public static List<Teacher> getAll() {
        List<Teacher> all = new ArrayList<>();
        try {
            Server.getInstance().processRequest(
                fields -> {
                    Teacher teacher = Teacher.get(Integer.parseInt(fields[0]));
                    if (teacher != null) all.add(teacher);
                },
                "get_all_teachers", SQL_FIELDS
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return all;
    }

    /**
     * Registers a new teacher in the database.
     * This method adds a new teacher with the provided details and returns the created Teacher object.
     *
     * @param firstName the first name of the teacher
     * @param lastName  the last name of the teacher
     * @param email     the email address of the teacher
     * @param password  the password for the teacher's account
     * @return the newly created Teacher object
     * @throws SQLException if there is an error during database operations
     */
    public static Teacher registerTeacher(String firstName, String lastName, String email, String password) throws SQLException {
        String passwordHash = User.passHash(password);
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("teacher", firstName, lastName, email, passwordHash)
        );
        // Now fetch the teacher from DB to get the ID and cache it
        return fromEmail(email);
    }

    /**
     * Loads the classes taught by this teacher.
     * This method retrieves all class IDs associated with the teacher from the database.
     * It populates the classIds set with these IDs for further operations.
     */
    private void loadClasses() {
        classIds.clear();
        try {
            Server.getInstance().processRequest(
                fields -> classIds.add(Integer.parseInt(fields[0])),
                "get_teacher_classes", CLASS_FIELDS, String.valueOf(id)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a class to this teacher.
     *
     * @param classId the ID of the class to add
     * @throws SQLException if there is an error during database operations
     */
    public void addClass(int classId) throws SQLException {
        if (classIds.contains(classId)) return;
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("teacher_to_class", String.valueOf(id), String.valueOf(classId))
        );
        classIds.add(classId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
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
        Teacher other = (Teacher) obj;
        if (id != other.id)
            return false;
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        return true;
    }
}