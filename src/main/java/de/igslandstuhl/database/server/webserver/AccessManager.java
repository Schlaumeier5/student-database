package de.igslandstuhl.database.server.webserver;

import java.util.Arrays;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class AccessManager {
    private static final String[] PUBLIC_SPACES = {"error", "site", "icons"};
    private static final String USER_SPACE = "user";
    private static final String TEACHER_SPACE = "teacher";
    private static final String[] PUBLIC_LOCATIONS = {"rooms"};
    
    public static boolean hasAccess(String user, ResourceLocation resource) {
        if (Arrays.asList(PUBLIC_SPACES).contains(resource.namespace()) || Arrays.asList(PUBLIC_LOCATIONS).contains(resource.resource())) {
            return true;
        } else if (user != null) {
            if (resource.namespace().equals(USER_SPACE) || resource.resource().startsWith("my")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
