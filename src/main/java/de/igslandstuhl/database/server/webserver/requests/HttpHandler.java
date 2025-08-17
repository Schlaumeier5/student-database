package de.igslandstuhl.database.server.webserver.requests;

import java.util.function.Function;

import de.igslandstuhl.database.Registry;
import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.webserver.AccessLevel;
import de.igslandstuhl.database.server.webserver.SessionManager;
import de.igslandstuhl.database.server.webserver.Status;
import de.igslandstuhl.database.server.webserver.responses.HttpResponse;

public class HttpHandler<Rq extends HttpRequest> {
    private final String path;
    private final AccessLevel accessLevel;
    private final Function<Rq, HttpResponse> handler;

    private HttpHandler(String path, AccessLevel accessLevel, Function<Rq, HttpResponse> handler) {
        this.accessLevel = accessLevel;
        this.handler = handler;
        this.path = path;
    }

    public HttpResponse handleHttpRequest(Rq request) {
        SessionManager sessionManager = Server.getInstance().getWebServer().getSessionManager();
        int contentLength = request.getContentLength();
        if (contentLength <= 0 && !(request instanceof GetRequest)) {
            return HttpResponse.error(request, Status.BAD_REQUEST);
        }
        if (!accessLevel.hasAccess(sessionManager.getSessionUser(request))) {
            return HttpResponse.error(request, Status.UNAUTHORIZED);
        } else if (path != request.getPath().split("\\?")[0]) {
            System.err.println("Wrong path for HTTP handler: " + handler + ", path: " + request.getPath());
            return HttpResponse.error(request, Status.INTERNAL_SERVER_ERROR);
        } else {
            try {
                return handler.apply(request);
            } catch (Throwable t) {
                return HttpResponse.error(request, Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    public static void registerPostRequestHandler(String path, AccessLevel accessLevel, Function<APIPostRequest, HttpResponse> handler) {
        Registry.postRequestHandlerRegistry().register(path, new HttpHandler<>(path, accessLevel, handler));
    }
    public static void registerGetRequestHandler(String path, AccessLevel accessLevel, Function<GetRequest, HttpResponse> handler) {
        Registry.getRequestHandlerRegistry().register(path, new HttpHandler<>(path, accessLevel, handler));
    }
}
