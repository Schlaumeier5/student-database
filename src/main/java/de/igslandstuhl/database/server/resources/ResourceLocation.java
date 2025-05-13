package de.igslandstuhl.database.server.resources;

public record ResourceLocation(String context, String namespace, String resource) {
    public static ResourceLocation get(String context, String resourceID) {
        String[] parts = resourceID.split(":");
        if (parts.length > 1) {
            return new ResourceLocation(context, parts[0], parts[1]);
        } else {
            return new ResourceLocation(context, "main", resourceID);
        }
    }
    public boolean isVirtual() {
        return context.equals("virtual");
    }
}
