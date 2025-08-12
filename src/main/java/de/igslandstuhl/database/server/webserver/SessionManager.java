package de.igslandstuhl.database.server.webserver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.igslandstuhl.database.api.User;

public class SessionManager {
    private static Map<UUID,Session> sessionStore = new HashMap<>();
    /**
     * A map to store session IDs and their associated usernames.
     * This is a simple in-memory session store.
     */
    private static Map<Session, String> sessionUsers = new HashMap<>();

    public Session getSession(UUID sessionUUID) {
        return sessionStore.get(sessionUUID);
    }
    public Session getSession(HttpRequest request) {
        // Search for cookie header
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("session")) {
                Session session = sessionStore.get(UUID.fromString(cookie.getValue()));
                // Check if the session exists
                if (session != null) {
                    return session;
                }
            }
        }
        // Session does not exist yet
        Session session = new Session(request);
        sessionStore.put(session.getUUID(), session);
        return session;
    }
    public User getSessionUser(Session session) {
        return User.getUser(sessionUsers.get(session));
    }

    public User getSessionUser(HttpRequest request) {
        return getSessionUser(getSession(request));
    }
    /**
     * Adds a session to the session store.
     * This method is used to create a new session for a user.
     *
     * @param sessionId The session ID to be added.
     * @param username  The username associated with the session.
     */
    public void addSessionUser(Session session, String username) {
        // Remove all previous sessions for this user (see #51)
        if (sessionUsers.values().contains(username)) {
            sessionUsers.keySet().stream()
            .filter((k) -> sessionUsers.get(k).equals(username))
            .toList() // Convert to list to prevent ConcurrentModificationException
            .forEach((k) -> sessionUsers.remove(k));
        }
        sessionUsers.put(session, username);
    }
}
