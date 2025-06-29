package de.igslandstuhl.database;

/**
 * Represents the main application class that serves as a singleton instance.
 * This class provides methods to check if the application is running on a server.
 * It will later replace Server as main class.
 */
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
