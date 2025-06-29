package de.igslandstuhl.database.server.webserver;

import java.io.PrintWriter;

/**
 * Represents a response to a POST request in the web server.
 */
public class PostResponse {
    /**
     * The HTTP status code of this response.
     * This code indicates the result of processing the POST request.
     * For example, 200 for success, 400 for bad request, etc.
     * @see Status
     */
    private final Status statusCode;
    private final String body;
    private final ContentType contentType;

    public PostResponse(Status statusCode, String body, ContentType contentType) {
        this.statusCode = statusCode;
        this.body = body;
        this.contentType = contentType;
    }

    public void respond(PrintWriter out) {
        out.print("HTTP/1.1 ");
        statusCode.write(out);
        out.print("\r\n");
        out.print("Content-Type: " + contentType + "; charset=UTF-8\r\n");
        out.print("\r\n");
        if (body != null) {
            out.print(body);
        }
        out.flush();
    }

    public static PostResponse ok(String body, ContentType contentType) {
        return new PostResponse(Status.OK, body, contentType);
    }

    public static PostResponse badRequest(String message) {
        return new PostResponse(Status.BAD_REQUEST, message, ContentType.TEXT_PLAIN);
    }

    public static PostResponse unauthorized(String message) {
        return new PostResponse(Status.UNAUTHORIZED, message, ContentType.TEXT_PLAIN);
    }

    public static PostResponse internalServerError(String message) {
        return new PostResponse(Status.INTERNAL_SERVER_ERROR, message, ContentType.TEXT_PLAIN);
    }
}