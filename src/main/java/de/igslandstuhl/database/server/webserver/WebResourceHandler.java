package de.igslandstuhl.database.server.webserver;

import java.util.Arrays;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public final class WebResourceHandler {
    private static final String[] SQL_WEB_RESOURCES = {"/mydata", "/rooms", "/mysubjects"};
    private static String[] userOnlySpace = {"dashboard", "fetch_data.js"};
    private static String[] teacherOnlySpace = {};
    private WebResourceHandler(){}

    private static boolean isSQLWebResource(String path) {
        return Arrays.asList(SQL_WEB_RESOURCES).contains(path);
    }

    private static boolean inUserOnlySpace(String path) {
        for (String userOnlyPaths : userOnlySpace) {
            if (path.contains(userOnlyPaths)) {
                return true;
            }
        }
        return false;
    }
    private static boolean inTeacherOnlySpace(String path) {
        for (String userOnlyPaths : teacherOnlySpace) {
            if (path.contains(userOnlyPaths)) {
                return true;
            }
        }
        return false;
    }

    public static ResourceLocation locationFromPath(String path) {
        if (isSQLWebResource(path)) {
            return new ResourceLocation("virtual", "sql", path);
        }
        if (path.equals("/")) {
            path = "/index.html";
        } else if (path.endsWith(".ico")) {
            path = "/icons" + path;
        }
        String[] parts = path.split("\\.");
        
        String context;
        if (path.endsWith(".ico") || path.endsWith(".png")) {
            context = "imgs";
        } else if (parts.length > 1) {
            context = parts[1];
        } else {
            context = "html";
            path += ".html";
        }

        parts = path.split("/", 3);
        String namespace;
        String resource;
        if (parts.length > 2) {
            namespace = parts[1];
            resource = parts[2];
        } else {
            if (inTeacherOnlySpace(parts[1])) {
                namespace = "teacher";
            } else if (inUserOnlySpace(parts[1])) {
                namespace = "user";
            } else {
                namespace = "site";
            }
            resource = parts[1];
        }

        return new ResourceLocation(context, namespace, resource);
    }
}
