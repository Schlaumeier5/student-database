package de.igslandstuhl.database.utils;

import java.util.Arrays;
import java.util.Scanner;

import de.igslandstuhl.database.server.commands.Command;

public class CommandLineUtils {
    private static final Scanner SCANNER = new Scanner(System.in);
    public static String input(Object... messages) {
        Arrays.stream(messages).forEach((m) -> System.out.print(m.toString() + " "));
        System.out.print(">>>");
        return SCANNER.nextLine();
    }
    public static void waitForCommandAndExec() {
        String[] args = input().split(" ");
        String command = args[0];
        System.out.println(Command.executeCommand(command, Arrays.copyOfRange(args, 1, args.length)));
    }
}
