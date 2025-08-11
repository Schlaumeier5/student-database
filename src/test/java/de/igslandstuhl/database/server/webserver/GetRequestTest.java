package de.igslandstuhl.database.server.webserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class GetRequestTest {
    GetRequest validRequest;
    @BeforeEach
    void initRequest() {
        validRequest = new GetRequest("GET /login HTTP/1.1");
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
            assertThrows(IllegalArgumentException.class, () -> new GetRequest(rqString));
        }
    }
    @Test
    void testIsValid() {
        assertTrue(validRequest::isValid);
        assertFalse(new GetRequest("GET /test.sql HTTP/1.1")::isValid);
    }

    @Test
    void testToResourceLocation() {
        assertEquals(ResourceLocation.get("html", "site:login.html"), validRequest.toResourceLocation(null));
    }
}
