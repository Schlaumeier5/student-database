package de.igslandstuhl.database.server.sql;

public class SQLCommandNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SQLCommandNotFoundException(String command) {
        super("SQL command not found: " + command);
    }

    public SQLCommandNotFoundException(String command, Throwable cause) {
        super("SQL command not found: " + command, cause);
    }
    
}
