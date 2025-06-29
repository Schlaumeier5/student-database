package de.igslandstuhl.database.server.webserver;

import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * Exception thrown when a requested resource is not a web resource.
 * This exception is used to indicate that the requested resource does not exist or is not accessible.
 */
public class NoWebResourceException extends Exception {
    /**
     * The location of the resource that was not found.
     */
    private final ResourceLocation resourceLocation;

    /**
     * Constructs a new NoWebResourceException with the specified resource location.
     * @param resourceLocation the location of the resource that was not found
     */
    public NoWebResourceException(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }
    
    /**
     * Returns the resource location associated with this exception.
     * This method provides access to the resource location that caused the exception.
     * @return the resource location that was not found
     */
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }
}
