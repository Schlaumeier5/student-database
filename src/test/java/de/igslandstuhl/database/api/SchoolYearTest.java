package de.igslandstuhl.database.api;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SchoolYearTest {

    @Before
    public void setupDatabase() throws SQLException {
        PreConditions.setupDatabase();
    }

    @Test
    public void addAndGetSchoolYear() throws SQLException {
        SchoolYear year = SchoolYear.addSchoolYear("2024/2025", 40, 1);
        assertNotNull(year);
        assertEquals("2024/2025", year.getLabel());
        assertEquals(40, year.getWeekCount());
        assertEquals(1, year.getCurrentWeek());

        SchoolYear loaded = SchoolYear.get(year.getId());
        assertNotNull(loaded);
        assertEquals(year.getLabel(), loaded.getLabel());
    }

    @Test
    public void getAllSchoolYears() throws SQLException {
        SchoolYear.addSchoolYear("2023/2024", 39, 39);
        SchoolYear.addSchoolYear("2024/2025", 40, 1);
        List<SchoolYear> years = SchoolYear.getAll();
        assertTrue(years.size() >= 2);
    }

    @Test
    public void updateCurrentWeek() throws SQLException {
        SchoolYear year = SchoolYear.addSchoolYear("2025/2026", 41, 1);
        year.setCurrentWeek(10);
        SchoolYear loaded = SchoolYear.get(year.getId());
        assertEquals(10, loaded.getCurrentWeek());
    }

    @Test
    public void getCurrentYear() throws SQLException {
        SchoolYear.addSchoolYear("2022/2023", 38, 38);
        SchoolYear.addSchoolYear("2023/2024", 39, 39);
        SchoolYear.addSchoolYear("2024/2025", 40, 10);
        SchoolYear current = SchoolYear.getCurrentYear();
        assertNotNull(current);
        // Should be the year with the lowest current_week
        assertEquals(10, current.getCurrentWeek());
    }
}