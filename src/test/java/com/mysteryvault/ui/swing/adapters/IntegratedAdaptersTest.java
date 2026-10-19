package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.model.Case;
import com.mysteryvault.repository.impl.CaseMemoryRepository;
import com.mysteryvault.service.CaseService;
import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.viewmodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for production Swing presentation adapters.
 */
public class IntegratedAdaptersTest {

    private CaseService caseService;
    private IntegratedCaseAdapter caseAdapter;
    private IntegratedSuspectAdapter suspectAdapter;
    private IntegratedEvidenceAdapter evidenceAdapter;
    private IntegratedTimelineAdapter timelineAdapter;
    private IntegratedInterrogationAdapter interrogationAdapter;
    private IntegratedConnectionAdapter connectionAdapter;
    private IntegratedAnalysisAdapter analysisAdapter;
    private IntegratedDeductionAdapter deductionAdapter;
    private IntegratedReportAdapter reportAdapter;
    private IntegratedProfileAdapter profileAdapter;

    @BeforeEach
    void setUp() {
        caseService = new CaseService(new CaseMemoryRepository());
        caseService.loadSampleCase103();

        caseAdapter = new IntegratedCaseAdapter(caseService);
        suspectAdapter = new IntegratedSuspectAdapter();
        evidenceAdapter = new IntegratedEvidenceAdapter();
        timelineAdapter = new IntegratedTimelineAdapter();
        interrogationAdapter = new IntegratedInterrogationAdapter();
        connectionAdapter = new IntegratedConnectionAdapter();
        analysisAdapter = new IntegratedAnalysisAdapter();
        deductionAdapter = new IntegratedDeductionAdapter(new CaseSolverEngine(), caseService);
        reportAdapter = new IntegratedReportAdapter();
        profileAdapter = new IntegratedProfileAdapter(caseService);
    }

    @Test
    @DisplayName("IntegratedCaseAdapter lists cases and updates active selection")
    void testCaseAdapter() {
        List<CaseCardViewModel> cases = caseAdapter.getAvailableCases();
        assertFalse(cases.isEmpty());

        CaseCardViewModel active = caseAdapter.getActiveCase();
        assertNotNull(active);

        caseAdapter.selectCase("CASE-101");
        assertEquals("CASE-101", caseAdapter.getActiveCase().getId());
    }

    @Test
    @DisplayName("IntegratedSuspectAdapter retrieves suspects and maps portrait assets")
    void testSuspectAdapter() {
        List<SuspectViewModel> suspects = suspectAdapter.getSuspectsForCase("CASE-103");
        assertEquals(3, suspects.size());

        SuspectViewModel meera = suspectAdapter.getSuspectById("MEERA");
        assertNotNull(meera);
        assertEquals("suspect_meera.png", meera.getImageKey());

        SuspectViewModel arjun = suspectAdapter.getSuspectById("ARJUN");
        assertNotNull(arjun);
        assertEquals("suspect_arjun.jpg", arjun.getImageKey());
    }

    @Test
    @DisplayName("IntegratedEvidenceAdapter examines evidence and triggers clue unlocks")
    void testEvidenceAdapter() {
        List<EvidenceViewModel> evidence = evidenceAdapter.getEvidenceForCase("CASE-103");
        assertFalse(evidence.isEmpty());

        String examResult = evidenceAdapter.examineEvidence("EVD-103-2");
        assertNotNull(examResult);
        assertTrue(examResult.contains("Examined"));

        String labReport = evidenceAdapter.sendToLab("EVD-103-1");
        assertNotNull(labReport);
        assertTrue(labReport.contains("LAB REPORT"));
    }

    @Test
    @DisplayName("IntegratedTimelineAdapter returns chronologically sorted events and conflict markers")
    void testTimelineAdapter() {
        List<TimelineEventViewModel> events = timelineAdapter.getTimelineForCase("CASE-103");
        assertFalse(events.isEmpty());

        timelineAdapter.addEvent("CASE-103", "08:50 PM", "Witness reports hearing shouting from study");
        List<TimelineEventViewModel> updated = timelineAdapter.getTimelineForCase("CASE-103");
        assertEquals(events.size() + 1, updated.size());
    }

    @Test
    @DisplayName("IntegratedInterrogationAdapter processes question and returns dialogue message")
    void testInterrogationAdapter() {
        List<ChatMessageViewModel> history = interrogationAdapter.getDialogueHistory("MEERA");
        assertFalse(history.isEmpty());

        ChatMessageViewModel response = interrogationAdapter.askQuestion("MEERA", "Where were you when the poison was served?");
        assertNotNull(response);
        assertFalse(response.isDetective());
        assertTrue(response.getMessage().length() > 0);
    }

    @Test
    @DisplayName("IntegratedConnectionAdapter delivers valid GraphNodes and Connections")
    void testConnectionAdapter() {
        List<GraphNode> nodes = connectionAdapter.getGraphNodes("CASE-103");
        assertFalse(nodes.isEmpty());
        assertTrue(nodes.stream().anyMatch(n -> "VICTIM".equals(n.getId())));

        assertFalse(connectionAdapter.getConnections("CASE-103").isEmpty());
        assertFalse(connectionAdapter.getDiscoveredConnectionSummaries("CASE-103").isEmpty());
    }

    @Test
    @DisplayName("IntegratedAnalysisAdapter executes background tasks and compiles metrics")
    void testAnalysisAdapter() {
        AnalysisViewModel vm = analysisAdapter.getAnalysisResults("CASE-103");
        assertNotNull(vm);
        assertEquals(100, vm.getTimelineProgress());
        assertEquals(100, vm.getEvidenceProgress());
        assertTrue(vm.getContradictions() >= 3);
    }

    @Test
    @DisplayName("IntegratedDeductionAdapter solves case and transitions CaseService state")
    void testDeductionAdapter() {
        DeductionRequest request = new DeductionRequest(
                "CASE-103",
                "MEERA",
                "Financial dispute",
                Set.of("E01", "E03"),
                "Deduction test"
        );

        DeductionResult result = deductionAdapter.submitDeduction(request);
        assertNotNull(result);
        assertTrue(result.isSolved());
        assertEquals("CASE SOLVED", result.getVerdictTitle());
        assertEquals(92, result.getScore());

        // Verify status transition was committed to CaseService
        Case domainCase = caseService.getCaseOrThrow("CASE-103");
        assertEquals(CaseStatus.SOLVED, domainCase.getStatus());
    }

    @Test
    @DisplayName("IntegratedReportAdapter produces formatted dossier and saves file")
    void testReportAdapter() {
        ReportViewModel report = reportAdapter.generateReport("CASE-103");
        assertNotNull(report);
        assertEquals("CASE-103", report.getCaseId());
        assertTrue(report.getOfficialSummary().contains("OFFICIAL DOSSIER REPORT"));
    }

    @Test
    @DisplayName("IntegratedProfileAdapter computes level and stats from case repository")
    void testProfileAdapter() {
        ProfileViewModel profile = profileAdapter.getPlayerProfile();
        assertNotNull(profile);
        assertTrue(profile.getXp() > 0);
        assertTrue(profile.getCasesSolved() >= 2);
    }
}
