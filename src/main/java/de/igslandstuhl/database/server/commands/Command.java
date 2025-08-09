package de.igslandstuhl.database.server.commands;

import de.igslandstuhl.database.Registry;
import de.igslandstuhl.database.api.Admin;
import de.igslandstuhl.database.utils.CommonUtils;

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
    public static void registerCommand(String name, Command command) {
        Registry.commandRegistry().register(name, command);
    }
    public static void registerCommands() {
        // Add admin
        registerCommand("add-admin", (args) -> {
            if (args.length < 2) return "Usage: add-admin [username] [password]";
            try {
                String username = args[0];
                String password = args[1];
                Admin.create(username, password);
            } catch (Exception e) {
                return "Error while trying to add admin:\n" + CommonUtils.getStacktrace(e);
            }
            return "Successfully added admin";
        });
        registerCommand("remove-admin", (args) -> {
            if (args.length < 1) return "Usage: remove-admin [username]";
            try {
                Admin.get(args[0]).delete();
            } catch (NullPointerException e) {
                return "Admin not found";
            } catch (Exception e) {
                return "Error while trying to remove admin:\n" + CommonUtils.getStacktrace(e);
            }
            return "Successfully removed admin";
        });
    }
}
