package de.igslandstuhl.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.igslandstuhl.database.api.SerializationException;
import de.igslandstuhl.database.api.Subject;
import de.igslandstuhl.database.api.Topic;
import de.igslandstuhl.database.server.Server;

/**
 * Represents the main application class that serves as a singleton instance.
 * This class provides methods to check if the application is running on a server.
 * It will later replace Server as main class.
 */
public final class Application {
    public static final String TOPIC_DELIMITER = "\n";
    public static final String TITLE_DELIMITER = "¶";
    public static final String TASK_TITLE_DELIMITER = "\\|";
    public static final String TASK_DELIMITER = "¤";
    private static final Application instance = new Application();
    public static Application getInstance() {
        return instance;
    }

    private final boolean onServer = true;
    public boolean isOnServer() {
        return onServer;
    }

    public Topic[] readFile(String file) throws SerializationException, SQLException {
        Subject subject=null;
        int grade=-1;
        List<Topic> topics = new ArrayList<>();

        try {
            String[] lines = file.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i == 0) {
                    subject = Subject.get(line);
                } else if (i == 1) {
                    grade = Integer.parseInt(line);
                } else {
                    topics.add(Topic.fromSerialized(line, subject, grade, i - 1));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new SerializationException("Failed to read file", t);
        }

        Topic[] topicsArr = new Topic[topics.size()];
        return topics.toArray(topicsArr);
    }

    public static void main(String[] args) throws Exception {
        Server.getInstance().getConnection().createTables();
        Server.getInstance().getWebServer().start();

        while (true);
    }
}
