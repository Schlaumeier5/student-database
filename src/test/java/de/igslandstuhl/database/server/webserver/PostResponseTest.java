package de.igslandstuhl.database.server.webserver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.igslandstuhl.database.server.webserver.requests.PostRequest;
import de.igslandstuhl.database.server.webserver.responses.PostResponse;

public class PostResponseTest {
    PostRequest initialRequest;
    @BeforeEach
    void setup() {
        initialRequest = new PostRequest("POST /login HTTP/1.1", "username=adminUser;password=adminPass", "127.0.0.1", true);
    }
    private String read(PostResponse r) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        r.respond(new PrintStream(out));
        return out.toString();
    }
    @Test
    void testBadRequest() {
        assert read(PostResponse.badRequest("Test", initialRequest)).contains("400");
    }

    @Test
    void testForbidden() {
        assert read(PostResponse.forbidden("Test", initialRequest)).contains("403");
    }

    @Test
    void testInternalServerError() {
        assert read(PostResponse.internalServerError("Test", initialRequest)).contains("500");
    }

    @Test
    void testNotFound() {
        assert read(PostResponse.notFound("Test", initialRequest)).contains("404");
    }

    @Test
    void testOk() {
        assert read(PostResponse.ok("Test", ContentType.TEXT_PLAIN, initialRequest)).contains("200");
        assert read(PostResponse.ok("Test", ContentType.TEXT_PLAIN, initialRequest, new Cookie("test-key", "test-value"))).contains("Set-Cookie: test-key=test-value");
    }

    @Test
    void testRedirect() {
        assert read(PostResponse.redirect("Test", initialRequest)).contains("302");
    }

    @Test
    void testUnauthorized() {
        assert read(PostResponse.unauthorized("Test", initialRequest)).contains("401");
    }
}
