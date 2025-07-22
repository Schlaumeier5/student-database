package de.igslandstuhl.database.api;

import static org.junit.Assert.*;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import de.igslandstuhl.database.server.Server;

public class StudentTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        PreConditions.setupDatabase();
        server = Server.getInstance();
        PreConditions.addSampleClass();
    }
    @Test
    public void testPuttingStudent() throws SQLException {
        Student added = Student.registerStudentWithPassword(0, "Max", "Mustermann", "max@muster.mann", "12345", SchoolClass.get(1), 1);
        Student student = Student.get(0);
        assertEquals(added, student);
    }
    @Test
    public void testAssignTopicToStudent() throws SQLException {
        PreConditions.addSampleStudent();
        PreConditions.addSampleSubject();
        PreConditions.addSampleTopic();
        Student student = Student.get(0);
        Topic topic = Topic.get(1);
        assertNotNull(topic);
        student.assignTopic(topic);
        assertEquals(topic, student.getCurrentTopic(topic.getSubject()));
    }
}