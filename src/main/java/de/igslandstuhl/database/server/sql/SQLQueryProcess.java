package de.igslandstuhl.database.server.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLQueryProcess implements SQLProcess {
    private final String query;

    public SQLQueryProcess(String query) {
        this.query = query;
    }

    @Override
    public ResultSet execute(Statement stmt) throws SQLException {
        return stmt.executeQuery(query);
    }
}
