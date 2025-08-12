package de.igslandstuhl.database.server.webserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class GetRequestTest {
    private static final String LOCALHOST = "127.0.0.1";
    GetRequest validRequest;
    @BeforeEach
    void initRequest() {
        validRequest = new GetRequest("GET /login HTTP/1.1", LOCALHOST, true);
    }
    @Test
    void testConstructorThrows() {
        String[] invalidRequests = {
            "GET /login HTP/1",
            "GET",
            "POST",
            "GET /login"
        };
        for (String rqString : invalidRequests) {
            assertThrows(IllegalArgumentException.class, () -> new GetRequest(rqString, LOCALHOST, true));
        }
    }
    @Test
    void testIsValid() {
        assertTrue(validRequest::isValid);
        assertFalse(new GetRequest("GET /test.sql HTTP/1.1", LOCALHOST, true)::isValid);
    }

    @Test
    void testToResourceLocation() {
        assertEquals(ResourceLocation.get("html", "site:login.html"), validRequest.toResourceLocation(null));
    }
}
