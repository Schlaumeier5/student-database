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
        server = Server.getInstance();
        server.getConnection().createTables();
        SchoolClass.addClass("5a", 5);
    }
    @Test
    public void addSubject() throws SQLException {
        Subject.addSubject("Mathematik");
        Subject subject = Subject.get(1);
        assertNotNull(subject);
        assertEquals("Mathematik", subject.getName());
    }
    @Test
    public void addSubjectToGrade() throws SQLException {
        Subject.addSubject("Mathematik"); // Ensure subject exists and gets ID 1
        Subject.get(1).addToGrade(5);
        SchoolClass schoolClass = SchoolClass.get(1);
        assertTrue(
            "Subject not found in SchoolClass",
            schoolClass.getSubjects().stream().anyMatch(subject -> subject.getId() == 1)
        );
    }
}
