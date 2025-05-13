package de.igslandstuhl.database.server.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.igslandstuhl.database.server.resources.ResourceHelper;

public class SQLiteConnection implements AutoCloseable {
    private final String url;
    private final Connection conn;
    private final List<Statement> pendingStatements = new ArrayList<>();
    private void createTables(Statement stmt) throws SQLException {
        for (BufferedReader in : ResourceHelper.openResourcesAsReader(Pattern.compile(".*tables.+\\.sql"))) {
            try (in) {
                String request = ResourceHelper.readResourceCompletely(in);
                stmt.execute(request);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
    public ResultSet executeStatementSecure(PreparedStatement statement) throws SQLException {
        try (statement) {
             return statement.executeQuery();
        }
    }
    public void test() {

        // 2. SQL-Abfrage zum Einfügen
        String sql = "SELECT * FROM students";

        // 3. Verbindung zur Datenbank herstellen
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet result = pstmt.executeQuery();
            result.next();
            System.out.println(result.getString("first_name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void executeVoidProcessSecure(SQLVoidProcess p) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            p.execute(stmt);
        }
    }
    public ResultSet executeProcess(SQLProcess p) throws SQLException {
        Statement stmt = conn.createStatement();
        pendingStatements.add(stmt);
        return p.execute(stmt);
    }
    public void closeAllPendingStatements() throws SQLException {
        for (Statement statement : pendingStatements) {
            statement.close();
        }
    }
    public void createTables() throws SQLException {
        executeVoidProcessSecure(this::createTables);
    }
    
    public SQLiteConnection(String url) throws SQLException {
        this.url = "jdbc:sqlite:" + url + ".db";
        this.conn = DriverManager.getConnection(this.url);
    }
    @Override
    public void close() throws SQLException {
        closeAllPendingStatements();
        conn.close();
    }
    public static void main(String[] args) throws SQLException {
        String url = "lernjobs"; // Datenbank-Datei im Projektverzeichnis

        try (SQLiteConnection c = new SQLiteConnection(url)) {
            c.createTables();
        }

    }
}