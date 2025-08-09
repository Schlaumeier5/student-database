package de.igslandstuhl.database.utils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

import de.igslandstuhl.database.server.commands.Command;

public class CommandLineUtils {
    private static boolean waitingForInput = false;
    private static String prompt = "";
    private static PrintStream origin = System.out;
    private static final Scanner SCANNER = new Scanner(System.in);
    public static synchronized String input(Object... messages) {
        waitingForInput = true;
        prompt = Arrays.stream(messages).map(String::valueOf).map((s) -> s + " ").reduce("", (s1,s2) -> s1+s2) + ">>>";
        try {
            printPrompt(origin);
            return SCANNER.nextLine();
        } finally {
            waitingForInput = false;
        }
    }
    public static void waitForCommandAndExec() {
        String[] args = input().split(" ");
        String command = args[0];
        System.out.println(Command.executeCommand(command, Arrays.copyOfRange(args, 1, args.length)));
    }
    private static void printPrompt(PrintStream stream) {
        if (waitingForInput){
            stream.print(prompt);
        }
    }
    private static void clearPrompt(PrintStream  stream) {
        if (waitingForInput) {
            stream.print("\u001B[2K\r");
            stream.flush();
        }
    }
    public static void setup() {
        origin = System.out;
        System.setOut(new PrintStreamWrapper(origin, origin, CommandLineUtils::clearPrompt, CommandLineUtils::printPrompt));
        System.setErr(new PrintStreamWrapper(System.err, origin, CommandLineUtils::clearPrompt, CommandLineUtils::printPrompt));
    }
}
