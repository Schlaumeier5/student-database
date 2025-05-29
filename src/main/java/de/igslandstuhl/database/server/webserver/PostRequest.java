package de.igslandstuhl.database.server.webserver;

import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class PostRequest {
    private final String path;
    private final Map<String, String> params = new HashMap<>();
    private final String context;

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

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getContext() {
        return context;
    }

    public ResourceLocation toResourceLocation() {
        return de.igslandstuhl.database.server.webserver.WebResourceHandler.locationFromPath(path);
    }
}