package de.igslandstuhl.database.api;

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
    }
}
