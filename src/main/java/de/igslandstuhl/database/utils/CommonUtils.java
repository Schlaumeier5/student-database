package de.igslandstuhl.database.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CommonUtils {
    public static String getStacktrace(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
