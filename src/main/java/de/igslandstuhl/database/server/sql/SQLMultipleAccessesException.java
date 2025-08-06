package de.igslandstuhl.database.server.sql;

public class SQLMultipleAccessesException extends RuntimeException {
    public SQLMultipleAccessesException() {}
    public SQLMultipleAccessesException(String msg) { super(msg); System.out.println("Test");}
    public SQLMultipleAccessesException(Throwable cause) { super(cause); }
    public SQLMultipleAccessesException(String msg, Throwable cause) { super(msg, cause); }
}
