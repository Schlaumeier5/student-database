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

/**
 * Represents a connection to an SQLite database.
 */
public class SQLiteConnection implements AutoCloseable {
    /**
     * The URL of the SQLite database.
     * It is constructed as "jdbc:sqlite:" + url + ".db".
     */
    private final String url;
    /**
     * The SQL connection object.
     */
    private final Connection conn;
    /**
     * Returns the <code>java.sql.Connection</code> associated with this <code>SQLiteConnection</code>.
     * @return the <code>Connection</code> object
     */
    public Connection getSQLConnection() {
        return conn;
    }
    /**
     * A list of pending statements that need to be closed when the connection is closed.
     */
    private final List<Statement> pendingStatements = new ArrayList<>();
    /**
     * Creates the necessary tables in the database by executing SQL scripts.
     * This method reads SQL files matching the pattern "./tables/*.sql" (regex: .*tables.+\\.sql) and executes their content.
     * @param stmt the Statement object used to execute the SQL commands
     * @throws SQLException if an SQL error occurs during table creation
     */
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
    /**
     * Prepares a SQL statement for execution.
     * @param sql the SQL query to prepare
     * @return a PreparedStatement object
     * @throws SQLException if an SQL error occurs during preparation
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
    /**
     * Executes a SQL statement securely, ensuring that the statement is closed after execution.
     * @param statement the PreparedStatement to execute
     * @return a ResultSet containing the results of the query
     * @throws SQLException if an SQL error occurs during execution
     */
    public ResultSet executeStatementSecure(PreparedStatement statement) throws SQLException {
        try (statement) {
             return statement.executeQuery();
        }
    }
    /**
     * Executes a SQL statement that does not return a result set.
     * @param sql the SQL command to execute
     * @throws SQLException if an SQL error occurs during execution
     */
    public void executeVoidProcessSecure(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    /**
     * Executes a SQL void process securely, ensuring that the statement is closed after execution.
     * @param p the SQLVoidProcess to execute
     * @throws SQLException if an SQL error occurs during execution
     */
    public void executeVoidProcessSecure(SQLVoidProcess p) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            p.execute(stmt);
        }
    }
    /**
     * Executes a SQL process that returns a ResultSet.
     * @param p the SQLProcess to execute
     * @return a ResultSet containing the results of the query
     * @throws SQLException if an SQL error occurs during execution
     */
    public ResultSet executeProcess(SQLProcess p) throws SQLException {
        Statement stmt = conn.createStatement();
        pendingStatements.add(stmt);
        return p.execute(stmt);
    }
    /**
     * Closes all pending statements that have been created during the lifetime of this connection.
     * This method should be called before closing the connection to ensure that all resources are released.
     * @throws SQLException if an error occurs while closing the statements
     */
    public void closeAllPendingStatements() throws SQLException {
        for (Statement statement : pendingStatements) {
            statement.close();
        }
    }
    /**
     * Creates the necessary tables in the database by executing SQL scripts.
     * This method reads SQL files matching the pattern "./tables/*.sql" (regex: .*tables.+\\.sql) and executes their content.
     * @throws SQLException if an SQL error occurs during table creation
     */
    public void createTables() throws SQLException {
        executeVoidProcessSecure(this::createTables);
    }
    
    /**
     * Constructs a new SQLiteConnection with the specified database URL.
     * The URL is prefixed with "jdbc:sqlite:" and suffixed with ".db".
     * @param url the name of the database file (without extension)
     * @throws SQLException if an error occurs while establishing the connection
     */
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