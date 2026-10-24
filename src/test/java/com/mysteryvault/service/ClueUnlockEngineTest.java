package com.mysteryvault.service;

import com.mysteryvault.database.CaseDatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for ClueUnlockEngine (Module 3).
 * Verifies deterministic prerequisite validation and database unlock mutations.
 */
public class ClueUnlockEngineTest {

    private ClueUnlockEngine engine;
    private CaseDatabaseManager dbManager;
    private File tempDbDir;

    @BeforeEach
    void setUp() throws SQLException {
        engine = new ClueUnlockEngine();
        tempDbDir = new File("target/test-db-clues-" + System.nanoTime());
        tempDbDir.mkdirs();
        dbManager = new CaseDatabaseManager(tempDbDir);
        dbManager.initializeAndSeedAll();
    }

    @AfterEach
    void tearDown() {
        deleteRecursively(tempDbDir);
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    @Test
    @DisplayName("canUnlock returns true when prerequisites are null or empty")
    void testCanUnlockNoPrerequisites() {
        Set<String> discovered = new HashSet<>();
        assertTrue(engine.canUnlock(null, discovered));
        assertTrue(engine.canUnlock("", discovered));
        assertTrue(engine.canUnlock("   ", discovered));
    }

    @Test
    @DisplayName("canUnlock returns true when all prerequisite evidence IDs are present")
    void testCanUnlockAllPrerequisitesPresent() {
        Set<String> discovered = new HashSet<>();
        discovered.add("EV_101_1");
        discovered.add("EV_101_2");
        discovered.add("EV_101_3");

        assertTrue(engine.canUnlock("EV_101_1", discovered));
        assertTrue(engine.canUnlock("EV_101_1, EV_101_2", discovered));
        assertTrue(engine.canUnlock("EV_101_2,EV_101_3", discovered));
    }

    @Test
    @DisplayName("canUnlock returns false when any prerequisite evidence ID is missing")
    void testCanUnlockMissingPrerequisite() {
        Set<String> discovered = new HashSet<>();
        discovered.add("EV_101_1");

        assertFalse(engine.canUnlock("EV_101_1, EV_101_2", discovered));
        assertFalse(engine.canUnlock("EV_101_99", discovered));
    }

    @Test
    @DisplayName("unlockClueInDatabase unlocks clue when evidence prerequisites are satisfied")
    void testUnlockClueInDatabaseSuccess() throws SQLException {
        try (Connection conn = dbManager.getCaseConnection("CASE_101")) {
            // Ensure EVD_101_GLASS_CUTTER is discovered
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE evidence SET is_discovered = 1 WHERE evidence_id = 'EVD_101_GLASS_CUTTER'")) {
                ps.executeUpdate();
            }

            // Insert test clue requiring EVD_101_GLASS_CUTTER
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO clues (clue_id, title, description, status, prerequisite_evidence_ids) " +
                    "VALUES ('CLUE_TEST_1', 'Test Clue', 'Unlocked clue', 'LOCKED', 'EVD_101_GLASS_CUTTER')")) {
                ps.executeUpdate();
            }

            boolean unlocked = engine.unlockClueInDatabase(conn, "CLUE_TEST_1");
            assertTrue(unlocked, "Clue with satisfied prerequisites should unlock");

            // Verify status in database
            try (PreparedStatement ps = conn.prepareStatement("SELECT status FROM clues WHERE clue_id = 'CLUE_TEST_1'")) {
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                assertEquals("UNLOCKED", rs.getString("status"));
            }
        }
    }

    @Test
    @DisplayName("unlockClueInDatabase rejects unlock when prerequisite evidence is not yet discovered")
    void testUnlockClueInDatabaseBlocked() throws SQLException {
        try (Connection conn = dbManager.getCaseConnection("CASE_101")) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO clues (clue_id, title, description, status, prerequisite_evidence_ids) " +
                    "VALUES ('CLUE_TEST_BLOCKED', 'Locked Clue', 'Cannot unlock yet', 'LOCKED', 'EV_UNDISCOVERED_999')")) {
                ps.executeUpdate();
            }

            boolean unlocked = engine.unlockClueInDatabase(conn, "CLUE_TEST_BLOCKED");
            assertFalse(unlocked, "Clue with missing prerequisite evidence must not unlock");

            try (PreparedStatement ps = conn.prepareStatement("SELECT status FROM clues WHERE clue_id = 'CLUE_TEST_BLOCKED'")) {
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next());
                assertEquals("LOCKED", rs.getString("status"));
            }
        }
    }
}
