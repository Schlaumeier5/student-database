package de.igslandstuhl.database.api;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SchoolClassTest {
    @BeforeAll
    public static void setupServer() throws SQLException {
        PreConditions.setupDatabase();
    }
    @Test
    public void addClass() throws SQLException {
        SchoolClass added = SchoolClass.addClass("5a", 5);
        SchoolClass schoolClass = SchoolClass.get(1);
        assertNotNull(schoolClass);
        assertEquals(added, schoolClass);
    }
}