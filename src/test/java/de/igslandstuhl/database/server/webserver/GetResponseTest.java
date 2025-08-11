package de.igslandstuhl.database.server.webserver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class GetResponseTest {
    @Test
    void testForbidden() throws FileNotFoundException {
        assertTrue(GetResponse.forbidden().getResponseBody().contains("403"));
    }

    @Test
    void testInternalServerError() throws FileNotFoundException {
        assertTrue(GetResponse.internalServerError().getResponseBody().contains("500"));
    }

    @Test
    void testNotFound() throws FileNotFoundException {
        assertTrue(GetResponse.notFound().getResponseBody().contains("404"));
    }

    @Test
    void testUnauthorized() throws FileNotFoundException {
        assertTrue(GetResponse.unauthorized().getResponseBody().contains("401"));
    }

    @Test
    void testGetResource() throws FileNotFoundException {
        assertTrue(GetResponse.getResource(ResourceLocation.get("html", "site:login.html"), null).getResponseBody().contains("login"));
    }

    @Test
    void testGetResponseBody() {
        assertNotNull(GetResponse.getResource(ResourceLocation.get("html", "site:login.html"), null));
    }

    @Test
    void testRespond() throws FileNotFoundException {
        ByteArrayOutputStream testStream = new ByteArrayOutputStream();
        PrintStream printWriter = new PrintStream(testStream);
        GetResponse response = GetResponse.getResource(ResourceLocation.get("html", "site:login.html"), null);
        response.respond(printWriter);
        String responseString = testStream.toString();
        String responseBody = response.getResponseBody();
        assertTrue(responseString.contains(responseBody));
        assertTrue(responseString.contains("HTTP/1.1 200 OK"));
    }
}
