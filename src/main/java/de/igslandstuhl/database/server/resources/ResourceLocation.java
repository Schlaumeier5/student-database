package de.igslandstuhl.database.server.resources;

/**
 * Represents a resource location with context, namespace, and resource name.
 * This class is used to identify resources in the application.
 */
public record ResourceLocation(String context, String namespace, String resource) {
    /**
     * Constructs a ResourceLocation with the specified context, namespace, and resource name.
     *
     * @param context   the context of the resource (e.g., "virtual", "main")
     * @param namespace the namespace of the resource (e.g., "default", "custom")
     * @param resource  the name of the resource (e.g., "config.json", "data.txt")
     */
    public static ResourceLocation get(String context, String resourceID) {
        String[] parts = resourceID.split(":");
        if (parts.length > 1) {
            return new ResourceLocation(context, parts[0], parts[1]);
        } else {
            return new ResourceLocation(context, "main", resourceID);
        }
    }
    /**
     * Constructs a ResourceLocation with the specified context and resource name.
     * The namespace is set to "main" by default.
     *
     * @param context  the context of the resource (e.g., "virtual", "main")
     * @param resource the name of the resource (e.g., "config.json", "data.txt")
     */
    public boolean isVirtual() {
        return context.equals("virtual");
    }
}
