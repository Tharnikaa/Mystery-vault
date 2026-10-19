package com.mysteryvault.config;

import com.mysteryvault.exception.DatabaseConfigurationException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Reusable database connection factory and schema migration runner.
 * Used by all JDBC repositories across Modules 1, 2, and 3.
 */
public class DatabaseConnectionFactory {

    private final DatabaseConfig config;

    public DatabaseConnectionFactory() {
        this(DatabaseConfig.fromEnvironment());
    }

    public DatabaseConnectionFactory(DatabaseConfig config) {
        this.config = Objects.requireNonNull(config, "DatabaseConfig cannot be null");
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    /**
     * Obtains a new database connection from the underlying configuration.
     */
    public Connection getConnection() throws SQLException {
        return config.getConnection();
    }

    /**
     * Verifies connectivity using SELECT 1.
     */
    public boolean checkConnectivity() {
        return config.checkConnectivity();
    }

    /**
     * Initializes the database schema using the bundled schema.sql script.
     * Idempotent (uses IF NOT EXISTS clauses).
     */
    public void initializeSchema() {
        String schemaSql = loadSchemaScript();
        executeSqlScript(schemaSql);
    }

    /**
     * Drops all tables in reverse dependency order (strictly for clean test fixtures).
     */
    public void dropSchema() {
        String dropSql = "DROP TABLE IF EXISTS clue_prerequisites CASCADE;\n" +
                "DROP TABLE IF EXISTS case_clues CASCADE;\n" +
                "DROP TABLE IF EXISTS clues CASCADE;\n" +
                "DROP TABLE IF EXISTS case_evidence CASCADE;\n" +
                "DROP TABLE IF EXISTS evidence_archive CASCADE;\n" +
                "DROP TABLE IF EXISTS evidence CASCADE;\n" +
                "DROP TABLE IF EXISTS case_suspects CASCADE;\n" +
                "DROP TABLE IF EXISTS suspects CASCADE;\n" +
                "DROP TABLE IF EXISTS cases CASCADE;\n";
        executeSqlScript(dropSql);
    }

    private void executeSqlScript(String script) {
        if (script == null || script.trim().isEmpty()) {
            return;
        }
        // Strip single-line comments (-- ...)
        String cleanScript = script.replaceAll("(?m)^\\s*--.*$", "");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String[] statements = cleanScript.split(";");
            for (String raw : statements) {
                String trimmed = raw.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConfigurationException("Failed executing database schema script: " + e.getMessage(),
                    config.getUrl(), e);
        }
    }

    private String loadSchemaScript() {
        try (InputStream is = getClass().getResourceAsStream("/schema.sql")) {
            if (is == null) {
                return getFallbackSchemaSql();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return getFallbackSchemaSql();
        }
    }

    private String getFallbackSchemaSql() {
        return "CREATE TABLE IF NOT EXISTS cases (\n" +
                "    id VARCHAR(64) PRIMARY KEY,\n" +
                "    title VARCHAR(255) NOT NULL,\n" +
                "    description TEXT,\n" +
                "    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',\n" +
                "    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',\n" +
                "    lead_investigator VARCHAR(255),\n" +
                "    created_at TIMESTAMP NOT NULL,\n" +
                "    updated_at TIMESTAMP NOT NULL\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS suspects (\n" +
                "    id VARCHAR(64) PRIMARY KEY,\n" +
                "    name VARCHAR(255) NOT NULL,\n" +
                "    role VARCHAR(64) NOT NULL DEFAULT 'Suspect',\n" +
                "    notes TEXT,\n" +
                "    occupation VARCHAR(255),\n" +
                "    age INT,\n" +
                "    alibi TEXT,\n" +
                "    suspicion_score INT,\n" +
                "    motive_description TEXT,\n" +
                "    alibi_verified BOOLEAN DEFAULT FALSE,\n" +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS case_suspects (\n" +
                "    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,\n" +
                "    suspect_id VARCHAR(64) NOT NULL REFERENCES suspects(id) ON DELETE CASCADE,\n" +
                "    PRIMARY KEY (case_id, suspect_id)\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS evidence (\n" +
                "    id VARCHAR(64) PRIMARY KEY,\n" +
                "    name VARCHAR(255) NOT NULL,\n" +
                "    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,\n" +
                "    description TEXT,\n" +
                "    discovered_at TIMESTAMP,\n" +
                "    location_found VARCHAR(255),\n" +
                "    collected_by VARCHAR(255),\n" +
                "    analyzed BOOLEAN DEFAULT FALSE,\n" +
                "    evidence_type VARCHAR(32) NOT NULL,\n" +
                "    author VARCHAR(255),\n" +
                "    page_count INT DEFAULT 0,\n" +
                "    classified BOOLEAN DEFAULT FALSE,\n" +
                "    document_type VARCHAR(255),\n" +
                "    device_type VARCHAR(255),\n" +
                "    file_hash VARCHAR(255),\n" +
                "    ip_address VARCHAR(64),\n" +
                "    file_size_bytes BIGINT DEFAULT 0,\n" +
                "    discovered_in_session BOOLEAN DEFAULT FALSE\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS case_evidence (\n" +
                "    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,\n" +
                "    evidence_id VARCHAR(64) NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,\n" +
                "    PRIMARY KEY (case_id, evidence_id)\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS clues (\n" +
                "    id VARCHAR(64) PRIMARY KEY,\n" +
                "    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,\n" +
                "    title VARCHAR(255) NOT NULL,\n" +
                "    description TEXT,\n" +
                "    unlocked BOOLEAN DEFAULT FALSE,\n" +
                "    unlocked_at TIMESTAMP\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS case_clues (\n" +
                "    case_id VARCHAR(64) NOT NULL REFERENCES cases(id) ON DELETE CASCADE,\n" +
                "    clue_id VARCHAR(64) NOT NULL REFERENCES clues(id) ON DELETE CASCADE,\n" +
                "    PRIMARY KEY (case_id, clue_id)\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS clue_prerequisites (\n" +
                "    clue_id VARCHAR(64) NOT NULL REFERENCES clues(id) ON DELETE CASCADE,\n" +
                "    required_evidence_id VARCHAR(64) NOT NULL REFERENCES evidence(id) ON DELETE CASCADE,\n" +
                "    PRIMARY KEY (clue_id, required_evidence_id)\n" +
                ");\n" +
                "CREATE TABLE IF NOT EXISTS evidence_archive (\n" +
                "    archive_id VARCHAR(64) PRIMARY KEY,\n" +
                "    evidence_id VARCHAR(64) NOT NULL,\n" +
                "    case_id VARCHAR(64),\n" +
                "    name VARCHAR(255),\n" +
                "    summary TEXT,\n" +
                "    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
                ");\n";
    }
}
