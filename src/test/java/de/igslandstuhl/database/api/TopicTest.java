package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
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
        Subject.addSubject("Mathematik");
    }
    @Test
    public void addTopic() throws SQLException {
        Topic.addTopic("Bruchrechnung", Subject.get(1), 100, 5);
        Topic topic = Topic.get(1);
        assertNotNull(topic);
        assertEquals("Bruchrechnung", topic.getName());
        assertEquals(100, topic.getRatio());
        assertEquals(5, topic.getGrade());
        assertEquals(1, topic.getSubject().getId());
    }
}
