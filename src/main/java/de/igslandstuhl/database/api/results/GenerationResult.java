package de.igslandstuhl.database.api.results;

public class GenerationResult<T> {
    private final T entity;
    private final String password;

    public GenerationResult(T entity, String password) {
        this.entity = entity;
        this.password = password;
    }

    public T getEntity() {
        return entity;
    }

    public String getPassword() {
        return password;
    }
}
