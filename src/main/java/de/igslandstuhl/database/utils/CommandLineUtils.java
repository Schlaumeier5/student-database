package de.igslandstuhl.database.utils;

import java.util.Arrays;
import java.util.Scanner;

public class CommandLineUtils {
    private static final Scanner SCANNER = new Scanner(System.in);
    public static String input(Object... messages) {
        Arrays.stream(messages).forEach((m) -> System.out.print(m.toString() + " "));
        System.out.print(">>>");
        return SCANNER.nextLine();
    }
}
