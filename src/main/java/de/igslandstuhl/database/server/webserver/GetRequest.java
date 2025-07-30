package de.igslandstuhl.database.server.webserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * Represents a GET request in the web server.
 * It parses the request string to extract the path, query parameters, and context.
 * The context is determined by the file extension of the requested resource.
 */
public class GetRequest {
    /**
     * Valid contexts for web resources.
     * These contexts are used to determine the type of resource being requested.
     */
    private static final String[] validContexts = {"html", "js", "css"};
    /**
     * The path of the requested resource.
     * This is the part of the URL that comes after the domain and before any query parameters.
     */
    private final String path;
    /**
     * The query parameters of the request.
     * These are key-value pairs that come after the '?' in the URL.
     */
    private final Map<String, String> args = new HashMap<>();
    /**
     * The context of the requested resource.
     * This is determined by the file extension of the requested resource.
     */
    private final String context;

    /**
     * Constructs a new GetRequest from the given request string.
     * @param request the request string to parse
     */
    public GetRequest(String request) {
        if (!request.startsWith("GET")){
            throw new IllegalArgumentException();
        }
        String [] lines = request.split("\n");

        String url = lines[0].split(" ")[1];
        String[] urlParts = url.split("\\?");
        path = urlParts[0];
        if (urlParts.length > 1) {
            String[] arg_pairs = urlParts[1].split("&");
            for (String arg: arg_pairs) {
                String[] kv = arg.split("=");
                args.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        String[] extPts = path.split("\\.");
        if (extPts.length > 1) {
            context = extPts[1];
        } else {
            context = "html";
        }
    }

    /**
     * Determines if the request is valid based on its context.
     * A request is considered valid if its context is one of the predefined valid contexts.
     * @return true if the request is valid, false otherwise
     */
    public boolean isValid() {
        return Arrays.asList(validContexts).contains(context);
    }
    /**
     * Returns the resource location for this request, wrapped in a ResourceLocation object.
     * The resource location is constructed from the path of the request.
     * @param user the username associated with the session, or null if not logged in
     * @return the ResourceLocation for this request
     * @see de.igslandstuhl.database.server.resources.ResourceLocation
     */
    public ResourceLocation toResourceLocation(String user) {
        return WebResourceHandler.locationFromPath(path, user);
    }
}
