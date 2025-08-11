package de.igslandstuhl.database.server.webserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.igslandstuhl.database.api.User;
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
    private final String body;
    /**
     * Represents the context of the POST request.
     * This is derived from the path, typically indicating the type of resource (e.g., "html", "json").
     */
    private final String context;
    /**
     * Represents the content length of the POST request.
     * This is used to determine the size of the request body.
     */
    private final int contentLength;
    /**
     * Represents the cookies associated with the POST request.
     * This is an array of Cookie objects that may be included in the request.
     */
    private final Cookie[] cookies;

    /**
     * Constructs a new PostRequest with the given header and body.
     * @param header the header of the POST request
     * @param body the body of the POST request
     */
    public PostRequest(String header, String body) {
        // Example: "POST /login HTTP/1.1"
        String[] parts = header.split(" ");
        this.path = parts[1];
        this.body = body != null ? body : "";
        
        int contentLength = 0;
        Cookie[] cookies = new Cookie[0];
        for (String line : header.split("\n")) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("Cookie:")) {
                String[] cookieData = line.substring(7).split("; ");
                List<Cookie> cookieList = new ArrayList<>();
                for (String cookie : cookieData) {
                    String[] keyValue = cookie.split("=");
                    if (keyValue.length == 2) {
                        cookieList.add(new Cookie(keyValue[0].trim(), keyValue[1].trim()));
                    }
                }
                cookies = cookieList.toArray(new Cookie[0]);
            }
        }
        this.cookies = cookies;
        this.contentLength = contentLength;

        String[] extPts = path.split("\\.");
        if (extPts.length > 1) {
            context = extPts[1];
        } else {
            context = "html";
        }
    }
    /**
     * Constructs a new PostRequest with the given header and body.
     * @param header the header of the POST request
     * @param body the body of the POST request
     */
    public PostRequest(PostHeader header, String body) {
        this.path = header.getPath();
        this.body = body != null ? body : "";
        this.contentLength = header.getContentLength();
        this.cookies = header.getCookies();
        
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
     * Returns the context of the POST request.
     * This is derived from the path, typically indicating the type of resource (e.g., "html", "json").
     * @return the context of the POST request
     */
    public String getContext() {
        return context;
    }
    /**
     * Returns the content length of the POST request.
     * This is used to determine the size of the request body.
     * @return the content length of the POST request
     */
    public int getContentLength() {
        return contentLength;
    }

    public Map<String, String> getFormData() {
        Map<String, String> params = new HashMap<>();
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
        return params;
    }

    public String getBodyAsString() {
        return body;
    }
    public Map<String, Object> getJson() {
        // Parse body as JSON
        Gson gson = new Gson();
        java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> json = gson.fromJson(body, mapType);
        return json;
    }
    public Cookie[] getCookies() {
        return cookies;
    }

    /**
     * Converts the path of the POST request to a ResourceLocation.
     * This method is used to create a ResourceLocation object from the path,
     * which can be used for further processing or resource management.
     * @return a ResourceLocation object representing the path of the POST request
     */
    public ResourceLocation toResourceLocation(String username) {
        return de.igslandstuhl.database.server.webserver.WebResourceHandler.locationFromPath(path, User.getUser(username));
    }
}