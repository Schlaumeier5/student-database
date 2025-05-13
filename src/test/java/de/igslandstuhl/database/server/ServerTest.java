package de.igslandstuhl.database.server;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import de.igslandstuhl.database.api.SchoolClass;
import de.igslandstuhl.database.api.Student;

public class ServerTest {
    Server server;
    SchoolClass testClass;
    @Before
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        server.getConnection().createTables();
    }
    @Test
    public void testPuttingStudent() throws SQLException {
        Student.registerStudentWithPassword(0, "Max", "Mustermann", "max@muster.mann", "12345", testClass, 1);
    }
}
