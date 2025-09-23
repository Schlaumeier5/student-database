package de.igslandstuhl.database.server.commands;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.igslandstuhl.database.Registry;
import de.igslandstuhl.database.api.*;
import de.igslandstuhl.database.server.Server;
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
        registerCommand("exit", (args) -> {
            System.out.println("Exiting...");
            System.exit(0);
            return "";
        });
        registerCommand("help", (args) -> {
            return Registry.commandRegistry().keyStream().reduce("Available Commands:", (s1,s2) -> s1+"\n"+s2);
        });
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
            TaskLevel level;
            try {
                level = TaskLevel.get(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                return "This is no valid number.";
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
            return String.valueOf(level.getRatio() * 100) + "%";
        });

        // Room commands
        registerCommand("list-rooms", (args) -> {
            try {
                Room.fetchAll();
            } catch (SQLException e) {
                return "Error while trying to access database:\n" + CommonUtils.getStacktrace(e);
            }
            return Room.getRooms().keySet().stream().reduce("Rooms:", (s1, s2) -> s1 + "\n" + s2);
        });
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
        // school year
        registerCommand("list-school-years", (args) -> {
            return SchoolYear.getAll().stream().map(SchoolYear::getLabel).reduce("School years:", (s1,s2) -> s1 + "\n" + s2);
        });
        registerCommand("get-current-school-year", (args) -> {
            SchoolYear current = SchoolYear.getCurrentYear();
            if (current == null) return "No school year found.";
            return "Current school year: " + current.getLabel() + ", current week: " + current.getCurrentWeek() + ", total week count: " + current.getWeekCount();
        });
        registerCommand("get-current-week", (args) -> {
            SchoolYear current = SchoolYear.getCurrentYear();
            if (current == null) return "No school year found.";
            return "Current week: " + current.getCurrentWeek();
        });
        registerCommand("set-current-week", (args) -> {
            if (args.length != 1) return "Usage: set-current-week [week]";
            int week;
            try {
                week = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return "You have to specify a valid number";
            }
            SchoolYear current = SchoolYear.getCurrentYear();
            if (current == null) return "No school year found";
            if (week < 0 || week > current.getWeekCount()) return "Week not inside school year";
            try {
                current.setCurrentWeek(week);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return "Week successfully changed";
        });
        registerCommand("inc-week", (args) -> {
            SchoolYear current = SchoolYear.getCurrentYear();
            if (current == null) return "No school year found";
            try {
                current.setCurrentWeek(current.getCurrentWeek() + 1);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return "Week successfully changed";
        });
        registerCommand("add-school-year", (args) -> {
            if (args.length != 2) return "Usage: add-school-year [label] [week count]";
            try {
                SchoolYear.addSchoolYear(args[0], Integer.parseInt(args[1]), 1);
            } catch (NumberFormatException e) {
                return "No valid week count";
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return "Successfully added school year";
        });
        registerCommand("remove-school-year", (args) -> {
            if (args.length != 1) return "Usage: remove-school-year [label]";
            SchoolYear schoolYear = SchoolYear.get(args[0]);
            if (schoolYear == null) return "School year not found";
            try {
                schoolYear.delete();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return "School Year successfully removed";
        });
        // User commands
        registerCommand("regenerate-user-password", (args) -> {
            if (args.length != 1) return "Usage: regenerate-user-password [user]";
            User user = User.getUser(args[0]);
            if (user == null) return "User not found";
            try {
                return "Password: " + user.regeneratePassword();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        registerCommand("generate-password", (args) -> {
            return "Passwort: " + User.generateRandomPassword(12, CommonUtils.stringToSeed(argsPart(args, 0, args.length)) + System.currentTimeMillis() + new Random().nextInt(1000));
        });
        registerCommand("change-user-password", (args) -> {
            if (args.length != 2) return "Usage: change-user-password [user] [password]";
            User user = User.getUser(args[0]);
            if (user == null) return "User not found";
            try {
                user.setPassword(args[1]);
                return "Changed password of " + user.getUsername() + " to " + args[1];
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        // Subject commands
        registerCommand("list-subjects", (args) -> {
            return Subject.getAll().stream().map(Subject::getName).reduce("Subjects:", (s1,s2) -> s1+"\n"+s2);
        });
        registerCommand("add-subject", (args) -> {
            if (args.length < 1) return "Usage: add-subject [subject]";
            String name = argsPart(args, 0, args.length);
            try {
                Subject subject = Subject.addSubject(name);
                return "Subject '" + name + "' successfully created with id " + subject.getId();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        registerCommand("rename-subject", (args) -> {
            if (args.length < 3) return "Usage: rename-subject [old name] : [new name]";
            
            String[] parts = argsPart(args, 0, args.length).split("[\\s]*:[\\s]*");
            if (parts.length != 2) return "Usage: add-subject [old name] : [new name]";
            String oldLabel = parts[0];
            String newLabel = parts[1];
            
            Subject subject = Subject.get(oldLabel);
            if (subject == null) return "Subject " + oldLabel + " does not exist";
            try {
                subject.edit(newLabel);
                return "Successfully changed subject to " + newLabel;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        registerCommand("remove-subject", (args) -> {
            if (args.length == 0) return "remove-subject [name]";
            String name = argsPart(args, 0, args.length);
            Subject subject = Subject.get(name);
            if (subject == null) return "Subject " + name + " does not exist";
            try {
                subject.delete();
                return "Successfully deleted " + name;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        // manual sql commands
        registerCommand("sql-update", (args) -> {
            String command = argsPart(args, 0, args.length);
            try {
                Server.getInstance().getConnection().executeVoidProcessSecure((stmt) -> stmt.executeUpdate(command));
            } catch (SQLException e) {
                throw new IllegalArgumentException(e);
            }
            return "Successfully executed";
        });
    }
    private static String argsPart(String[] args, int start, int end) {
        return Arrays.stream(Arrays.copyOfRange(args, start, end)).reduce("", (s1,s2) -> s1+" "+s2).replaceFirst(" ", "");
    }
    public static List<String> getAllCommandNames() {
        return Registry.commandRegistry().keyStream().toList();
    }
}
