package de.igslandstuhl.database.api;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StudentTest {
    @BeforeAll
    public static void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        PreConditions.addSampleClass();
    }
    @Test
    public void testPuttingStudent() throws SQLException {
        Student added = Student.registerStudentWithPassword(0, "Max", "Mustermann", "max@muster.mann", "12345", SchoolClass.get(1), GraduationLevel.LEVEL1);
        Student student = Student.get(0);
        assertEquals(added, student);
    }
    @Test
    public void testAssignTopicToStudent() throws SQLException {
        PreConditions.addSampleStudent();
        PreConditions.addSampleSchoolYear();
        PreConditions.addSampleSubject();
        PreConditions.addSampleTopic();
        Student student = Student.get(0);
        Topic topic = Topic.get(1);
        assertNotNull(topic);
        student.assignTopic(topic);
        assertEquals(topic, student.getCurrentTopic(topic.getSubject()));
    }

    @Test
    public void testTaskAttemptCounter() throws SQLException {
        PreConditions.addSampleStudent();
        PreConditions.addSampleSchoolYear();
        PreConditions.addSampleSubject();
        PreConditions.addSampleTopic();

        Student student = Student.get(0);
        assertNotNull(student);

        PreConditions.addSampleTask();

        Task task = Task.get(1);
        assertNotNull(task);

        // Never started.
        assertEquals(0, student.getTaskAttempts(task));

        // First real start -> attempt 1.
        student.changeTaskStatus(task, Task.STATUS_IN_PROGRESS);
        assertEquals(1, student.getTaskAttempts(task));

        // Duplicate start while already in progress must not count again.
        student.changeTaskStatus(task, Task.STATUS_IN_PROGRESS);
        assertEquals(1, student.getTaskAttempts(task));

        // Leaving the task does not increase the counter.
        student.changeTaskStatus(task, Task.STATUS_NOT_STARTED);
        assertEquals(1, student.getTaskAttempts(task));

        // Starting it again is a new attempt.
        student.changeTaskStatus(task, Task.STATUS_IN_PROGRESS);
        assertEquals(2, student.getTaskAttempts(task));

        // Counter is exposed through the student JSON API.
        assertTrue(
            student.toJSON().contains(
                "\"" + task.getId() + "\": 2"
            )
        );
    }

}
