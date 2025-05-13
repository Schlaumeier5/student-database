package de.igslandstuhl.database.server.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@FunctionalInterface
public interface SQLProcess {
    public ResultSet execute(Statement stmt) throws SQLException;
}
