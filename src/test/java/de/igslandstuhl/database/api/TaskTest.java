package de.igslandstuhl.database.api;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import de.igslandstuhl.database.server.Server;

public class TaskTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        server.getConnection().createTables();
    }
    @Test
    public void addTask() throws SQLException {
        Task.addTask(new Topic(0, "Bruchrechnung", new Subject(0, "Mathematik"), 100, 5), "Addition", Level.LEVEL1);
    }
}
