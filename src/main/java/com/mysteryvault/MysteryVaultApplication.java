package com.mysteryvault;

import com.mysteryvault.analyzer.ConcurrentAnalysisManager;
import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.service.CaseSolverEngine;
import com.mysteryvault.ui.swing.MainFrame;
import com.mysteryvault.ui.swing.adapters.*;
import com.mysteryvault.ui.swing.ports.*;

import javax.swing.*;

/**
 * Mystery Vault — Uncover the Truth
 *
 * Single production main application entry point and composition root.
 * Initializes SQLite databases and wires all production adapters and services to the 12-screen UI.
 */
public class MysteryVaultApplication {

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // 1. Initialize & seed SQLite databases programmatically
        CaseDatabaseManager dbManager = new CaseDatabaseManager();
        try {
            dbManager.initializeAndSeedAll();
        } catch (Exception e) {
            System.err.println("Database initialization status: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            // 2. Application Composition Root — Live Integrated Adapters
            CaseSolverEngine solverEngine = new CaseSolverEngine(false);
            ConcurrentAnalysisManager analysisManager = new ConcurrentAnalysisManager(dbManager);

            CaseViewPort casePort = new IntegratedCaseAdapter(dbManager);
            SuspectViewPort suspectPort = new IntegratedSuspectAdapter(dbManager);
            EvidenceViewPort evidencePort = new IntegratedEvidenceAdapter(dbManager);
            TimelineViewPort timelinePort = new IntegratedTimelineAdapter(dbManager);
            InterrogationViewPort interrogationPort = new IntegratedInterrogationAdapter(dbManager);
            ConnectionViewPort connectionPort = new IntegratedConnectionAdapter(dbManager);
            AnalysisViewPort analysisPort = new IntegratedAnalysisAdapter(dbManager, analysisManager);
            DeductionViewPort deductionPort = new IntegratedDeductionAdapter(dbManager, solverEngine);
            ReportViewPort reportPort = new IntegratedReportAdapter(dbManager);
            ProfileViewPort profilePort = new IntegratedProfileAdapter(dbManager);

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
