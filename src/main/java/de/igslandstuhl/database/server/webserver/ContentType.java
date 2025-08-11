package de.igslandstuhl.database.server.webserver;

import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * Represents the different content types that can be served by the web server.
 */
public enum ContentType {
    TEXT_PLAIN("text/plain"),
    HTML ("text/html"),
    JAVASCRIPT ("text/javascript"),
    CSS ("text/css"),
    PNG ("image/png"),
    JSON ("text/json")
    ;
    /**
     * The name of the content type, used in HTTP headers.
     */
    private final String name;
    /**
     * Constructs a ContentType with the specified name.
     * @param name the name of the content type
     */
    private ContentType(String name) {
        this.name = name;
    }
    /**
     * Returns the name of the content type.
     * @return the name of the content type
     */
    public String getName() {
        return name;
    }
    public boolean isText() {
        switch (this) {
            case TEXT_PLAIN:
            case HTML:
            case JAVASCRIPT:
            case CSS:
            case JSON:
                return true;
            default:
                return false;
        }
    }
    /**
     * Returns the ContentType corresponding to the given ResourceLocation.
     * @param l the ResourceLocation to determine the content type for
     * @return the ContentType corresponding to the ResourceLocation
     * @throws NoWebResourceException if the ResourceLocation does not correspond to a known web resource
     */
    public static ContentType ofResourceLocation(ResourceLocation l) throws NoWebResourceException {
        if (l.context().equals("html")) {
            return HTML;
        } else if (l.context().equals("js")) {
            return JAVASCRIPT;
        } else if (l.context().equals("css")) {
            return CSS;
        } else if (l.context().equals("imgs")) {
            if (l.resource().endsWith(".png") || l.resource().endsWith(".ico")) {
                return PNG;
            } else {
                throw new UnsupportedOperationException("Not supported");
            }
        } else if (l.context().equals("virtual")) {
            return JSON;
        } else {
            throw new NoWebResourceException(l);
        }
    }
}
