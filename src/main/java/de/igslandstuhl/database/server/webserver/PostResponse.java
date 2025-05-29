package de.igslandstuhl.database.server.webserver;

import java.io.PrintWriter;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class PostResponse {
    private final int statusCode;
    private final String statusMessage;
    private final String body;
    private final String contentType;

    public PostResponse(int statusCode, String statusMessage, String body, String contentType) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.contentType = contentType;
    }

    public void respond(PrintWriter out) {
        out.print("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");
        out.print("Content-Type: " + contentType + "; charset=UTF-8\r\n");
        out.print("\r\n");
        if (body != null) {
            out.print(body);
        }
        out.flush();
    }

    public static PostResponse ok(String body, String contentType) {
        return new PostResponse(200, "OK", body, contentType);
    }

    public static PostResponse badRequest(String message) {
        return new PostResponse(400, "Bad Request", message, "text/plain");
    }

    public static PostResponse unauthorized(String message) {
        return new PostResponse(401, "Unauthorized", message, "text/plain");
    }
}