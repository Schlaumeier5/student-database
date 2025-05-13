package de.igslandstuhl.database.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.codec.digest.DigestUtils;

import de.igslandstuhl.database.api.Room;
import de.igslandstuhl.database.api.Student;
import de.igslandstuhl.database.api.User;
import de.igslandstuhl.database.server.sql.SQLHelper;
import de.igslandstuhl.database.server.sql.SQLiteConnection;

public final class Server implements AutoCloseable {
    private static final Server instance = new Server();
    public static Server getInstance() {
        return instance;
    }
    private static final String SQL_URL = "test-server";
    private final SQLiteConnection connection;
    public SQLiteConnection getConnection() {
        return connection;
    }
    private final WebServer webServer;
    public WebServer getWebServer() {
        return webServer;
    }

    private Server() {
        try {
            connection = new SQLiteConnection(SQL_URL);
            String keystorePath = "keys/web/keystore.jks";
            String keystorePassword = "changeit";
            int port = 443;
            webServer = new WebServer(port, keystorePath, keystorePassword);
        } catch (Exception e) {
            throw new IllegalStateException("Server failed on start", e);
        }
    }

    public <T> T processSingleRequest(Function<String[],T> output, String request, String[] sqlFields, String... args) throws SQLException {
        synchronized (connection) {
            ResultSet result = connection.executeProcess(SQLHelper.getQueryProcess(request, args));
            if (result.next()){
                List<String> results = new ArrayList<>();
                for (String columnLabel : sqlFields) {
                    results.add(result.getString(columnLabel));
                }
                String[] resultArr = new String[results.size()];
                connection.closeAllPendingStatements();
                return output.apply(results.toArray(resultArr));
            } else {
                connection.closeAllPendingStatements();
                return null;
            }
        }
    }
    public void processRequest(Consumer<String[]> callback, String request, String[] sqlFields, String... args) throws SQLException {
        synchronized (connection) {
            ResultSet result = connection.executeProcess(SQLHelper.getQueryProcess(request, args));
            while (result.next()) {
                List<String> results = new ArrayList<>();
                for (String columnLabel : sqlFields) {
                    results.add(result.getString(columnLabel));
                }
                String[] resultArr = new String[results.size()];
                callback.accept(results.toArray(resultArr));
            }
            connection.closeAllPendingStatements();
        }
    }

    public static void main(String[] args) throws Exception {
        try (instance) {
            getInstance().getConnection().createTables();
            getInstance().getWebServer().start();
            while (true);
        }
    }

    public boolean isValidUser(String username, String password) {
        User user = User.getUser(username);
        if (user != null && user.getPasswordHash().equals(DigestUtils.sha1Hex(password))) {
            return true;
        } else {
            return false;
        }
    }

    public String getSQLResource(String username, String resource) {
        if (resource.equals("/mydata")) {
            User user = User.getUser(username);
            return user.toJSON();
        } else if (resource.equals("/rooms")) {
            return new HashSet<>(Room.getRooms().values()).toString();
        } else if (resource.equals("/mysubjects")) {
            User user = User.getUser(username);
            if (user instanceof Student) {
                Student student = (Student) user;
                // TODO
                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
        webServer.stop();
    }
}
