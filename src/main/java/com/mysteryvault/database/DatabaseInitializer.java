package com.mysteryvault.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/**
 * Programmatic schema initialization and migration for SQLite databases.
 * Uses schema_version tracking to ensure repeatability and prevent data loss on repeat startup.
 */
public class DatabaseInitializer {

    private static final int CURRENT_CATALOG_VERSION = 1;
    private static final int CURRENT_CASE_VERSION = 1;

    public static boolean initCatalog(Connection conn) throws SQLException {
        createCatalogSchemaVersionTable(conn);
        int version = getVersion(conn);
        if (version >= CURRENT_CATALOG_VERSION) {
            return false; // Already initialized
        }

        try (Statement stmt = conn.createStatement()) {
            // Cases catalog
            stmt.execute("CREATE TABLE IF NOT EXISTS cases_catalog (" +
                    "case_id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "difficulty TEXT NOT NULL, " +
                    "db_filename TEXT NOT NULL, " +
                    "is_unlocked INTEGER NOT NULL DEFAULT 1, " +
                    "is_completed INTEGER NOT NULL DEFAULT 0" +
                    ");");

            // Player profile
            stmt.execute("CREATE TABLE IF NOT EXISTS player_profile (" +
                    "profile_id TEXT PRIMARY KEY, " +
                    "detective_name TEXT NOT NULL, " +
                    "experience_points INTEGER NOT NULL DEFAULT 0, " +
                    "completed_cases INTEGER NOT NULL DEFAULT 0, " +
                    "correct_deductions INTEGER NOT NULL DEFAULT 0, " +
                    "total_deductions INTEGER NOT NULL DEFAULT 0, " +
                    "best_score INTEGER NOT NULL DEFAULT 0" +
                    ");");

            // Saved investigations
            stmt.execute("CREATE TABLE IF NOT EXISTS saved_investigations (" +
                    "investigation_id TEXT PRIMARY KEY, " +
                    "case_id TEXT NOT NULL REFERENCES cases_catalog(case_id), " +
                    "saved_at TEXT NOT NULL, " +
                    "progress_percent INTEGER NOT NULL DEFAULT 0, " +
                    "notes_summary TEXT" +
                    ");");

            stmt.execute("INSERT OR REPLACE INTO schema_version (version, applied_at) VALUES (" +
                    CURRENT_CATALOG_VERSION + ", '" + Instant.now().toString() + "');");
        }
        return true;
    }

