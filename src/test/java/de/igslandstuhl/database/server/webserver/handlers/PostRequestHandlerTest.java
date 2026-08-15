package de.igslandstuhl.database.server.webserver.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PostRequestHandlerTest {

    @Test
    void resolvesValidHtmlTemplatePath() {
        Path path =
            PostRequestHandler.resolveTemplatePath("login.html");

        assertTrue(path.isAbsolute());
        assertTrue(
            path.endsWith(
                Path.of(
                    "resources",
                    "templates",
                    "html",
                    "login.html"
                )
            )
        );
    }

    @Test
    void rejectsPathTraversal() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PostRequestHandler.resolveTemplatePath(
                "../evil.html"
            )
        );
    }

    @Test
    void rejectsSubdirectories() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PostRequestHandler.resolveTemplatePath(
                "subdir/login.html"
            )
        );
    }

    @Test
    void rejectsNonHtmlFiles() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PostRequestHandler.resolveTemplatePath(
                "login.txt"
            )
        );

        assertEquals(
            "Only .html template files can be edited",
            exception.getMessage()
        );
    }
}
