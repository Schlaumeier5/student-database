package de.igslandstuhl.database.server.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.resources.ResourceHelper;
import de.igslandstuhl.database.server.resources.ResourceLocation;

public class SQLHelper {
    public static final String CONTEXT = "sql";
    public static final String QUERIES = "queries";
    public static final String PUSHES = "pushes";
    public static String getSQLQuery(String queryName, String... args) {
        ResourceLocation location = new ResourceLocation(CONTEXT, QUERIES, queryName + ".sql");
        String query = ResourceHelper.readResourceCompletely(location);
        for (int i = 0; i < args.length; i++) {
            query = Pattern.compile("\\{" + i + "\\}").matcher(query).replaceAll(args[i]);
        }
        return query;
    }
    public static PreparedStatement prepareSQLQuery(String queryName, String... args) throws SQLException {
        String query = getSQLQuery(queryName, args);
        return Server.getInstance().getConnection().prepareStatement(query);
    }
    public static SQLProcess getQueryProcess(String queryName, String... args) {
        String query = getSQLQuery(queryName, args);
        return (stmt) -> stmt.executeQuery(query);
    }

    public static String getSQLAddStatement(String object, String... args) {
        ResourceLocation location = new ResourceLocation(CONTEXT, PUSHES, "add_" + object + ".sql");
        String statement = ResourceHelper.readResourceCompletely(location);
        for (int i = 0; i < args.length; i++) {
            statement = Pattern.compile("\\{" + i + "\\}").matcher(statement).replaceAll(args[i]);
        }
        return statement;
    }
    public static SQLVoidProcess getAddObjectProcess(String object, String... args) {
        return (stmt) -> stmt.executeUpdate(getSQLAddStatement(object, args));
    }
}
