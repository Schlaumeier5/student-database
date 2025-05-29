package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import de.igslandstuhl.database.server.Server;

public class StudentTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        server.getConnection().createTables();
        SchoolClass.addClass("5a", 5);
    }
    @Test
    public void testPuttingStudent() throws SQLException {
        Student.registerStudentWithPassword(0, "Max", "Mustermann", "max@muster.mann", "12345", SchoolClass.get(1), 1);
        Student student = Student.get(0);
        assertNotNull(student);
        assertEquals("Max", student.getFirstName());
        assertEquals("Mustermann", student.getLastName());
        assertEquals("max@muster.mann", student.getEmail());
        assertEquals(1, student.getGraduationLevel());
        assertEquals(1, student.getSchoolClass().getId());
    }
}
