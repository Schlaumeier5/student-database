package de.igslandstuhl.database.server.webserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CookieTest {
    Cookie cookie;
    @BeforeEach
    public void initCookie() {
        cookie = new Cookie("test-key", "test-value");
    }
    @Test
    void testGetName() {
        assertEquals(cookie.getName(), "test-key");
    }

    @Test
    void testGetValue() {
        assertEquals(cookie.getValue(), "test-value");
    }

    @Test
    void testToString() {
        assertEquals(cookie.toString(), "test-key=test-value");
    }

    @Test
    void testEquals() {
        assertEquals(cookie, new Cookie("test-key", "test-value"));
    }
}
