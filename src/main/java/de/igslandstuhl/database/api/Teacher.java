package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.*;
import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class Teacher extends User {
    private static final String[] SQL_FIELDS = new String[] {"id", "first_name", "last_name", "email", "password"};
    private static final String[] CLASS_FIELDS = new String[] {"class_id"};
    private static final Map<Integer, Teacher> teachers = new HashMap<>();
    private static final Map<String, Teacher> teachersByEmail = new HashMap<>();

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Set<Integer> classIds = new HashSet<>(); // IDs of classes this teacher teaches

    public Teacher(int id, String firstName, String lastName, String email, String passwordHash) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    @Override
    public String getPasswordHash() { return passwordHash; }

    public Set<Integer> getClassIds() { return new HashSet<>(classIds); }
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

    // Get all students for this teacher (by class)
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

    private static Teacher fromSQL(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String firstName = fields[1];
        String lastName = fields[2];
        String email = fields[3];
        String passwordHash = fields[4];
        return new Teacher(id, firstName, lastName, email, passwordHash);
    }

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

    // Register a new teacher
    public static Teacher registerTeacher(String firstName, String lastName, String email, String password) throws SQLException {
        String passwordHash = User.passHash(password);
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("teacher", firstName, lastName, email, passwordHash)
        );
        // Now fetch the teacher from DB to get the ID and cache it
        return fromEmail(email);
    }

    // Load classes this teacher teaches
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

    // Add a class to this teacher
    public void addClass(int classId) throws SQLException {
        if (classIds.contains(classId)) return;
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess("teacher_to_class", String.valueOf(id), String.valueOf(classId))
        );
        classIds.add(classId);
    }
}