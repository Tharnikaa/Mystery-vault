package com.mysteryvault.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CaseDatabaseManagerTest {

    @TempDir
    Path tempDir;

    private CaseDatabaseManager dbManager;

    @BeforeEach
    void setUp() throws SQLException {
        dbManager = new CaseDatabaseManager(tempDir.toFile());
        dbManager.initializeAndSeedAll();
    }

    @Test
    void testDatabasesCreated() {
        File catalogFile = new File(dbManager.getDataDir(), "mysteryvault_catalog.db");
        assertTrue(catalogFile.exists(), "Catalog DB should exist");

        for (String cId : new String[]{"case_101.db", "case_102.db", "case_103.db", "case_104.db"}) {
            File caseFile = new File(dbManager.getCasesDir(), cId);
            assertTrue(caseFile.exists(), "Case file " + cId + " should exist");
        }
    }

    @Test
    void testCatalogContents() throws SQLException {
        try (Connection conn = dbManager.getCatalogConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM cases_catalog");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(4, rs.getInt(1), "Should contain 4 registered mystery cases");
        }

        try (Connection conn = dbManager.getCatalogConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT detective_name, experience_points FROM player_profile WHERE profile_id = ?")) {
            ps.setString(1, "default_detective");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("Detective Jack Malone", rs.getString("detective_name"));
                assertEquals(0, rs.getInt("experience_points"), "New detective must start with 0 XP");
            }
        }
    }

    @Test
    void testCaseIsolation() throws SQLException {
        // Case 101 suspects
        try (Connection conn101 = dbManager.getCaseConnection("CASE_101");
             PreparedStatement ps = conn101.prepareStatement("SELECT name FROM suspects WHERE suspect_id = 'SUS_101_3'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals("Marcus Vance", rs.getString("name"));
        }

        // Case 103 suspects
        try (Connection conn103 = dbManager.getCaseConnection("CASE_103");
             PreparedStatement ps = conn103.prepareStatement("SELECT name FROM suspects WHERE suspect_id = 'SUS_103_3'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals("Vivienne Sinclair", rs.getString("name"));
        }

        // Case 101 suspect should NOT exist in Case 103
        try (Connection conn103 = dbManager.getCaseConnection("CASE_103");
             PreparedStatement ps = conn103.prepareStatement("SELECT COUNT(*) FROM suspects WHERE suspect_id = 'SUS_101_3'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "Case 101 suspect must not exist in Case 103 database");
        }
    }

    @Test
    void testCanonicalSolutionIntegrity() throws SQLException {
        String[] cases = {"CASE_101", "CASE_102", "CASE_103", "CASE_104"};
        for (String cId : cases) {
            try (Connection conn = dbManager.getCaseConnection(cId);
                 PreparedStatement ps = conn.prepareStatement("SELECT culprit_id, decisive_evidence_ids FROM canonical_solution");
                 ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Canonical solution must exist for " + cId);
                String culpritId = rs.getString("culprit_id");
                String decisiveEvd = rs.getString("decisive_evidence_ids");
                assertNotNull(culpritId);
                assertNotNull(decisiveEvd);

                // Verify culprit exists in suspects table
                try (PreparedStatement checkSuspect = conn.prepareStatement("SELECT COUNT(*) FROM suspects WHERE suspect_id = ?")) {
                    checkSuspect.setString(1, culpritId);
                    try (ResultSet sRs = checkSuspect.executeQuery()) {
                        assertTrue(sRs.next());
                        assertEquals(1, sRs.getInt(1), "Culprit ID must exist in suspects table");
                    }
                }

                // Verify decisive evidence exists
                for (String evId : decisiveEvd.split(",")) {
                    try (PreparedStatement checkEv = conn.prepareStatement("SELECT COUNT(*) FROM evidence WHERE evidence_id = ?")) {
                        checkEv.setString(1, evId.trim());
                        try (ResultSet eRs = checkEv.executeQuery()) {
                            assertTrue(eRs.next());
                            assertEquals(1, eRs.getInt(1), "Decisive evidence ID must exist: " + evId);
                        }
                    }
                }
            }
        }
    }

    @Test
    void testRepeatInitializationDoesNotEraseData() throws SQLException {
        // Record something in state
        try (Connection conn = dbManager.getCaseConnection("CASE_101");
             PreparedStatement ps = conn.prepareStatement("INSERT INTO investigation_state (state_key, state_value, updated_at) VALUES (?, ?, ?)")) {
            ps.setString(1, "test_checkpoint");
            ps.setString(2, "stage_3");
            ps.setString(3, "2026-10-22T00:00:00Z");
            ps.executeUpdate();
        }

        // Run initializeAndSeedAll again
        dbManager.initializeAndSeedAll();

        // Checkpoint must still be there
        try (Connection conn = dbManager.getCaseConnection("CASE_101");
             PreparedStatement ps = conn.prepareStatement("SELECT state_value FROM investigation_state WHERE state_key = ?")) {
            ps.setString(1, "test_checkpoint");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "State checkpoint should survive re-initialization");
                assertEquals("stage_3", rs.getString("state_value"));
            }
        }
    }
}
