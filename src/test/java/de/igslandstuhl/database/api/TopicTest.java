package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;

public class TopicTest {
    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        PreConditions.addSampleSubject();
    }
    @Test
    public void addTopic() throws SQLException {
        Topic added = Topic.addTopic("Bruchrechnung", Subject.get(1), 100, 5, 1);
        Topic topic = Topic.get(1);
        assertNotNull(topic);
        assertEquals(added, topic);
    }
}
