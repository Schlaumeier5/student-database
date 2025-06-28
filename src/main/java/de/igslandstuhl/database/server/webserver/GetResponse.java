package de.igslandstuhl.database.server.webserver;

import java.io.PrintWriter;

import de.igslandstuhl.database.server.resources.ResourceHelper;
import de.igslandstuhl.database.server.resources.ResourceLocation;

public class GetResponse {
    public enum Status {
        OK (200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        FORBIDDEN (403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        I_AM_A_TEAPOT(418, "I'm a teapot"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error")
        ;
        private final int code;
        private final String name;
        private Status(int code, String name) {
            this.code = code;
            this.name = name;
        }
        public int getCode() {
            return code;
        }
        public String getName() {
            return name;
        }
        public void write(PrintWriter out) {
            out.print(code);out.print(" ");out.print(name);
        }
    }

    private static final GetResponse NOT_FOUND = new GetResponse(Status.NOT_FOUND, new ResourceLocation("html", "errors", "404.html"), ContentType.HTML, "");
    public static GetResponse notFound() {
        return NOT_FOUND;
    }
    private static final GetResponse INTERNAL_ERROR = new GetResponse(Status.INTERNAL_SERVER_ERROR, new ResourceLocation("html", "errors", "500.html"), ContentType.HTML, "");
    public static GetResponse internalServerError() {
        return INTERNAL_ERROR;
    }
    private static final GetResponse FORBIDDEN = new GetResponse(Status.FORBIDDEN, new ResourceLocation("html", "errors", "403.html"), ContentType.HTML, "");
    public static GetResponse forbidden() {
        return FORBIDDEN;
    }
    private static final GetResponse UNAUTHORIZED = new GetResponse(Status.UNAUTHORIZED, new ResourceLocation("html", "errors", "401.html"), ContentType.HTML, "");
    public static GetResponse unauthorized() {
        return UNAUTHORIZED;
    }
    private final Status status;
    private final ResourceLocation resourceLocation;
    private final ContentType contentType;
    private final String charset = "UTF-8";
    private final String user;
    public GetResponse(Status status, ResourceLocation resourceLocation, ContentType contentType, String user) {
        this.status = status;
        this.resourceLocation = resourceLocation;
        this.contentType = contentType;
        this.user = user;
    }

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
        } catch (NullPointerException e) {
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
}
