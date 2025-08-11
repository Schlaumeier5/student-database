package de.igslandstuhl.database.api;

import java.sql.SQLException;

import de.igslandstuhl.database.server.Server;

public class PreConditions {
    public static void setupDatabase() throws SQLException {
        Server server = Server.getInstance();
        server.getConnection().createTables();
    }
    public static void addSampleStudent() throws SQLException {
        Student.registerStudentWithPassword(0, "Max", "Mustermann", "max@muster.mann", "12345", SchoolClass.get(1), 1);
    }
    public static void addSampleSubject() throws SQLException {
        Subject.addSubject("Mathematik");
    }
    public static void addSampleTopic() throws SQLException {
        Topic.addTopic("Bruchrechnung", Subject.get(1), 100, 5, 1);
    }
    public static void addSampleTask() throws SQLException {
        Task.addTask(Topic.get(1), "Addition", TaskLevel.LEVEL1);
    }
    public static void addSampleSpecialTask() throws SQLException {
        Subject subject = Subject.get(1);
        SpecialTask.addSpecialTask("Nansteinaufgabe", 0.05, subject);
    }
    public static void addSampleClass() throws SQLException {
        SchoolClass.addClass("5a", 5);
    }
}