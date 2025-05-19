package de.igslandstuhl.database.api;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import de.igslandstuhl.database.server.Server;

public class TopicTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        server.getConnection().createTables();
    }
    @Test
    public void addTopic() throws SQLException {
        Topic.addTopic("Bruchrechnung", new Subject(0, "Mathematik"), 100, 5);
    }
}
