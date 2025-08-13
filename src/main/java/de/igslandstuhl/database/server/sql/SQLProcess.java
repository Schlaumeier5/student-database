package de.igslandstuhl.database.server.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a SQL process that can be executed using a Statement.
 * This interface is designed to encapsulate SQL operations that return a ResultSet.
 */
@FunctionalInterface
public interface SQLProcess {
    /**
     * Executes the SQL process using the given Statement.
     * @param stmt the Statement to use for execution
     * @return a ResultSet containing the results of the query
     * @throws SQLException if an SQL error occurs during execution
     */
    public ResultSet execute(PreparedStatementSupplier supplier) throws SQLException;
}
