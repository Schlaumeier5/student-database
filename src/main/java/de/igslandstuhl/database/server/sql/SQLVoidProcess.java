package de.igslandstuhl.database.server.sql;

import java.sql.SQLException;
import java.sql.Statement;

@FunctionalInterface
public interface SQLVoidProcess {
    public void execute(Statement stmt) throws SQLException;
}
