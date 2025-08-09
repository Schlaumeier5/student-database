package de.igslandstuhl.database.api;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SubjectTest {
    @BeforeAll
    public static void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        PreConditions.addSampleClass(); // Ensure a class exists for testing
    }
    @Test
    public void addSubject() throws SQLException {
        Subject added = Subject.addSubject("Mathematik");
        Subject subject = Subject.get(1);
        assertNotNull(subject);
        assertEquals(added, subject);
    }
    @Test
    public void addSubjectToGrade() throws SQLException {
        PreConditions.addSampleSubject();
        Subject.get(1).addToGrade(5);
        SchoolClass schoolClass = SchoolClass.get(1);
        assertTrue(
            schoolClass.getSubjects().stream().anyMatch(subject -> subject.getId() == 1),
            "Subject not found in SchoolClass"
        );
    }
}
