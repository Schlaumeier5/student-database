package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import de.igslandstuhl.database.server.Server;

public class SubjectTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        server = Server.getInstance();
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
            "Subject not found in SchoolClass",
            schoolClass.getSubjects().stream().anyMatch(subject -> subject.getId() == 1)
        );
    }
}
