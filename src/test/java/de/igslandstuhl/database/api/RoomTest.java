package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import de.igslandstuhl.database.server.Server;

public class RoomTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        server = Server.getInstance();
    }
    @Test
    public void addRoom() throws SQLException {
        Room room = Room.addRoom("Gelingensnachweis", 0);
        assertNotNull(room);
        assertEquals("Gelingensnachweis", room.getLabel());
        assertEquals(0, room.getMinimumLevel());
    }
    @Test
    public void getRoom() throws SQLException {
        Room.addRoom("Gelingensnachweis", 0);
        Room room = Room.getRoom("Gelingensnachweis");
        assertNotNull(room);
        assertEquals("Gelingensnachweis", room.getLabel());
        assertEquals(0, room.getMinimumLevel());
    }
    @Test
    public void addAllRooms() throws SQLException {
        List<Room> rooms = Room.addAllRooms(
            List.of("5 Einzelarbeitsraum", "5 Teamarbeitsraum", "5 Inputraum groß", "5 Inputraum klein",
                    "5 Gruppenarbeitsraum 1", "5 Gruppenarbeitsraum 2", "5 Gruppenarbeitsraum 3"),
            List.of(0, 0, 0, 0, 0, 0, 0)
        );
        assertEquals(7, rooms.size());
    }
}
