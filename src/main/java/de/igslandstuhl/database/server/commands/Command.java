package de.igslandstuhl.database.server.commands;

import de.igslandstuhl.database.Registry;

@FunctionalInterface
public interface Command {
    public String execute(String[] args);

    public static String executeCommand(String command, String[] args) {
        try {
            return Registry.commandRegistry().get(command).execute(args);
        } catch (NullPointerException e) {
            return "Command not found: " + command;
        }
    }
}
