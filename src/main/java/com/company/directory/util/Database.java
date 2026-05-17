package com.company.directory.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Owns one HikariCP connection pool for the whole app. Read DB settings from
 * environment variables (set by Docker), falling back to local defaults.
 * Call {@link #getConnection()} to borrow a connection; always close it
 * (use try-with-resources) to return it to the pool.
 */
public final class Database {

    private static final HikariDataSource DATA_SOURCE = build();

    private Database() { }

    private static HikariDataSource build() {
        HikariConfig config = new HikariConfig();
        // Name the driver explicitly so HikariCP loads it with the web app's
        // classloader — relying on DriverManager auto-registration fails in a WAR.
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(env("DB_URL", "jdbc:mysql://localhost:3307/directory"));
        config.setUsername(env("DB_USER", "directory"));
        config.setPassword(env("DB_PASSWORD", "directory"));
        config.setMaximumPoolSize(10);
        config.setPoolName("directory-pool");
        // MySQL can be slow to accept connections right after the container starts.
        config.setInitializationFailTimeout(60_000);
        return new HikariDataSource(config);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
}
