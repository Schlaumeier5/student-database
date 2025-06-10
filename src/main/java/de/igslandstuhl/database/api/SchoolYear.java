package de.igslandstuhl.database.api;

import java.sql.SQLException;
import java.util.*;
import de.igslandstuhl.database.server.Server;
import de.igslandstuhl.database.server.sql.SQLHelper;

public class SchoolYear {
    private static final String[] SQL_FIELDS = {"id", "label", "week_count", "current_week"};
    private static final Map<Integer, SchoolYear> years = new HashMap<>();

    private final int id;
    private final String label;
    private final int weekCount;
    private int currentWeek;

    public SchoolYear(int id, String label, int weekCount, int currentWeek) {
        this.id = id;
        this.label = label;
        this.weekCount = weekCount;
        this.currentWeek = currentWeek;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
    public int getWeekCount() { return weekCount; }
    public int getCurrentWeek() { return currentWeek; }
    public void setCurrentWeek(int week) throws SQLException {
        this.currentWeek = week;
        Server.getInstance().getConnection().executeVoidProcessSecure(
            "UPDATE school_years SET current_week = " + week + " WHERE id = " + id
        );
    }

    public static SchoolYear fromSQL(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        String label = fields[1];
        int weekCount = Integer.parseInt(fields[2]);
        int currentWeek = Integer.parseInt(fields[3]);
        return new SchoolYear(id, label, weekCount, currentWeek);
    }

    public static SchoolYear get(int id) {
        if (years.containsKey(id)) return years.get(id);
        try {
            SchoolYear year = Server.getInstance().processSingleRequest(SchoolYear::fromSQL, "get_school_year_by_id", SQL_FIELDS, String.valueOf(id));
            if (year != null) years.put(id, year);
            return year;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SchoolYear> getAll() {
        List<SchoolYear> all = new ArrayList<>();
        try {
            Server.getInstance().processRequest(
                fields -> {
                    SchoolYear year = SchoolYear.fromSQL(fields);
                    years.put(year.getId(), year);
                    all.add(year);
                },
                "get_all_school_years", SQL_FIELDS
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return all;
    }

    public static SchoolYear addSchoolYear(String label, int weekCount, int currentWeek) throws SQLException {
        Server.getInstance().getConnection().executeVoidProcessSecure(
            SQLHelper.getAddObjectProcess(
                "school_year",
                label,
                String.valueOf(weekCount),
                String.valueOf(currentWeek)
            )
        );
        // Fetch the newly created year
        return Server.getInstance().processSingleRequest(SchoolYear::fromSQL, "get_school_year_by_label", SQL_FIELDS, label);
    }

    public static SchoolYear getCurrentYear() {
        try {
            SchoolYear year = Server.getInstance().processSingleRequest(
                SchoolYear::fromSQL,
                "get_current_school_year",
                SQL_FIELDS
            );
            if (year != null) years.put(year.getId(), year);
            return year;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{\"id\":" + id + ",\"label\":\"" + label + "\",\"weekCount\":" + weekCount + ",\"currentWeek\":" + currentWeek + "}";
    }
}