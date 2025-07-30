package de.igslandstuhl.database.server.webserver;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import de.igslandstuhl.database.server.resources.ResourceHelper;
import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * Represents a response to a GET request in the web server.
 */
public class GetResponse {
    /**
     * Represents a response for a GET request that was not found.
     */
    private static final GetResponse NOT_FOUND = new GetResponse(Status.NOT_FOUND, new ResourceLocation("html", "errors", "404.html"), ContentType.HTML, "");
    /**
     * Returns a response for a GET request that was not found.
     * @return the GetResponse object
     */
    public static GetResponse notFound() {
        return NOT_FOUND;
    }
    /**
     * Represents a response for a GET request that resulted in an internal error.
     */
    private static final GetResponse INTERNAL_ERROR = new GetResponse(Status.INTERNAL_SERVER_ERROR, new ResourceLocation("html", "errors", "500.html"), ContentType.HTML, "");
    /**
     * Returns a response for a GET request that resulted in an internal error.
     * @return the GetResponse object
     */
    public static GetResponse internalServerError() {
        return INTERNAL_ERROR;
    }
    /**
     * Represents a response for a GET request the user has no access to.
     */
    private static final GetResponse FORBIDDEN = new GetResponse(Status.FORBIDDEN, new ResourceLocation("html", "errors", "403.html"), ContentType.HTML, "");
    /**
     * Returns a response for a GET request the user has no access to.
     * @return the GetRequest object
     */
    public static GetResponse forbidden() {
        return FORBIDDEN;
    }
    /**
     * Represents a response for a GET request the user must be logged in for.
     */
    private static final GetResponse UNAUTHORIZED = new GetResponse(Status.UNAUTHORIZED, new ResourceLocation("html", "errors", "401.html"), ContentType.HTML, "");
    /**
     * Returns a response for a GET request the user must be logged in for.
     * @return the GetResponse object
     */
    public static GetResponse unauthorized() {
        return UNAUTHORIZED;
    }
    /**
     * The HTTP status of this response
     * @see Status
     */
    private final Status status;
    /**
     * The location of the resource to be returned.
     */
    private final ResourceLocation resourceLocation;
    /**
     * The HTTP content type of the resource.
     * @see ContentType
     */
    private final ContentType contentType;
    /**
     * The character set used for the response.
     * This is always UTF-8.
     */
    private final String charset = "UTF-8";
    /**
     * The user who made the request.
     * This is used to check access permissions.
     */
    private final String user;

    /**
     * Creates a new GetResponse with the given parameters.
     * This constructor is used to create a response for a GET request.
     * @param status the HTTP status of the response
     * @param resourceLocation the location of the resource to be returned
     * @param contentType the HTTP content type of the resource
     * @param user the user who made the request
     */
    public GetResponse(Status status, ResourceLocation resourceLocation, ContentType contentType, String user) {
        this.status = status;
        this.resourceLocation = resourceLocation;
        this.contentType = contentType;
        this.user = user;
    }
    /**
     * Returns a response for a GET request for the given resource.
     * @param resourceLocation the location of the resource to be returned
     * @param user the user who made the request
     * @return the GetResponse object
     */
    public static GetResponse getResource(ResourceLocation resourceLocation, String user) {
        try {
            if (AccessManager.hasAccess(user, resourceLocation)) {
                return new GetResponse(Status.OK, resourceLocation, ContentType.ofResourceLocation(resourceLocation), user);
            } else {
                return unauthorized();
            }
        } catch (NoWebResourceException e) {
            return forbidden();
        }
    }

    /**
     * Responds to the GET request by writing the response to the given PrintWriter.
     * This method formats the HTTP response header and body, including the status, content type, and resource.
     * @param out the PrintWriter to write the response to
     */
    public void respond(PrintWriter out) {
        try {
            String resource = "";
            if (resourceLocation != null) {
                if (!resourceLocation.isVirtual()) {
                    resource = ResourceHelper.readResourceCompletely(resourceLocation);
                } else {
                    resource = ResourceHelper.readVirtualResource(user, resourceLocation);
                    if (resource == null) throw new NullPointerException();
                }
            }
            out.print("HTTP/1.1 "); status.write(out); out.println();
            if (contentType != null) {
                out.print("Content-Type: "); out.print(contentType.getName());
                out.print("; charset="); out.println(charset);
            }
            out.println(); // <--- Diese Zeile ist wichtig: trennt Header von Body!
            out.println(resource);
        } catch (FileNotFoundException e) {
            notFound().respond(out);
        } catch (Exception e) {
            e.printStackTrace();
            if (status != Status.INTERNAL_SERVER_ERROR) {
                internalServerError().respond(out);
            } else {
                throw new IllegalStateException("Uncaught exception", e);
            }
        }
    }

    public String getResponseBody() throws FileNotFoundException {
        if (resourceLocation != null) {
            if (!resourceLocation.isVirtual()) {
                return ResourceHelper.readResourceCompletely(resourceLocation);
            } else {
                return ResourceHelper.readVirtualResource(user, resourceLocation);
            }
        }
        return "";
    }
}
