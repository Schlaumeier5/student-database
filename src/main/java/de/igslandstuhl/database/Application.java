package de.igslandstuhl.database;

public final class Application {
    private static final Application instance = new Application();
    public static Application getInstance() {
        return instance;
    }

    private final boolean onServer = true;
    public boolean isOnServer() {
        return onServer;
    }
}
