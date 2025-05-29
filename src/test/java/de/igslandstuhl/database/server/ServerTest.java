package de.igslandstuhl.database.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

public class ServerTest {
    Server server;
    @Test
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        assertNotNull(server);
        assertNotNull(server.getConnection());
        server.getConnection().createTables();
        assertNotNull(server.getWebServer());
    }
    @Test
    public void testValidUser() {
        server = Server.getInstance();
        assertFalse(server.isValidUser("anamethatnostudentwillevergetbecauseitistoolongtoputintotheloginfield", "password"));
        assertFalse(server.isValidUser("max@muster.mann", "123456")); // Test student set up by StudentTest
        assertTrue(server.isValidUser("max@muster.mann", "12345"));
    }
}
