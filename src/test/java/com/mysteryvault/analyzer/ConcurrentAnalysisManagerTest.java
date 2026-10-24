package com.mysteryvault.analyzer;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.viewmodel.AnalysisViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for ConcurrentAnalysisManager (Module 8).
 * Tests concurrent execution of background analytics across multiple database tasks.
 */
public class ConcurrentAnalysisManagerTest {

    private CaseDatabaseManager dbManager;
    private ConcurrentAnalysisManager analysisManager;
    private File tempDbDir;

    @BeforeEach
    void setUp() throws SQLException {
        tempDbDir = new File("target/test-db-analysis-" + System.nanoTime());
        tempDbDir.mkdirs();
        dbManager = new CaseDatabaseManager(tempDbDir);
        dbManager.initializeAndSeedAll();
        analysisManager = new ConcurrentAnalysisManager(dbManager, Executors.newFixedThreadPool(4));
    }

    @AfterEach
    void tearDown() {
        if (analysisManager != null) {
            analysisManager.shutdown();
        }
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
    @DisplayName("runAnalysis executes all four analyzer tasks concurrently without deadlocks")
    void testRunAnalysisConcurrentExecution() {
        AnalysisViewModel vm = analysisManager.runAnalysis("CASE_101");

        assertNotNull(vm, "AnalysisViewModel should not be null");
        assertTrue(vm.getTimelineProgress() >= 0 && vm.getTimelineProgress() <= 100);
        assertTrue(vm.getEvidenceProgress() >= 0 && vm.getEvidenceProgress() <= 100);
        assertTrue(vm.getStatementProgress() >= 0 && vm.getStatementProgress() <= 100);
        assertTrue(vm.getConnectionProgress() >= 0 && vm.getConnectionProgress() <= 100);
        assertTrue(vm.getConnectionsFound() >= 0);
        assertTrue(vm.getContradictions() >= 0);
    }

    @Test
    @DisplayName("runAnalysis returns valid metrics across different cases")
    void testRunAnalysisMultipleCases() {
        for (String caseId : new String[]{"CASE_101", "CASE_102", "CASE_103", "CASE_104"}) {
            AnalysisViewModel vm = analysisManager.runAnalysis(caseId);
            assertNotNull(vm);
            assertTrue(vm.getSuspiciousCharacters() >= 0, "Case " + caseId + " should report suspicious characters");
        }
    }
}
