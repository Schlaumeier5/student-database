package de.igslandstuhl.database.api;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TeacherTest {

    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        PreConditions.addSampleClass(); // Ensure a class exists for testing
    }

    @Test
    public void registerAndLoadTeacher() throws SQLException {
        Teacher teacher = Teacher.registerTeacher("Erika", "Mustermann", "erika@schule.de", "pw123");
        assertNotNull(teacher);
        assertEquals("Erika", teacher.getFirstName());
        assertEquals("Mustermann", teacher.getLastName());
        assertEquals("erika@schule.de", teacher.getEmail());

        Teacher loaded = Teacher.fromEmail("erika@schule.de");
        assertNotNull(loaded);
        assertEquals(teacher.getId(), loaded.getId());
    }

    @Test
    public void assignClassToTeacher() throws SQLException {
        Teacher teacher = Teacher.registerTeacher("Max", "Lehrer", "max@schule.de", "pw456");
        SchoolClass schoolClass = SchoolClass.get(1);
        teacher.addClass(schoolClass.getId());

        // Reload teacher to ensure class assignment is persisted
        Teacher loaded = Teacher.fromEmail("max@schule.de");
        assertTrue(loaded.getClassIds().contains(schoolClass.getId()));

        // Check if getMyStudents works (should be empty unless students are added)
        List<Student> students = loaded.getMyStudents();
        assertNotNull(students);
    }
}