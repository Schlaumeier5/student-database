package de.igslandstuhl.database.server.webserver;

import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * Represents a POST request in the web server.
 * This class is used to parse the request line and body of a POST request,
 * extracting the path, parameters, and context.
 */
public class PostRequest {
    /**
     * Represents the path of the POST request.
     * This is the part of the request line that specifies the resource being requested.
     */
    private final String path;
    /**
     * Represents the parameters of the POST request.
     * This is a map of key-value pairs extracted from the request body.
     */
    private final Map<String, String> params = new HashMap<>();
    /**
     * Represents the context of the POST request.
     * This is derived from the path, typically indicating the type of resource (e.g., "html", "json").
     */
    private final String context;

    /**
     * Constructs a new PostRequest with the given request line and body.
     * @param requestLine the request line of the POST request
     * @param body the body of the POST request
     */
    public PostRequest(String requestLine, String body) {
        // Example: "POST /login HTTP/1.1"
        String[] parts = requestLine.split(" ");
        this.path = parts[1];

        // Parse body as form data: key1=value1&key2=value2
        if (body != null && !body.isEmpty()) {
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
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
     * Returns the path of the POST request.
     * This is the part of the request line that specifies the resource being requested.
     * @return the path of the POST request
     */
    public String getPath() {
        return path;
    }
    /**
     * Returns the parameters of the POST request.
     * This is a map of key-value pairs extracted from the request body.
     * @return a map containing the parameters of the POST request
     */
    public Map<String, String> getParams() {
        return params;
    }
    /**
     * Returns the context of the POST request.
     * This is derived from the path, typically indicating the type of resource (e.g., "html", "json").
     * @return the context of the POST request
     */
    public String getContext() {
        return context;
    }

    /**
     * Converts the path of the POST request to a ResourceLocation.
     * This method is used to create a ResourceLocation object from the path,
     * which can be used for further processing or resource management.
     * @return a ResourceLocation object representing the path of the POST request
     */
    public ResourceLocation toResourceLocation() {
        return de.igslandstuhl.database.server.webserver.WebResourceHandler.locationFromPath(path);
    }
}