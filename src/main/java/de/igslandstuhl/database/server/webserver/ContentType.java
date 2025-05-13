package de.igslandstuhl.database.server.webserver;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public enum ContentType {
    HTML ("text/html"),
    JAVASCRIPT ("text/javascript"),
    CSS ("text/css"),
    PNG ("image/png"),
    JSON ("text/json")
    ;
    private final String name;
    private ContentType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
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