    public static boolean initCaseDatabase(Connection conn) throws SQLException {
        createCaseSchemaVersionTable(conn);
        int version = getVersion(conn);
        if (version >= CURRENT_CASE_VERSION) {
            return false; // Already initialized
        }

        try (Statement stmt = conn.createStatement()) {
            // Case metadata
            stmt.execute("CREATE TABLE IF NOT EXISTS case_metadata (" +
                    "case_id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "summary TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'OPEN'" +
                    ");");

            // Canonical solution (Restricted internal table)
            stmt.execute("CREATE TABLE IF NOT EXISTS canonical_solution (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "culprit_id TEXT NOT NULL, " +
                    "culprit_name TEXT NOT NULL, " +
                    "motive TEXT NOT NULL, " +
                    "decisive_evidence_ids TEXT NOT NULL, " +
                    "explanation TEXT NOT NULL" +
                    ");");

            // Suspects
            stmt.execute("CREATE TABLE IF NOT EXISTS suspects (" +
                    "suspect_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "occupation TEXT NOT NULL, " +
                    "age INTEGER NOT NULL, " +
                    "alibi_statement TEXT NOT NULL, " +
                    "suspicion_level INTEGER NOT NULL DEFAULT 20, " +
                    "is_prime_suspect INTEGER NOT NULL DEFAULT 0" +
                    ");");

            // Evidence
            stmt.execute("CREATE TABLE IF NOT EXISTS evidence (" +
                    "evidence_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "location_found TEXT NOT NULL, " +
                    "time_recorded TEXT NOT NULL, " +
                    "associated_person_id TEXT, " +
                    "significance TEXT NOT NULL, " +
                    "is_discovered INTEGER NOT NULL DEFAULT 0" +
                    ");");

            // Clues
            stmt.execute("CREATE TABLE IF NOT EXISTS clues (" +
                    "clue_id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'LOCKED', " +
                    "prerequisite_evidence_ids TEXT NOT NULL" +
                    ");");

            // Timeline events
            stmt.execute("CREATE TABLE IF NOT EXISTS timeline_events (" +
                    "event_id TEXT PRIMARY KEY, " +
                    "timestamp_str TEXT NOT NULL, " +
                    "sort_order INTEGER NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "person_id TEXT, " +
                    "location TEXT NOT NULL, " +
                    "is_verified INTEGER NOT NULL DEFAULT 1, " +
                    "has_conflict INTEGER NOT NULL DEFAULT 0, " +
                    "conflict_details TEXT" +
                    ");");

            // Interrogation dialogue
            stmt.execute("CREATE TABLE IF NOT EXISTS interrogation_dialogues (" +
                    "dialogue_id TEXT PRIMARY KEY, " +
                    "suspect_id TEXT NOT NULL, " +
                    "question_text TEXT NOT NULL, " +
                    "response_text TEXT NOT NULL, " +
                    "is_asked INTEGER NOT NULL DEFAULT 0, " +
                    "extracted_statement_id TEXT" +
                    ");");

            // Statements
            stmt.execute("CREATE TABLE IF NOT EXISTS statements (" +
                    "statement_id TEXT PRIMARY KEY, " +
                    "person_id TEXT NOT NULL, " +
                    "statement_text TEXT NOT NULL, " +
                    "time_recorded TEXT NOT NULL, " +
                    "extracted_times TEXT, " +
                    "extracted_locations TEXT" +
                    ");");

            // Contradictions
            stmt.execute("CREATE TABLE IF NOT EXISTS contradictions (" +
                    "contradiction_id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "suspect_id TEXT NOT NULL, " +
                    "evidence_id TEXT, " +
                    "timeline_event_id TEXT, " +
                    "severity TEXT NOT NULL, " +
                    "is_resolved INTEGER NOT NULL DEFAULT 0" +
                    ");");

            // Graph connections
            stmt.execute("CREATE TABLE IF NOT EXISTS connections (" +
                    "connection_id TEXT PRIMARY KEY, " +
                    "source_id TEXT NOT NULL, " +
                    "source_label TEXT NOT NULL, " +
                    "target_id TEXT NOT NULL, " +
                    "target_label TEXT NOT NULL, " +
                    "relationship_type TEXT NOT NULL, " +
                    "weight REAL NOT NULL DEFAULT 1.0" +
                    ");");

            // Investigation state (Key-Value)
            stmt.execute("CREATE TABLE IF NOT EXISTS investigation_state (" +
                    "state_key TEXT PRIMARY KEY, " +
                    "state_value TEXT NOT NULL, " +
                    "updated_at TEXT NOT NULL" +
                    ");");

            // Investigation notes
            stmt.execute("CREATE TABLE IF NOT EXISTS investigation_notes (" +
                    "note_id TEXT PRIMARY KEY, " +
                    "content TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL" +
                    ");");

            // Deduction attempts
            stmt.execute("CREATE TABLE IF NOT EXISTS deduction_attempts (" +
                    "attempt_id TEXT PRIMARY KEY, " +
                    "suspect_id TEXT NOT NULL, " +
                    "motive TEXT NOT NULL, " +
                    "evidence_ids TEXT NOT NULL, " +
                    "is_correct INTEGER NOT NULL, " +
                    "score INTEGER NOT NULL, " +
                    "feedback TEXT NOT NULL, " +
                    "attempted_at TEXT NOT NULL" +
                    ");");

            stmt.execute("INSERT OR REPLACE INTO schema_version (version, applied_at) VALUES (" +
                    CURRENT_CASE_VERSION + ", '" + Instant.now().toString() + "');");
        }
        return true;
    }

    private static void createCatalogSchemaVersionTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version INTEGER PRIMARY KEY, " +
                    "applied_at TEXT NOT NULL" +
                    ");");
        }
    }

    private static void createCaseSchemaVersionTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version INTEGER PRIMARY KEY, " +
                    "applied_at TEXT NOT NULL" +
                    ");");
        }
    }

    private static int getVersion(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM schema_version")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }
}
