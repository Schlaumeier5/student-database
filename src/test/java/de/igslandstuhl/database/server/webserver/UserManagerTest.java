package de.igslandstuhl.database.server.webserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserManagerTest {
    private static final String LOCALHOST = "127.0.0.1";
    UserManager userManager;
    PostRequest sessionRequest;
    PostRequest requestWithoutSession;
    @BeforeEach
    void setup() {
        userManager = new UserManager();
        sessionRequest = new PostRequest("POST /student-data HTTP/1.1\r\n" + //
                        "Cookie: test=test;session=sessionId;other=value", null, LOCALHOST, true);
        requestWithoutSession = new PostRequest("POST /login HTTP/1.1", null, LOCALHOST, true);
    }
    @Test
    void testNonExistingSessionUser() {
        assertNull(userManager.getSessionUser("testUser"));
    }
    @Test
    void testAddSessionUser() {
        userManager.addSession("sessionId", "testUser");
        assertEquals("testUser", userManager.getSessionUser(sessionRequest));
    }
    @Test
    void testSessionRequest() {
        userManager.addSession("sessionId", "testUser");
        assertEquals("testUser", userManager.getSessionUser(sessionRequest));
        assertNull(userManager.getSessionUser(requestWithoutSession));
    }
}
