package de.igslandstuhl.database.server.webserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.igslandstuhl.database.api.PreConditions;

public class SessionManagerTest {
    private static final String LOCALHOST = "127.0.0.1";
    SessionManager sessionManager;
    PostRequest sessionRequest;
    PostRequest requestWithoutSession;
    @BeforeEach
    void setup() {
        sessionManager = new SessionManager();
        sessionRequest = new PostRequest("POST /student-data HTTP/1.1\r\n" + //
                        "Cookie: test=test;session=" + UUID.randomUUID().toString() + ";other=value", null, LOCALHOST, true);
        requestWithoutSession = new PostRequest("POST /login HTTP/1.1", null, LOCALHOST, true);
    }
    @Test
    void testAddSessionUser() throws SQLException {
        Session session = sessionManager.getSession(sessionRequest);
        sessionManager.addSessionUser(session, "adminUser");
        PreConditions.addSampleAdmin();
        assertNotNull(sessionManager.getSessionUser(session));
        assertNotNull(sessionManager.getSessionUser(sessionRequest));
        assertEquals(sessionManager.getSessionUser(sessionRequest), sessionManager.getSessionUser(session));
        assertTrue(sessionManager.getSessionUser(session).isAdmin());
    }
    @Test
    void testGetSession() {
        Session session1 = sessionManager.getSession(sessionRequest);
        Session session2 = sessionManager.getSession(requestWithoutSession);
        assertNotNull(session1);assertNotNull(session2);
        assertNotEquals(session1, session2);

        PostRequest sessionRequest2 = new PostRequest("POST /student-data HTTP/1.1\r\n" + //
                        "Cookie: " + session1.createSessionCookie().toString(), null, LOCALHOST, true);
        Session session3 = sessionManager.getSession(sessionRequest2);
        assertEquals(session1, session3);
    }
}
