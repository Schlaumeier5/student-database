package de.igslandstuhl.database.server.webserver;

import java.util.Arrays;

import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * AccessManager is responsible for managing access to resources based on user roles and resource locations.
 * It determines whether a user has access to a specific resource based on predefined rules.
 */
public class AccessManager {
    /**
     * Public spaces and locations that are accessible without authentication.
     * These resources can be accessed by anyone, regardless of their authentication status.
     */
    private static final String[] PUBLIC_SPACES = {"error", "site", "icons"};
    /**
     * The user space is restricted to authenticated users.
     */
    private static final String USER_SPACE = "user";
    /**
     * The teacher space is restricted to authenticated teachers.
     */
    private static final String TEACHER_SPACE = "teacher";
    /**
     * The admin space is restricted to authenticated admins.
     */
    private static final String ADMIN_SPACE = "admin";
    /**
     * Public locations that are accessible without authentication.
     * These resources can be accessed by anyone, regardless of their authentication status.
     */
    private static final String[] PUBLIC_LOCATIONS = {"rooms"};
    /**
     * Admin locations that are accessible only to authenticated admins.
     * These resources require admin privileges for access.
     */
    private static final String[] ADMIN_LOCATIONS = {"students", "teachers"};
    
    /**
     * Checks if a user has access to a specific resource.
     * 
     * @param user the username of the user, or null if not authenticated
     * @param resource the ResourceLocation representing the resource to check access for
     * @return true if the user has access to the resource, false otherwise
     */
    public static boolean hasAccess(String user, ResourceLocation resource) {
        if (Arrays.asList(PUBLIC_SPACES).contains(resource.namespace()) || Arrays.asList(PUBLIC_LOCATIONS).contains(resource.resource())) {
            return true;
        } else if (user != null) {
            if (resource.namespace().equals(USER_SPACE) || resource.resource().startsWith("my")) {
                return true;
            } else if (resource.namespace().equals(TEACHER_SPACE)) {
                return User.getUser(user).isTeacher();
            } else if (resource.namespace().equals(ADMIN_SPACE) || Arrays.asList(ADMIN_LOCATIONS).contains(resource.resource())) {
                return User.getUser(user).isAdmin();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
