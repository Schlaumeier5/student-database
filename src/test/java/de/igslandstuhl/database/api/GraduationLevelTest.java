package de.igslandstuhl.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.igslandstuhl.database.server.resources.CoreResourceProvider;
import de.igslandstuhl.database.server.resources.FileResourceProvider;
import de.igslandstuhl.database.server.resources.ResourceLocation;
import de.igslandstuhl.database.server.resources.ResourceManager;

class GraduationLevelTest {

    @Test
    void defaultLevelsMatchPreviousBehavior() {
        assertEquals("Neustarter", GraduationLevel.of(0).getGermanTranslation());
        assertEquals("Starter", GraduationLevel.of(1).getGermanTranslation());
        assertEquals("Durchstarter", GraduationLevel.of(2).getGermanTranslation());
        assertEquals("Lernprofi", GraduationLevel.of(3).getGermanTranslation());
    }

    @Test
    void initialValueIsLevelOne() {
        assertEquals(1, GraduationLevel.initialValue().getLevel());
    }

    @Test
    void valuesAreOrderedByNumericLevel() {
        GraduationLevel[] values = GraduationLevel.of(0).values();

        assertEquals(4, values.length);
        assertEquals(0, values[0].getLevel());
        assertEquals(1, values[1].getLevel());
        assertEquals(2, values[2].getLevel());
        assertEquals(3, values[3].getLevel());
    }


    @Test
    void localConfigurationOverridesCoreConfiguration(@TempDir Path tempDir)
            throws Exception {
        Path config = tempDir.resolve("meta/api/graduation_levels.json");
        Files.createDirectories(config.getParent());
        Files.writeString(config, """
            {
                "1": "Lokaler Starter"
            }
            """);

        ResourceManager manager = new ResourceManager(
            new FileResourceProvider(tempDir),
            new CoreResourceProvider()
        );

        Map<String, ?> levels = manager.readJsonResourceAsMap(
            new ResourceLocation("meta", "api", "graduation_levels.json")
        );

        assertEquals("Lokaler Starter", levels.get("1"));
        assertEquals(1, levels.size());
    }

    @Test
    void unknownLevelThrowsException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> GraduationLevel.of(999)
        );
    }
}
