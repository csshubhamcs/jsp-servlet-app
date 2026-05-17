package com.company.directory.listener;

import com.company.directory.dao.AppUserDao;
import com.company.directory.model.AppUser;
import com.company.directory.model.Role;
import com.company.directory.service.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Runs once when the web app starts. If no Super Admin exists yet, it creates
 * one. Credentials come from the APP_SUPERADMIN_USERNAME / APP_SUPERADMIN_PASSWORD
 * environment variables, defaulting to superadmin / ChangeMe123!.
 */
@WebListener
public class BootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Read credentials from environment variables, falling back to safe defaults.
        // In production, always set APP_SUPERADMIN_USERNAME and APP_SUPERADMIN_PASSWORD.
        String username = env("APP_SUPERADMIN_USERNAME", "superadmin");
        String password = env("APP_SUPERADMIN_PASSWORD", "ChangeMe123!");

        AppUserDao dao = new AppUserDao();
        if (dao.findByUsername(username) != null) {
            // Super Admin already exists — nothing to seed (safe to run multiple times).
            event.getServletContext().log("Super Admin '" + username + "' already exists.");
            return;
        }

        // Create the first Super Admin account. mustChangePassword=true forces
        // them to set their own password on first login.
        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setFullName("Super Admin");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setPasswordHash(PasswordUtil.hash(password));
        admin.setActive(true);
        admin.setMustChangePassword(true);
        admin.setProfileCompleted(false);
        admin.setProfileLocked(false);
        dao.insert(admin);
        event.getServletContext().log("Seeded Super Admin '" + username + "'.");
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
