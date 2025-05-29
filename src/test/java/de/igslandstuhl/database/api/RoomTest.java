package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import de.igslandstuhl.database.server.Server;

public class RoomTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        server.getConnection().createTables();
    }
    @Test
    public void addRoom() throws SQLException {
        Room.addRoom("Test-Raum", 0);
        Room room = Room.getRoom("Test-Raum");
        assertNotNull(room);
        assertEquals("Test-Raum", room.getLabel());
        assertEquals(0, room.getMinimumLevel());
    }
}
