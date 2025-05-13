package de.igslandstuhl.database.server.webserver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class GetRequest {
    private static final String[] validContexts = {"html", "js", "css"};
    private final String path;
    private final Map<String, String> args = new HashMap<>();
    private final String context;
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

    public boolean isValid() {
        return Arrays.asList(validContexts).contains(context);
    }

    public ResourceLocation toResourceLocation() {
        return WebResourceHandler.locationFromPath(path);
    }
}
