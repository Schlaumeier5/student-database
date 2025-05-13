package de.igslandstuhl.database.server.webserver;

import de.igslandstuhl.database.server.resources.ResourceLocation;

public class NoWebResourceException extends Exception {
    private final ResourceLocation resourceLocation;

    public NoWebResourceException(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }
    
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }
}
