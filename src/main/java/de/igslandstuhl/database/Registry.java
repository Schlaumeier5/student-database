package de.igslandstuhl.database;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import de.igslandstuhl.database.server.commands.Command;

public class Registry<K, V> implements Closeable {
    private static final Registry<String,Command> COMMAND_REGISTRY = new Registry<>();
    public static Registry<String,Command> commandRegistry() {
        return COMMAND_REGISTRY;
    }

    private final Map<K,V> objects = new HashMap<>();

    public synchronized void register(K key, V value) {
        objects.put(key, value);
    }
    public synchronized Stream<V> stream() {
        return objects.values().stream();
    }
    public synchronized Stream<K> keyStream() {
        return objects.keySet().stream();
    }
    public synchronized V get(K key) {
        return objects.get(key);
    }

    @Override
    public void close() {
        objects.clear();
    }
}
