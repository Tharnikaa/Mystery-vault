package com.mysteryvault.config;

import com.mysteryvault.exception.DatabaseConfigurationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Database configuration manager for Mystery Vault.
 * Reads JDBC connection settings from environment variables:
 * - MYSTERYVAULT_DB_URL (default: jdbc:postgresql://localhost:5432/mysteryvault)
 * - MYSTERYVAULT_DB_USERNAME (default: postgres)
 * - MYSTERYVAULT_DB_PASSWORD (default: "")
 *
 * Provides connection creation, credentials masking, and SELECT 1 connectivity verification.
 */
public class DatabaseConfig {

    public static final String ENV_DB_URL = "MYSTERYVAULT_DB_URL";
    public static final String ENV_DB_USERNAME = "MYSTERYVAULT_DB_USERNAME";
    public static final String ENV_DB_PASSWORD = "MYSTERYVAULT_DB_PASSWORD";

    public static final String DEFAULT_POSTGRES_URL = "jdbc:postgresql://localhost:5432/mysteryvault";
    public static final String DEFAULT_POSTGRES_USER = "postgres";

    private final String url;
    private final String username;
    private final String password;

    public DatabaseConfig(String url, String username, String password) {
        if (url == null || url.trim().isEmpty()) {
            throw new DatabaseConfigurationException("Database JDBC URL must not be null or blank.");
        }
        this.url = url.trim();
        this.username = username != null ? username.trim() : "";
        this.password = password != null ? password : "";
    }

    /**
     * Factory method creating DatabaseConfig from environment variables.
     * Falls back to standard defaults if environment variables are not set.
     */
    public static DatabaseConfig fromEnvironment() {
        String url = System.getenv(ENV_DB_URL);
        if (url == null || url.trim().isEmpty()) {
            url = System.getProperty(ENV_DB_URL, DEFAULT_POSTGRES_URL);
        }

        String user = System.getenv(ENV_DB_USERNAME);
        if (user == null || user.trim().isEmpty()) {
            user = System.getProperty(ENV_DB_USERNAME, DEFAULT_POSTGRES_USER);
        }

        String pass = System.getenv(ENV_DB_PASSWORD);
        if (pass == null) {
            pass = System.getProperty(ENV_DB_PASSWORD, "");
        }

        return new DatabaseConfig(url, user, pass);
    }

    /**
     * Factory method for in-memory H2 database (useful for isolated integration tests).
     */
    public static DatabaseConfig forH2InMemory(String dbName) {
        String h2Url = String.format("jdbc:h2:mem:%s;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                (dbName != null && !dbName.trim().isEmpty()) ? dbName.trim() : "mysteryvault_test");
        return new DatabaseConfig(h2Url, "sa", "");
    }

    /**
     * Obtains a new database connection using the configured credentials.
     */
    public Connection getConnection() throws SQLException {
        ensureDriverLoaded();
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Executes SELECT 1 to verify database connectivity.
     *
     * @return true if connectivity is verified
     * @throws DatabaseConfigurationException if connection fails
     */
    public boolean checkConnectivity() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            throw new DatabaseConfigurationException(
                    "Database connectivity check failed (SELECT 1). Verify that the database server is running and accessible.",
                    url, e);
        }
    }

    /**
     * Proactively checks connectivity without throwing an exception.
     */
    public boolean isAvailable() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void ensureDriverLoaded() {
        try {
            if (url.startsWith("jdbc:postgresql:")) {
                Class.forName("org.postgresql.Driver");
            } else if (url.startsWith("jdbc:h2:")) {
                Class.forName("org.h2.Driver");
            }
        } catch (ClassNotFoundException e) {
            throw new DatabaseConfigurationException("JDBC Driver class not found for URL: " + maskUrl(url), url, e);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getMaskedUrl() {
        return maskUrl(url);
    }

    public String getUsername() {
        return username;
    }

    public String getMaskedPassword() {
        return (password == null || password.isEmpty()) ? "(none)" : "*****";
    }

    public static String maskUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll("(?i)password=[^&;]*", "password=*****");
    }

    @Override
    public String toString() {
        return String.format("DatabaseConfig{url='%s', user='%s'}", getMaskedUrl(), username);
    }
}
