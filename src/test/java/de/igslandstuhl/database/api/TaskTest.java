package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
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
        Subject.addSubject("Mathematik");
        Topic.addTopic("Bruchrechnung", Subject.get(1), 100, 5);
    }
    @Test
    public void addTask() throws SQLException {
        Task.addTask(Topic.get(1), "Addition", Level.LEVEL1);
        Task task = Task.get(1);
        assertNotNull(task);
        assertEquals("Addition", task.getName());
        assertEquals(Level.LEVEL1, task.getNiveau());
        assertEquals(1, task.getTopic().getId());
    }
}
