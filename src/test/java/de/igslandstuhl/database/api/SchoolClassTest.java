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
        server = Server.getInstance();
        server.getConnection().createTables();
    }
    @Test
    public void addClass() throws SQLException {
        SchoolClass.addClass("5a", 5);
        SchoolClass schoolClass = SchoolClass.get(1);
        assertNotNull(schoolClass);
        assertEquals("5a", schoolClass.getLabel());
        assertEquals(5, schoolClass.getGrade());
    }
}