package de.igslandstuhl.database.utils;

@FunctionalInterface
public interface ThrowingFunction<T, V> {
    public V apply(T input) throws Exception;
}
