package de.igslandstuhl.database.api;

import org.junit.Before;
import org.junit.Test;

public class AdminTest {
    @Before
    public void createDatabase() throws Exception {
        PreConditions.setupDatabase();
    }
    @Test
    public void testCreateAdmin() throws Exception {
        Admin admin = Admin.create("adminUser", "adminPass");
        assert admin != null;
        assert "adminUser".equals(admin.getUsername());
        assert admin.isAdmin();
        assert !admin.isTeacher();
        assert !admin.isStudent();
        Admin fetchedAdmin = Admin.get("adminUser");
        assert fetchedAdmin != null;
        assert "adminUser".equals(fetchedAdmin.getUsername());
        assert fetchedAdmin.isAdmin();
        assert !fetchedAdmin.isTeacher();
        assert !fetchedAdmin.isStudent();
        assert fetchedAdmin.equals(admin);
    }
}