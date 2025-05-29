package de.igslandstuhl.database.api;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import de.igslandstuhl.database.server.Server;

public class SubjectTest {
    Server server;
    @Before
    public void setupServer() throws SQLException {
        server = Server.getInstance();
        server.getConnection().createTables();
    }
    @Test
    public void addSubject() throws SQLException {
        Subject.addSubject("Mathematik");
    }
    @Test
    public void addSubjectToGrade() throws SQLException {
        Subject.get(1).addToGrade(5);
    }
}
