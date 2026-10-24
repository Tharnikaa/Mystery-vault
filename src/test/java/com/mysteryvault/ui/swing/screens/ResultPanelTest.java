package com.mysteryvault.ui.swing.screens;

import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.service.dto.DeductionRequest;
import com.mysteryvault.service.dto.DeductionResult;
import com.mysteryvault.ui.swing.mock.MockDeductionViewPort;
import com.mysteryvault.ui.swing.mock.MockReportViewPort;
import com.mysteryvault.ui.swing.navigation.ScreenManager;
import com.mysteryvault.ui.swing.theme.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite verifying ResultPanel dynamic refresh behavior (Module 13 / Screen 12).
 *
 * Verifies:
 * - Refresh after consecutive deductions (Correct -> Solved, followed by Wrong -> Unresolved)
 * - Verifies CASE UNRESOLVED is displayed rather than retaining previous CASE SOLVED
 * - Score, verdict, statistics, and rank banner update accurately on refresh
 * - CardLayout ComponentShown listener invokes refresh
 */
public class ResultPanelTest {

    private MockDeductionViewPort deductionPort;
    private MockReportViewPort reportPort;
    private ResultPanel resultPanel;

    @BeforeAll
    static void initHeadlessEnvironment() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() {
        deductionPort = new MockDeductionViewPort(new CaseSolverEngine(true));
        reportPort = new MockReportViewPort();
        resultPanel = new ResultPanel(deductionPort, reportPort);
    }

    @Test
    @DisplayName("Scenario 9: ResultPanel refreshes after consecutive deductions (Solved -> Unresolved)")
    void testResultPanelRefreshAfterConsecutiveDeductions() {
        // Step 1: Submit Correct Deduction (Meera, Financial dispute, E01 + E03)
        Set<String> correctEvidence = new HashSet<>(Arrays.asList("E01", "E03", "E05"));
        DeductionRequest correctReq = new DeductionRequest("CASE_103", "MEERA", "Financial dispute",
                correctEvidence, "Paper trail confirms motive and sole access");
        deductionPort.submitDeduction(correctReq);

        // Explicitly trigger refresh
        resultPanel.refreshFromLastResult();

        // Verify initial SOLVED state
        assertTrue(resultPanel.isCaseSolved(), "Case must be marked as solved");
        assertEquals("CASE SOLVED", resultPanel.getVerdictText(), "Verdict should be CASE SOLVED");
        assertEquals(Theme.TEAL_ACCENT, resultPanel.getVerdictColor(), "Verdict color should be teal accent");
        assertEquals(92, resultPanel.getScore(), "Investigation score should be 92");
        assertEquals("0", resultPanel.getWrongAccusationsText(), "Wrong accusations should be 0");
        assertTrue(resultPanel.getRankBannerText().contains("MASTER DETECTIVE"), "Rank banner must contain MASTER DETECTIVE");
        assertEquals("THE POISONED DRINK", resultPanel.getCaseTitleText());

        // Step 2: Consecutive Wrong Deduction (Rohan, Accidental poisoning, E01)
        Set<String> wrongEvidence = new HashSet<>(Arrays.asList("E01"));
        DeductionRequest wrongReq = new DeductionRequest("CASE_103", "ROHAN", "Accidental poisoning",
                wrongEvidence, "Kitchen proximity speculation");
        deductionPort.submitDeduction(wrongReq);

        // Re-open / refresh ResultPanel
        resultPanel.refreshFromLastResult();

        // CRITICAL CHECK: Verify that submitting a wrong deduction displays CASE UNRESOLVED rather than CASE SOLVED
        assertFalse(resultPanel.isCaseSolved(), "Case must NOT remain solved after wrong deduction");
        assertEquals("CASE UNRESOLVED", resultPanel.getVerdictText(),
                "Verdict MUST refresh to CASE UNRESOLVED rather than previous CASE SOLVED");
        assertEquals(Theme.RED_CONFLICT, resultPanel.getVerdictColor(),
                "Verdict color must switch to RED_CONFLICT");
        assertTrue(resultPanel.getScore() <= 70, "Investigation score must decrease to partial score <= 70");
        assertEquals("1", resultPanel.getWrongAccusationsText(), "Wrong accusations must refresh to 1");
        assertFalse(resultPanel.getRankBannerText().contains("MASTER DETECTIVE"),
                "Rank banner must no longer indicate MASTER DETECTIVE");
    }

    @Test
    @DisplayName("ResultPanel handles empty/null result gracefully without NPE")
    void testResultPanelHandlesNullResultGracefully() {
        MockDeductionViewPort emptyPort = new MockDeductionViewPort() {
            @Override
            public DeductionResult getLastResult() {
                return null;
            }
        };

        ResultPanel panel = new ResultPanel(emptyPort, reportPort);
        panel.refreshFromLastResult();

        assertEquals("CASE UNRESOLVED", panel.getVerdictText());
        assertEquals("0 / 8", panel.getEvidenceStatText());
        assertEquals("0", panel.getWrongAccusationsText());
    }

    @Test
    @DisplayName("ComponentListener componentShown dispatches refreshFromLastResult")
    void testComponentShownListenerTriggersRefresh() {
        // Initial state
        assertEquals("CASE SOLVED", resultPanel.getVerdictText());

        // Submit wrong deduction
        DeductionRequest wrongReq = new DeductionRequest("CASE_103", "ARJUN", "Corporate blackmail",
                new HashSet<>(Arrays.asList("E03")), "Speculation");
        deductionPort.submitDeduction(wrongReq);

        // Simulate Swing CardLayout componentShown event
        ComponentEvent shownEvent = new ComponentEvent(resultPanel, ComponentEvent.COMPONENT_SHOWN);
        for (java.awt.event.ComponentListener cl : resultPanel.getComponentListeners()) {
            cl.componentShown(shownEvent);
        }

        // Must be updated to CASE UNRESOLVED
        assertEquals("CASE UNRESOLVED", resultPanel.getVerdictText(),
                "ComponentListener must trigger refreshFromLastResult on componentShown");
        assertEquals("1", resultPanel.getWrongAccusationsText());
    }

    /**
     * Standalone main runner for direct CLI execution without Maven.
     */
    public static void main(String[] args) {
        initHeadlessEnvironment();
        ResultPanelTest test = new ResultPanelTest();
        int passed = 0;

        test.setUp();
        test.testResultPanelRefreshAfterConsecutiveDeductions();
        passed++;

        test.setUp();
        test.testResultPanelHandlesNullResultGracefully();
        passed++;

        test.setUp();
        test.testComponentShownListenerTriggersRefresh();
        passed++;

        System.out.println("ResultPanelTest: ALL " + passed + " SCENARIOS PASSED!");
    }
}
