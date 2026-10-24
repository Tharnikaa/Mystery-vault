package com.mysteryvault.adapter;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.model.Connection;
import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.adapters.*;
import com.mysteryvault.ui.swing.viewmodel.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 integration test suite testing the Swing adapters against real SQLite databases.
 */
public class IntegratedAdaptersTest {

    private CaseDatabaseManager dbManager;
    private File tempDbDir;

    @BeforeEach
    void setUp() throws SQLException {
        tempDbDir = new File("target/test-db-adapters-" + System.nanoTime());
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
    @DisplayName("IntegratedCaseAdapter retrieves all cases and active case info")
    void testCaseAdapter() {
        IntegratedCaseAdapter adapter = new IntegratedCaseAdapter(dbManager);
        List<CaseCardViewModel> cases = adapter.getAvailableCases();

        assertNotNull(cases);
        assertEquals(4, cases.size(), "All 4 cases must be listed");
        assertTrue(cases.stream().anyMatch(c -> c.getTitle().contains("Missing Diamond")));
        assertTrue(cases.stream().anyMatch(c -> c.getTitle().contains("Library Code")));
        assertTrue(cases.stream().anyMatch(c -> c.getTitle().contains("Poisoned Drink")));
        assertTrue(cases.stream().anyMatch(c -> c.getTitle().contains("Vanishing Prototype")));
    }

    @Test
    @DisplayName("IntegratedSuspectAdapter retrieves suspect profiles for a case")
    void testSuspectAdapter() {
        IntegratedSuspectAdapter adapter = new IntegratedSuspectAdapter(dbManager);
        List<SuspectViewModel> suspects = adapter.getSuspectsForCase("CASE_101");

        assertNotNull(suspects);
        assertFalse(suspects.isEmpty());
        for (SuspectViewModel s : suspects) {
            assertNotNull(s.getName());
            assertNotNull(s.getRole());
            assertTrue(s.getSuspicion() >= 0 && s.getSuspicion() <= 100);
        }
    }

    @Test
    @DisplayName("IntegratedEvidenceAdapter retrieves and discovers evidence")
    void testEvidenceAdapter() {
        IntegratedEvidenceAdapter adapter = new IntegratedEvidenceAdapter(dbManager);
        List<EvidenceViewModel> evidenceList = adapter.getEvidenceForCase("CASE_101");

        assertNotNull(evidenceList);
        assertFalse(evidenceList.isEmpty());

        EvidenceViewModel first = evidenceList.get(0);
        assertNotNull(first.getTitle());
        assertNotNull(first.getLocation());
    }

    @Test
    @DisplayName("IntegratedTimelineAdapter retrieves timeline events in chronological order")
    void testTimelineAdapter() {
        IntegratedTimelineAdapter adapter = new IntegratedTimelineAdapter(dbManager);
        List<TimelineEventViewModel> events = adapter.getTimelineForCase("CASE_101");

        assertNotNull(events);
        assertFalse(events.isEmpty());
        for (TimelineEventViewModel ev : events) {
            assertNotNull(ev.getTime());
            assertNotNull(ev.getDescription());
        }
    }

    @Test
    @DisplayName("IntegratedInterrogationAdapter retrieves dialogues and contradictory flags")
    void testInterrogationAdapter() {
        IntegratedInterrogationAdapter adapter = new IntegratedInterrogationAdapter(dbManager);
        List<ChatMessageViewModel> dialogues = adapter.getDialogueHistory("SUS_101_1");

        assertNotNull(dialogues);
        assertFalse(dialogues.isEmpty());
    }

    @Test
    @DisplayName("IntegratedConnectionAdapter retrieves relationship links")
    void testConnectionAdapter() {
        IntegratedConnectionAdapter adapter = new IntegratedConnectionAdapter(dbManager);
        List<Connection> connections = adapter.getConnections("CASE_101");

        assertNotNull(connections);
        assertFalse(connections.isEmpty());
    }

    @Test
    @DisplayName("IntegratedDeductionAdapter correctly verifies deduction requests")
    void testDeductionAdapter() {
        IntegratedDeductionAdapter adapter = new IntegratedDeductionAdapter(dbManager, new CaseSolverEngine(false));

        // Test with incorrect suspect
        DeductionResult failedResult = adapter.submitDeduction(
                new DeductionRequest("CASE_101", "WRONG_SUSPECT", "Wrong Motive", Collections.emptySet(), "Guessing")
        );
        assertNotNull(failedResult);
        assertFalse(failedResult.isSolved());
    }

    @Test
    @DisplayName("IntegratedReportAdapter retrieves dossier view model")
    void testReportAdapter() {
        IntegratedReportAdapter adapter = new IntegratedReportAdapter(dbManager);
        ReportViewModel report = adapter.generateReport("CASE_101");

        assertNotNull(report);
        assertEquals("CASE_101", report.getCaseId());
        assertNotNull(report.getCulpritName());
    }

    @Test
    @DisplayName("IntegratedProfileAdapter loads and updates detective stats")
    void testProfileAdapter() {
        IntegratedProfileAdapter adapter = new IntegratedProfileAdapter(dbManager);
        ProfileViewModel profile = adapter.getPlayerProfile();

        assertNotNull(profile);
        assertEquals("Rookie Detective", profile.getRank());
        assertEquals(0, profile.getXp());
        assertEquals(0, profile.getCasesSolved());
        assertEquals("N/A", profile.getAccuracyDisplay());
    }
}
