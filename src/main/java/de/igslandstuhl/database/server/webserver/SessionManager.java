package de.igslandstuhl.database.server.webserver;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.igslandstuhl.database.api.User;

public class SessionManager {
    private Map<UUID,Session> sessionStore = new HashMap<>();
    /**
     * A map to store session IDs and their associated usernames.
     * This is a simple in-memory session store.
     */
    private Map<Session, String> sessionUsers = new HashMap<>();
    private Map<Session, Instant> lastActivity = new HashMap<>();
    private Map<Session, Integer> requestCount = new HashMap<>();

    /**
     * After this duration, sessions expire (are removed from the session store). It is measured in seconds.
     */
    private final int sessionExpireDuration;
    private final int maximumInactivityDuration;
    private final int maxRequests;

    public SessionManager(int sessionExpireDuration, int maximumInactivityDuration, int maxRequests) {
        this.sessionExpireDuration = sessionExpireDuration;
        this.maximumInactivityDuration = maximumInactivityDuration;
        this.maxRequests = maxRequests;
        new Thread(this::cleanSecondsJob, "Session Expiring").start();
    }

    private void removeSession(Session session) {
        UUID uuid = session.getUUID();
        synchronized (sessionStore) {
            sessionStore.remove(uuid);
        }
        synchronized (sessionUsers) {
            sessionUsers.remove(session);
        }
        lastActivity.remove(session);
    }
    private Instant getOrCreateLastActivity(Session session) {
        Instant instant = lastActivity.get(session);
        return instant == null ? Instant.now() : instant;
    }
    private void cleanSecondsJob() {
        while (true) {
            sessionStore.values().stream()
            .filter((session) -> 
                session.getLoginTime().plusSeconds(sessionExpireDuration).isBefore(Instant.now())
                || getOrCreateLastActivity(session).plusSeconds(maximumInactivityDuration).isBefore(Instant.now())
            )
            .toList().stream() // Convert to list to avoid ConcurrentModificationException
            .forEach(this::removeSession);
            requestCount.clear();
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean validateSession(HttpRequest request) {
        Session session = getSession(request);
        lastActivity.put(session, Instant.now());

        Integer requests = requestCount.get(session);
        int count = requests == null ? 0 : requests;
        count++;
        requestCount.put(session, count);
        if (count > maxRequests && !getSessionUser(request).isAdmin()) {
            System.out.println("Ratelimit!");
            return false;
        }

        return true;
    }

    public Session getSession(UUID sessionUUID) {
        return sessionStore.get(sessionUUID);
    }
    public Session getSession(HttpRequest request) {
        // Search for cookie header
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("session")) {
                synchronized (sessionStore) {
                    Session session = sessionStore.get(UUID.fromString(cookie.getValue()));
                    // Check if the session exists
                    if (session != null) {
                        return session;
                    }
                }
            }
        }
        // Session does not exist yet
        Session session = new Session(request);
        sessionStore.put(session.getUUID(), session);
        return session;
    }
    public User getSessionUser(Session session) {
        synchronized (sessionUsers) {
            User user = User.getUser(sessionUsers.get(session));
            return user == null ? User.ANONYMOUS : user;
        }
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
