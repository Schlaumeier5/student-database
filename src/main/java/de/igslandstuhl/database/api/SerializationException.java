package de.igslandstuhl.database.api;

public class SerializationException extends Exception {
    public SerializationException() {}
    public SerializationException(String message) {super(message);}
    public SerializationException(Throwable cause) {super(cause);}
    public SerializationException(String message, Throwable cause) {super(message, cause);}
}
