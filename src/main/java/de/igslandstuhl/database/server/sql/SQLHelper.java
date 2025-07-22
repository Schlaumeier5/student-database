package de.igslandstuhl.database.server.sql;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.resources.ResourceHelper;
import de.igslandstuhl.database.server.resources.ResourceLocation;

/**
 * Helper class for SQL operations.
 */
public class SQLHelper {
    /**
     * Context for SQL resources.
     * This is used to identify the context of SQL queries and pushes.
     */
    public static final String CONTEXT = "sql";
    /**
     * Subdirectory for SQL queries.
     * This is used to organize SQL query files.
     */
    public static final String QUERIES = "queries";
    /**
     * Subdirectory for SQL pushes.
     * This is used to organize SQL push files.
     */
    public static final String PUSHES = "pushes";
    /**
     * Gets an SQL query by its name and replaces placeholders with provided arguments.
     *
     * @param queryName the name of the SQL query file (without extension)
     * @param args      the arguments to replace in the query
     * @return the SQL query as a String with placeholders replaced
     */
    public static String getSQLQuery(String queryName, String... args) {
        ResourceLocation location = new ResourceLocation(CONTEXT, QUERIES, queryName + ".sql");
        String query;
        try {
            query = ResourceHelper.readResourceCompletely(location);
        } catch (FileNotFoundException e) {
            throw new SQLCommandNotFoundException(queryName, e);
        }
        for (int i = 0; i < args.length; i++) {
            query = Pattern.compile("\\{" + i + "\\}").matcher(query).replaceAll(args[i]);
        }
        return query;
    }
    /**
     * Prepares an SQL query statement by its name and replaces placeholders with provided arguments.
     *
     * @param queryName the name of the SQL query file (without extension)
     * @param args      the arguments to replace in the query
     * @return a PreparedStatement for the SQL query
     * @throws SQLException if an error occurs while preparing the statement
     */
    public static PreparedStatement prepareSQLQuery(String queryName, String... args) throws SQLException {
        String query = getSQLQuery(queryName, args);
        return Server.getInstance().getConnection().prepareStatement(query);
    }
    /**
     * Gets an SQL query process by its name and replaces placeholders with provided arguments.
     *
     * @param queryName the name of the SQL query file (without extension)
     * @param args      the arguments to replace in the query
     * @return a SQLProcess that executes the query
     */
    public static SQLProcess getQueryProcess(String queryName, String... args) {
        String query = getSQLQuery(queryName, args);
        return (stmt) -> stmt.executeQuery(query);
    }

    /**
     * Gets an SQL add statement for a specific object and replaces placeholders with provided arguments.
     *
     * @param object the name of the object to add (e.g., "student", "course")
     * @param args   the arguments to replace in the SQL statement
     * @return the SQL add statement as a String with placeholders replaced
     */
    public static String getSQLAddStatement(String object, String... args) {
        ResourceLocation location = new ResourceLocation(CONTEXT, PUSHES, "add_" + object + ".sql");
        String statement;
        try {
            statement = ResourceHelper.readResourceCompletely(location);
        } catch (FileNotFoundException e) {
            throw new SQLCommandNotFoundException("add_" + object, e);
        }
        for (int i = 0; i < args.length; i++) {
            statement = Pattern.compile("\\{" + i + "\\}").matcher(statement).replaceAll(args[i]);
        }
        return statement;
    }
    /**
     * Gets an SQL process for adding an object to the database.
     *
     * @param object the name of the object to add (e.g., "student", "course")
     * @param args   the arguments to replace in the SQL statement
     * @return a SQLVoidProcess that executes the add statement
     */
    public static SQLVoidProcess getAddObjectProcess(String object, String... args) {
        return (stmt) -> stmt.executeUpdate(getSQLAddStatement(object, args));
    }
}
