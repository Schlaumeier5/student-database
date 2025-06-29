package de.igslandstuhl.database.server.sql;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Represents a SQL process that does not return any result.
 * This interface is designed to encapsulate SQL operations that do not produce a ResultSet.
 */
@FunctionalInterface
public interface SQLVoidProcess {
    /**
     * Executes the SQL void process using the given Statement.
     * @param stmt the Statement to use for execution
     * @throws SQLException if an SQL error occurs during execution
     */
    public void execute(Statement stmt) throws SQLException;
}
