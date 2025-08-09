package de.igslandstuhl.database.server.commands;

import java.sql.SQLException;
import java.util.Arrays;

import de.igslandstuhl.database.Registry;
import de.igslandstuhl.database.api.*;
import de.igslandstuhl.database.utils.CommonUtils;

@FunctionalInterface
public interface Command {
    public String execute(String[] args);

    public static String executeCommand(String command, String[] args) {
        try {
            return Registry.commandRegistry().get(command).execute(args);
        } catch (NullPointerException e) {
            return "Command not found: " + command;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
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
        registerCommand("get-level-ratio", (args) -> {
            if (args.length < 1) return "Usage: get-level-ratio [level]";
            Level level;
            try {
                level = Level.get(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                return "This is no valid number.";
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
            return String.valueOf(level.getRatio() * 100) + "%";
        });
        registerCommand("list-rooms", (args) -> {
            try {
                Room.fetchAll();
            } catch (SQLException e) {
                return "Error while trying to access database:\n" + CommonUtils.getStacktrace(e);
            }
            return Room.getRooms().keySet().stream().reduce("Rooms:", (s1, s2) -> s1 + "\n" + s2);
        });
        

        // Room commands
        registerCommand("get-room-level", (args) -> {
            if (args.length < 1) return "Usage: get-room-level [room]";
            Room room = Room.getRoom(argsPart(args, 0, args.length));
            if (room == null) return "Room not found. Try list-rooms for a list of available rooms";
            return "Room " + room.getLabel() + " has access level " + room.getMinimumLevel();
        });
        registerCommand("set-room-level", (args) -> {
            if (args.length < 2) return "Usage: set-room-level [room] [level]";
            Room room = Room.getRoom(argsPart(args, 0, args.length-1));
            if (room == null) return "Room not found. Try list-rooms for a list of available rooms";
            int level;
            try {
                level = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException e) {
                return args[1] + " is not a valid number.";
            }
            try {
                room.setMinimumLevel(level);
            } catch (SQLException e) {
                return "Error while trying to access database: \n" + CommonUtils.getStacktrace(e);
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
            return "Successfully changed room level";
        });
        registerCommand("add-room", (args) -> {
            if (args.length < 2) return "Usage: add-room [room] [level]";
            if (Room.getRoom(args[0]) != null) return "Room already present";

            try {
                String label = argsPart(args, 0, args.length-1);
                int level = Integer.parseInt(args[args.length-1]);

                Room.addRoom(label, level);
            } catch (NumberFormatException e) {
                return args[args.length - 1] + " is not a valid number.";
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return "Room successfully added";
        });
        registerCommand("remove-room", (args) -> {
            if (args.length < 1) return "Usage: remove-room [room]";

            Room room = Room.getRoom(argsPart(args, 0, args.length));
            try {
                room.delete();
            } catch (SQLException e) {
                return "Error while trying to access database: \n" + CommonUtils.getStacktrace(e);
            }
            return "Room successfully deleted";
        });


        // class commands
        registerCommand("list-classes", (args) -> {
            return SchoolClass.getAll().stream().map(SchoolClass::getLabel).reduce("Classes:", (s1,s2) -> s1 + "\n" + s2);
        });
        registerCommand("add-subject-to-class", (args) -> {
            if (args.length != 2) return "Usage: add-subject-to-class [subject] [class]";

            Subject subject = Subject.get(args[0]);
            SchoolClass schoolClass = SchoolClass.get(args[1]);

            if (subject == null) return "Subject not found";
            if (schoolClass == null) return "Class not found";

            try {
                schoolClass.addSubject(subject);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return "Successfully added subject to class";
        });
        registerCommand(("set-class-label"), (args) -> {
            if (args.length != 2) return "Usage: set-class-label [old] [new]";

            SchoolClass schoolClass = SchoolClass.get(args[0]);
            
            try {
                schoolClass.setLabel(args[1]);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }

            return "Class label successfully updated.";
        });
        registerCommand(("set-class-grade"), (args) -> {
            if (args.length != 2) return "Usage: set-class-grade [class] [grade]";

            SchoolClass schoolClass = SchoolClass.get(args[0]);
            int grade;
            try {
                grade = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return args[1] + " is not a valid number.";
            }
            
            try {
                schoolClass.setGrade(grade);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }

            return "Class grade successfully updated.";
        });
        registerCommand("add-class", (args) -> {
            if (args.length != 1) return "Usage: add-class [class]";
            SchoolClass schoolClass = SchoolClass.getOrCreate(args[0]);
            return "Successfully created class " + schoolClass.getLabel() + " of grade " + schoolClass.getGrade();
        });
        registerCommand("remove-class", (args) -> {
            if (args.length != 1) return "Usage: remove-class [class]";
            SchoolClass schoolClass = SchoolClass.get(args[0]);
            if (schoolClass == null) return "Class not found";
            try {
                schoolClass.delete();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return "Successfully deleted class";
        });
    }
    private static String argsPart(String[] args, int start, int end) {
        return Arrays.stream(Arrays.copyOfRange(args, start, end)).reduce("", (s1,s2) -> s1+s2);
    }
}
