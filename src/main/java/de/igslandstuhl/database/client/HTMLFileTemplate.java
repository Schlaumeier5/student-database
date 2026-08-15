package de.igslandstuhl.database.client;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.resources.ResourceLocation;

public class HTMLFileTemplate implements HTMLTemplate {
    private static String sanitizeTemplateName(String name) {
        Path base = Paths.get("templates/html");
        Path resolved = base.resolve(name + ".html").normalize();

        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("Invalid template name");
        }

        return resolved.getFileName().toString();
    }
    private final ResourceLocation file;

    public HTMLFileTemplate(ResourceLocation file) throws FileNotFoundException {
        Server.getInstance().getResourceManager().readResourceCompletely(file);
        this.file = file;
    }
    public HTMLFileTemplate(String file) throws FileNotFoundException {
        this(new ResourceLocation("templates", "html", sanitizeTemplateName(file)));
    }
    @Override
    public String fill(Map<String, String> args) {
        final String templateString;

        try {
            templateString = Server.getInstance()
                .getResourceManager()
                .readResourceCompletely(file);
        } catch (FileNotFoundException e) {
            HTMLTemplate.LOGGER.error(
                "Failed to reload html template '{}'",
                file,
                e
            );
            return "";
        }

        if (templateString == null || templateString.isEmpty()) return "";

        Pattern p = Pattern.compile("%\\{([^}]+)\\}");
        Matcher m = p.matcher(templateString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String val = args.getOrDefault(key, "");
            // escape backslashes and dollars for regex replacement
            val = val.replace("\\", "\\\\").replace("$", "\\$");
            m.appendReplacement(sb, val);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
