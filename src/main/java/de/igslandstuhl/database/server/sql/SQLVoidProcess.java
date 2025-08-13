package de.igslandstuhl.database.server.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a SQL process that does not return any result.
 * This interface is designed to encapsulate SQL operations that do not produce a ResultSet.
 */
@FunctionalInterface
public interface SQLVoidProcess {
    /**
     * Executes the SQL void process using the given Statement.
     * @param supplier the Statement to use for execution
     * @throws SQLException if an SQL error occurs during execution
     */
    public void execute(PreparedStatementSupplier supplier) throws SQLException;

    public static SQLVoidProcess update(String query, String[] args) {
        return (supplier) -> {
            PreparedStatement p = supplier.prepareStatement(query);
            SQLHelper.insertArgs(p, args);
            p.executeUpdate();
        };
    }
}
