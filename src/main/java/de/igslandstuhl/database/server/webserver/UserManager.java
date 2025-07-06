package de.igslandstuhl.database.server.webserver;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    /**
     * A map to store session IDs and their associated usernames.
     * This is a simple in-memory session store.
     */
    private static Map<String, String> sessionStore = new HashMap<>();
    /**
     * Retrieves the username associated with the session from the request headers.
     * It searches for a cookie named "session" and checks if it exists in the session store.
     *
     * @param request The raw HTTP request string.
     * @return The username associated with the session, or null if no valid session is found.
     */
    public String getSessionUser(String request) {
        // Search for cookie header
        String[] lines = request.split("\n");
        for (String line : lines) {
            if (line.startsWith("Cookie:")) {
                String[] cookies = line.substring(7).split("; ");
                for (String cookie : cookies) {
                    String[] keyValue = cookie.split("=");
                    if (keyValue.length == 2 && keyValue[0].trim().equals("session")) {
                        return sessionStore.get(keyValue[1].trim()); // Überprüfe, ob die Session existiert
                    }
                }
            }
        }
        return null; // No valid session
    }
    public String getSessionUser(PostRequest request) {
        // Search for cookie header
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("session")) {
                return sessionStore.get(cookie.getValue()); // Check if the session exists
            }
        }
        return null; // No valid session
    }
    /**
     * Adds a session to the session store.
     * This method is used to create a new session for a user.
     *
     * @param sessionId The session ID to be added.
     * @param username  The username associated with the session.
     */
    public void addSession(String sessionId, String username) {
        sessionStore.put(sessionId, username);
    }
}
