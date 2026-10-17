package com.mysteryvault;

import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.ui.swing.MainFrame;
import com.mysteryvault.ui.swing.mock.*;
import com.mysteryvault.ui.swing.ports.*;

import javax.swing.*;

/**
 * Mystery Vault — Uncover the Truth
 *
 * Single production main application entry point and composition root.
 * Owned by Member 4.
 */
public class MysteryVaultApplication {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            // Application Composition Root
            // Currently wired to deterministic presentation mock adapters for standalone execution.
            // When teammates merge their services, production adapters will be wired here.

            CaseSolverEngine solverEngine = new CaseSolverEngine();

            CaseViewPort casePort = new MockCaseViewPort();
            SuspectViewPort suspectPort = new MockSuspectViewPort();
            EvidenceViewPort evidencePort = new MockEvidenceViewPort();
            TimelineViewPort timelinePort = new MockTimelineViewPort();
            InterrogationViewPort interrogationPort = new MockInterrogationViewPort();
            ConnectionViewPort connectionPort = new MockConnectionViewPort();
            AnalysisViewPort analysisPort = new MockAnalysisViewPort();
            DeductionViewPort deductionPort = new MockDeductionViewPort(solverEngine);
            ReportViewPort reportPort = new MockReportViewPort();
            ProfileViewPort profilePort = new MockProfileViewPort();

            MainFrame frame = new MainFrame(
                    casePort,
                    suspectPort,
                    evidencePort,
                    timelinePort,
                    interrogationPort,
                    connectionPort,
                    analysisPort,
                    deductionPort,
                    reportPort,
                    profilePort
            );

            frame.setVisible(true);
        });
    }
}
