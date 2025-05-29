package de.igslandstuhl.database.api;

import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import de.igslandstuhl.database.server.Server;

public class ClassTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        server.getConnection().createTables();
    }
    @Test
    public void addClass() throws SQLException {
        SchoolClass.addClass("5a", 5);
    }
    @Test
    public void accessClasses() {
        assertNotNull(SchoolClass.get(1));
    }
}
