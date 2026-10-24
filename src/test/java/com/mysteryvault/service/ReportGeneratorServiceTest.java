package com.mysteryvault.service;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.viewmodel.ReportViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for ReportGeneratorService (Module 10).
 * Verifies compiling dossiers from SQLite and exporting confidential reports to disk.
 */
public class ReportGeneratorServiceTest {

    private CaseDatabaseManager dbManager;
    private ReportGeneratorService reportService;
    private File tempDbDir;
    private File tempReportFile;

    @BeforeEach
    void setUp() throws SQLException {
        tempDbDir = new File("target/test-db-report-" + System.nanoTime());
        tempDbDir.mkdirs();
        dbManager = new CaseDatabaseManager(tempDbDir);
        dbManager.initializeAndSeedAll();
        reportService = new ReportGeneratorService(dbManager);
        tempReportFile = new File("target/test-dossier-" + System.nanoTime() + ".txt");
    }

    @AfterEach
    void tearDown() {
        deleteRecursively(tempDbDir);
        if (tempReportFile != null && tempReportFile.exists()) {
            tempReportFile.delete();
        }
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
    @DisplayName("generateReport compiles full dossier metadata and solution")
    void testGenerateReport() {
        ReportViewModel vm = reportService.generateReport("CASE_101");

        assertNotNull(vm);
        assertEquals("CASE_101", vm.getCaseId());
        assertNotNull(vm.getCaseTitle());
        assertNotNull(vm.getCulpritName());
        assertNotNull(vm.getMotive());
        assertNotNull(vm.getOfficialSummary());
        assertTrue(vm.getOfficialSummary().contains("CASE SYNOPSIS"));
    }

    @Test
    @DisplayName("exportReportToFile writes formatted confidential dossier to disk")
    void testExportReportToFile() throws IOException {
        boolean exported = reportService.exportReportToFile("CASE_101", tempReportFile);

        assertTrue(exported, "Report export should succeed");
        assertTrue(tempReportFile.exists(), "Export file should exist on disk");
        assertTrue(tempReportFile.length() > 0, "Export file should not be empty");

        String content = Files.readString(tempReportFile.toPath());
        assertTrue(content.contains("MYSTERYVAULT OFFICIAL INVESTIGATION DOSSIER"));
        assertTrue(content.contains("CASE ID:    CASE_101"));
        assertTrue(content.contains("CULPRIT:"));
        assertTrue(content.contains("CONFIDENTIAL — BUREAU OF CRIMINAL INVESTIGATION"));
    }
}
