package de.igslandstuhl.database.server.webserver;

import java.util.Arrays;

import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * Handles the mapping of web resource paths to resource locations.
 * This class provides methods to determine if a path is a SQL web resource,
 * and to convert paths into ResourceLocation objects.
 */
public final class WebResourceHandler {
    private static final String[] SQL_WEB_RESOURCES = {"/mydata", "/rooms", "/mysubjects", "/myclasses", "/teachers", "/students"};
    private static String[] userOnlySpace = {"dashboard", "build_dashboard.js", "results", "build_results.js"};
    private static String[] teacherOnlySpace = {"dashboard", "build_dashboard.js", "student", "build_student.js", "student-results", "build_results.js"};
    private static String[] adminOnlySpace = {"dashboard", "build_dashboard.js", "manage_students", "manage_teachers", "manage_classes", "manage_subjects", "manage_rooms", "student", "build_student.js", "student-results", "build_results.js", "teacher", "build_teacher.js", "room", "build_room.js", "subject", "build_subject.js", "schoolclass", "build_schoolclass.js", "/teacher-classes"};

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
    private static boolean inAdminOnlySpace(String path) {
        for (String userOnlyPaths : adminOnlySpace) {
            if (path.contains(userOnlyPaths)) {
                return true;
            }
        }
        return false;
    }

    public static ResourceLocation locationFromPath(String path, String username) {
        if (isSQLWebResource(path)) {
            return new ResourceLocation("virtual", "sql", path.replaceFirst("/", ""));
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

        User user = username == null ? null : User.getUser(username);

        parts = path.split("/", 3);
        String namespace;
        String resource;
        if (parts.length > 2) {
            namespace = parts[1];
            resource = parts[2];
        } else {
            if (inAdminOnlySpace(parts[1]) && (user == null || user.isAdmin())) {
                namespace = "admin";
            } else if (inTeacherOnlySpace(parts[1]) && (user == null || user.isTeacher())) {
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
