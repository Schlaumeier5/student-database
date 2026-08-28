package de.igslandstuhl.database.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.igslandstuhl.database.server.resources.ResourceLocation;

class HTMLFileTemplateTest {

    private final Path override =
        Path.of("resources", "templates", "html", "login.html");

    private boolean overrideExisted;
    private byte[] originalContent;

    @BeforeEach
    void backUpExistingOverride() throws Exception {
        overrideExisted = Files.exists(override);

        if (overrideExisted) {
            originalContent = Files.readAllBytes(override);
        }
    }

    @AfterEach
    void restoreExistingOverride() throws Exception {
        if (overrideExisted) {
            Files.createDirectories(override.getParent());
            Files.write(override, originalContent);
        } else {
            Files.deleteIfExists(override);
        }
    }

    @Test
    void reloadsLocalOverrideWithoutRestart() throws Exception {
        Files.createDirectories(override.getParent());

        Files.writeString(
            override,
            "First %{value}",
            StandardCharsets.UTF_8
        );

        HTMLFileTemplate template = new HTMLFileTemplate(
            new ResourceLocation("templates", "html", "login.html")
        );

        assertEquals(
            "First test\n",
            template.fill(Map.of("value", "test"))
        );

        Files.writeString(
            override,
            "Second %{value}",
            StandardCharsets.UTF_8
        );

        assertEquals(
            "Second test\n",
            template.fill(Map.of("value", "test"))
        );
    }
}
