package de.igslandstuhl.database.utils;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    public void accept(T t) throws Exception;
}
