package de.igslandstuhl.database.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.igslandstuhl.database.Registry;
import de.igslandstuhl.database.server.resources.CoreResourceProvider;
import de.igslandstuhl.database.server.resources.FileResourceProvider;
import de.igslandstuhl.database.server.resources.ResourceLocation;
import de.igslandstuhl.database.server.resources.ResourceManager;
import de.igslandstuhl.database.utils.RegistryEnum;

public class GraduationLevel extends RegistryEnum<GraduationLevel> implements APIObject {
    private static final ResourceLocation META =
        new ResourceLocation("meta", "api", "graduation_levels.json");

    /*
     * Local resources have priority. Plugin resources are deliberately excluded,
     * so graduation levels can only be overridden by the local file system.
     */
    private static final ResourceManager RESOURCE_MANAGER =
        new ResourceManager(
            new FileResourceProvider(Path.of("resources")),
            new CoreResourceProvider()
        );

    private static boolean initialized;

    private final int level;
    private final String germanTranslation;

    private GraduationLevel(
            Registry<String, GraduationLevel> registry,
            String key) {
        super(registry, key);
        this.level = -1;
        this.germanTranslation = key;
    }

    private GraduationLevel(
            Registry<String, GraduationLevel> registry,
            String key,
            int level,
            String germanTranslation) {
        super(registry, key);
        this.level = level;
        this.germanTranslation = germanTranslation;
    }

    public int getLevel() {
        return level;
    }

    public String getGermanTranslation() {
        return germanTranslation;
    }

    @Override
    public String toString() {
        return germanTranslation;
    }

    public static GraduationLevel of(int level) {
        ensureInitialized();
        GraduationLevel result =
            RegistryEnum.valueOf(String.valueOf(level), GraduationLevel.class);

        if (result == null) {
            throw new IllegalArgumentException(
                "No such graduation level: " + level
            );
        }
        return result;
    }

    public static GraduationLevel initialValue() {
        return of(1);
    }

    @Override
    public String toJSON() {
        return String.valueOf(level);
    }

    @Override
    protected GraduationLevel[] values(
            Registry<String, GraduationLevel> registry) {
        List<GraduationLevel> levels = registry.stream()
            .sorted(Comparator.comparingInt(GraduationLevel::getLevel))
            .toList();

        return levels.toArray(new GraduationLevel[0]);
    }

    @Override
    protected void initValues() {
        final Map<String, ?> config;

        try {
            config = RESOURCE_MANAGER.readJsonResourceAsMap(META);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to load graduation levels", e
            );
        }

        config.forEach((key, value) -> {
            int numericLevel = Integer.parseInt(key);
            String translation = String.valueOf(value);

            registry().register(
                key,
                new GraduationLevel(
                    registry(),
                    key,
                    numericLevel,
                    translation
                )
            );
        });
    }

    @Override
    protected GraduationLevel initValue(
            Registry<String, GraduationLevel> registry,
            String key) {
        return new GraduationLevel(registry, key);
    }

    private static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }

        try {
            RegistryEnum.init(GraduationLevel.class);
            initialized = true;
        } catch (InstantiationException |
                 IllegalAccessException |
                 IllegalArgumentException |
                 InvocationTargetException |
                 NoSuchMethodException |
                 SecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
