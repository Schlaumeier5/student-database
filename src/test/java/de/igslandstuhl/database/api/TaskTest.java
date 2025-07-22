package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;

public class TaskTest {
    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        PreConditions.addSampleSubject();
        PreConditions.addSampleTopic();
    }
    @Test
    public void addTask() throws SQLException {
        Task added = Task.addTask(Topic.get(1), "Addition", Level.LEVEL1);
        Task task = Task.get(1);
        assertNotNull(task);
        assertEquals(added, task);
    }
}
