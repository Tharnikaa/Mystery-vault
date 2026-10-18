package com.mysteryvault;

import com.mysteryvault.analyzer.*;
import com.mysteryvault.model.*;
import com.mysteryvault.service.*;
import com.mysteryvault.util.SerializerUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Member 3 deliverables (Modules 8, 9, 10, 12).
 */
public class Member3Test {

    @Test
    @DisplayName("ConcurrentAnalysisManager executes multiple analyzers and aggregates results")
    void testConcurrentAnalysis() {
        ConcurrentAnalysisManager manager = new ConcurrentAnalysisManager();
        AnalysisContext context = new AnalysisContext("CASE-01", "Test Case", List.of(), List.of(), List.of(), Map.of());

        Analyzer analyzer1 = ctx -> AnalysisResult.success("AnalyzerA", "Summary A", List.of("Finding 1"), Map.of(), 10);
        Analyzer analyzer2 = ctx -> AnalysisResult.success("AnalyzerB", "Summary B", List.of("Finding 2"), Map.of(), 15);

        List<AnalysisResult> results = manager.runAnalysisConcurrently(List.of(analyzer1, analyzer2), context, 5);

        assertEquals(2, results.size());
        assertEquals("AnalyzerA", results.get(0).getAnalyzerName());
        assertEquals("AnalyzerB", results.get(1).getAnalyzerName());
        assertEquals(AnalysisResult.Status.SUCCESS, results.get(0).getStatus());
        manager.shutdown();
    }

    @Test
    @DisplayName("SerializerUtils serializes and deserializes Java objects accurately")
    void testSerializationUtils() throws IOException, ClassNotFoundException {
        InvestigationNote note = new InvestigationNote("N-1", "C-1", "Alibi", "Checked out", "Detective");
        byte[] bytes = SerializerUtils.serializeToBytes(note);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        InvestigationNote deserialized = SerializerUtils.deserializeFromBytes(bytes, InvestigationNote.class);
        assertEquals("N-1", deserialized.getId());
        assertEquals("C-1", deserialized.getCaseId());
        assertEquals("Alibi", deserialized.getTitle());
    }

    @Test
    @DisplayName("FileStorageService writes and reads notes in UTF-8")
    void testFileStorageNotes() throws IOException {
        Path tempDir = Files.createTempDirectory("mv_storage_test");
        FileStorageService storage = new FileStorageService(tempDir);

        InvestigationNote note = new InvestigationNote("NOTE-99", "C-99", "Secret Meet", "Spotted at docks", "Agent X");
        Path savedFile = storage.saveTextNote(note);

        assertTrue(Files.exists(savedFile));
        String readContent = storage.readTextFile(savedFile);
        assertTrue(readContent.contains("Secret Meet"));
        assertTrue(readContent.contains("Spotted at docks"));
    }

    @Test
    @DisplayName("ReportGeneratorService creates formatted dossier with confidence score")
    void testReportGeneratorService() {
        ReportGeneratorService generator = new ReportGeneratorService();
        Case c = new Case("C-100", "Heist at Central Bank", "Vault breached", Priority.HIGH, "Investigator V");
        Suspect s = new Suspect("S-1", "John Doe", "Engineer", 35, "Home", 75, "Greed", false);

        InvestigationReport report = generator.generateReport(
                c,
                List.of(s),
                List.of(),
                List.of(),
                List.of("Alibi timeline mismatch"),
                "John Doe likely compromised the vault keypad.",
                88
        );

        assertNotNull(report.getReportId());
        assertEquals("C-100", report.getCaseId());
        assertEquals(88, report.getConfidenceScore());
        String formatted = report.toFormattedDossier();
        assertTrue(formatted.contains("OFFICIAL DOSSIER REPORT"));
        assertTrue(formatted.contains("John Doe"));
        assertTrue(formatted.contains("88 / 100"));
    }
}
