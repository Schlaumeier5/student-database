package de.igslandstuhl.database.utils;

import java.util.Arrays;
import java.util.Scanner;

public class CommandLineUtils {
    public static String input(Object... messages) {
        Arrays.stream(messages).forEach((m) -> System.out.print(m.toString() + " "));
        System.out.print(">>>");
        try (Scanner scanner = new Scanner(System.in)) {
            return scanner.nextLine();
        }
    }
}
