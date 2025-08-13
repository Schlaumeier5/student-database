package de.igslandstuhl.database.server.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLQueryProcess implements SQLProcess {
    private final String query;
    private final String[] params;

    public SQLQueryProcess(String query, String[] params) {
        this.query = query;
        this.params = params;
    }

    @Override
    public ResultSet execute(PreparedStatementSupplier stmt) throws SQLException {
        PreparedStatement s = stmt.prepareStatement(query);
        SQLHelper.insertArgs(s, params);
        return s.executeQuery();
    }
}
