package de.igslandstuhl.database.server.webserver;

import java.util.HashMap;
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
public class PostRequest implements HttpRequest {
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

    private final String ipAddress;
    private final String userAgent;
    private final String acceptLanguage;
    private final boolean secureConnection;

    /**
     * Constructs a new PostRequest with the given header and body.
     * @param header the header of the POST request
     * @param body the body of the POST request
     */
    public PostRequest(String header, String body, String ipAddress, boolean secureConnection) {
        this(new HttpHeader(header), body, ipAddress, secureConnection);
    }
    /**
     * Constructs a new PostRequest with the given header and body.
     * @param header the header of the POST request
     * @param body the body of the POST request
     */
    public PostRequest(HttpHeader header, String body, String ipAddress, boolean secureConnection) {
        this.path = header.getPath();
        this.body = body != null ? body : "";
        this.contentLength = header.getContentLength();
        this.cookies = header.getCookies();
        this.ipAddress = ipAddress;
        this.userAgent = header.getUserAgent();
        this.acceptLanguage = header.getAcceptLanguage();
        this.secureConnection = secureConnection;
        
        String[] extPts = path.split("\\.");
        if (extPts.length > 1) {
            context = extPts[1];
        } else {
            context = "html";
        }
    }

    @Override
    public String getPath() {
        return path;
    }
    @Override
    public String getContext() {
        return context;
    }
    @Override
    public int getContentLength() {
        return contentLength;
    }
    @Override
    public Cookie[] getCookies() {
        return cookies;
    }
    @Override
    public String getIP() {
        return ipAddress;
    }
    @Override
    public String getUserAgent() {
        return userAgent;
    }
    @Override
    public String getAcceptLanguage() {
        return acceptLanguage;
    }
    @Override
    public boolean isSecureConnection() {
        return secureConnection;
    }

    public Map<String, String> getFormData() {
        if (body == null || body.isEmpty()) return Map.of();
        else if (!Character.isLetter(body.charAt(0))) throw new IllegalArgumentException("No Form Data");
        Map<String, String> params = new HashMap<>();
        // Parse body as form data: key1=value1&key2=value2
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            } else {
                throw new IllegalArgumentException("No form data");
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