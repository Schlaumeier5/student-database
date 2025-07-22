package de.igslandstuhl.database.api;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

public class SpecialTaskTest {
    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        PreConditions.addSampleSubject();
    }
    @Test
    public void addSpecialTask() throws SQLException {
        Subject subject = Subject.get(1);
        SpecialTask.addSpecialTask("Nansteinaufgabe", 0.05, subject);
        SpecialTask task = SpecialTask.get(1);
        assertNotNull(task);
        assertEquals("Nansteinaufgabe", task.getName());
        assertEquals(0.05, task.getRatio(), 0.0001);
        assertEquals(subject, task.getSubject());
    }
    @Test
    public void addSpecialTaskToStudent() throws SQLException {
        PreConditions.addSampleSpecialTask();
        PreConditions.addSampleStudent();
        SpecialTask task = SpecialTask.get(1);
        Student student = Student.get(1);
        student.assignCompletedSpecialTask(task);
        assertTrue(student.getCompletedTasks().contains(task));
    }
}
