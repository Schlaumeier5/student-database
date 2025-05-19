package de.igslandstuhl.database.api;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import de.igslandstuhl.database.server.Server;

public class StudentTest {
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
