package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import de.igslandstuhl.database.server.Server;

public class SchoolClassTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        server = Server.getInstance();
    }
    @Test
    public void addClass() throws SQLException {
        SchoolClass added = SchoolClass.addClass("5a", 5);
        SchoolClass schoolClass = SchoolClass.get(1);
        assertNotNull(schoolClass);
        assertEquals(added, schoolClass);
    }
}