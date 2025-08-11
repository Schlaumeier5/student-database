package de.igslandstuhl.database.server.webserver;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

public class PostResponseTest {
    private String read(PostResponse r) {
        StringWriter w = new StringWriter();
        r.respond(new PrintWriter(w));
        return w.toString();
    }
    @Test
    void testBadRequest() {
        assert read(PostResponse.badRequest("Test")).contains("400");
    }

    @Test
    void testForbidden() {
        assert read(PostResponse.forbidden("Test")).contains("403");
    }

    @Test
    void testInternalServerError() {
        assert read(PostResponse.internalServerError("Test")).contains("500");
    }

    @Test
    void testNotFound() {
        assert read(PostResponse.notFound("Test")).contains("404");
    }

    @Test
    void testOk() {
        assert read(PostResponse.ok("Test", ContentType.TEXT_PLAIN)).contains("200");
        assert read(PostResponse.ok("Test", ContentType.TEXT_PLAIN, new Cookie("test-key", "test-value"))).contains("Set-Cookie: test-key=test-value");
    }

    @Test
    void testRedirect() {
        assert read(PostResponse.redirect("Test")).contains("302");
    }

    @Test
    void testUnauthorized() {
        assert read(PostResponse.unauthorized("Test")).contains("401");
    }
}
