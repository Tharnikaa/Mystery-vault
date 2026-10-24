package com.mysteryvault.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Thread-safe SQLite database manager for MysteryVault.
 * Ensures strict physical database isolation between cases and handles
 * catalog vs individual case database connections.
 */
public class CaseDatabaseManager {

    private static final String DEFAULT_DATA_DIR = "data";
    private static final String CATALOG_DB_NAME = "mysteryvault_catalog.db";

    private final File dataDir;
    private final File casesDir;
    private String activeCaseId;

    public CaseDatabaseManager() {
        this(new File(DEFAULT_DATA_DIR));
    }

    public CaseDatabaseManager(File dataDir) {
        this.dataDir = dataDir;
        this.casesDir = new File(dataDir, "cases");
        ensureDirectories();
    }

    private void ensureDirectories() {
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        if (!casesDir.exists()) {
            casesDir.mkdirs();
        }
    }

    public File getDataDir() {
        return dataDir;
    }

    public File getCasesDir() {
        return casesDir;
    }

    public synchronized void setActiveCaseId(String caseId) {
        this.activeCaseId = normalizeCaseId(caseId);
    }

    public synchronized String getActiveCaseId() {
        return activeCaseId;
    }

    public static String normalizeCaseId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return "CASE_101";
        }
        String clean = rawId.trim().toUpperCase().replace("-", "_");
        if (!clean.startsWith("CASE_")) {
            if (clean.matches("\\d+")) {
                clean = "CASE_" + clean;
            }
        }
        return clean;
    }

    /**
     * Gets a connection to the global catalog database.
     */
    public Connection getCatalogConnection() throws SQLException {
        ensureDirectories();
        File catalogFile = new File(dataDir, CATALOG_DB_NAME);
        String url = "jdbc:sqlite:" + catalogFile.getAbsolutePath();
        Connection conn = DriverManager.getConnection(url);
        enableForeignKeys(conn);
        return conn;
    }

    /**
     * Gets a connection to the SQLite database file for the specified case.
     */
    public Connection getCaseConnection(String caseId) throws SQLException {
        ensureDirectories();
        String normalized = normalizeCaseId(caseId);
        String filename = normalized.toLowerCase() + ".db";
        File caseDbFile = new File(casesDir, filename);
        String url = "jdbc:sqlite:" + caseDbFile.getAbsolutePath();
        Connection conn = DriverManager.getConnection(url);
        enableForeignKeys(conn);
        return conn;
    }

    /**
     * Gets a connection to the currently active case database.
     */
    public Connection getActiveCaseConnection() throws SQLException {
        if (activeCaseId == null) {
            throw new SQLException("No active case selected in CaseDatabaseManager");
        }
        return getCaseConnection(activeCaseId);
    }

    private void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    /**
     * Initializes and seeds the catalog database and all four original case databases.
     * Safe to call repeatedly: will not overwrite existing investigations.
     */
    public void initializeAndSeedAll() throws SQLException {
        ensureDirectories();

        // 1. Catalog database
        try (Connection catalogConn = getCatalogConnection()) {
            DatabaseInitializer.initCatalog(catalogConn);
            DatabasePopulator.seedCatalog(catalogConn);
        }

        // 2. The four case databases
        String[] caseIds = {"CASE_101", "CASE_102", "CASE_103", "CASE_104"};
        for (String cId : caseIds) {
            try (Connection caseConn = getCaseConnection(cId)) {
                DatabaseInitializer.initCaseDatabase(caseConn);
                DatabasePopulator.seedCaseDatabase(caseConn, cId);
            }
        }
    }
}
