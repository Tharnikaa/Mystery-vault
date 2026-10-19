package com.mysteryvault;

import com.mysteryvault.analyzer.ConcurrentAnalysisManager;
import com.mysteryvault.repository.impl.CaseMemoryRepository;
import com.mysteryvault.service.*;
import com.mysteryvault.ui.swing.MainFrame;
import com.mysteryvault.ui.swing.adapters.*;
import com.mysteryvault.ui.swing.mock.*;
import com.mysteryvault.ui.swing.ports.*;

import javax.swing.*;
import java.util.Arrays;

/**
 * Mystery Vault — Uncover the Truth
 *
 * Single production main application entry point and composition root.
 * Integrates all four teammates' modules:
 * - Member 1: Case, Suspect, Evidence, Clue domain models and CaseService (Modules 1, 2, 3)
 * - Member 2: Timeline, Statement Regex, and Contradiction Detection (Modules 5, 6, 7)
 * - Member 3: Concurrent Analysis, File Storage, and Dossier Reports (Modules 8, 9, 10, 12)
 * - Member 4: Clue Network Graph, Solver Engine, and complete 12-screen Swing UI (Modules 4, 11, 13)
 */
public class MysteryVaultApplication {

    public enum AppMode {
        INTEGRATED,
        DEMO
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        boolean forceDemo = args != null && Arrays.stream(args)
                .anyMatch(arg -> "--demo".equalsIgnoreCase(arg) || "--mock".equalsIgnoreCase(arg));
        AppMode mode = forceDemo ? AppMode.DEMO : AppMode.INTEGRATED;

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = createMainFrame(mode);
            frame.setVisible(true);
        });
    }

    /**
     * Composition root factory that initializes shared backend services and presentation ports.
     *
     * @param mode Selected startup mode (INTEGRATED vs DEMO)
     * @return Fully configured MainFrame instance ready for display
     */
    public static MainFrame createMainFrame(AppMode mode) {
        if (mode == AppMode.DEMO) {
            return new MainFrame();
        }

        // =========================================================================
        // COMPOSITION ROOT: PRODUCTION INTEGRATED SERVICES
        // =========================================================================

        // Member 1: Core Domain Services & Repository
        CaseMemoryRepository caseRepository = new CaseMemoryRepository();
        CaseService caseService = new CaseService(caseRepository);
        caseService.loadSampleCase103(); // Seed canonical Case #103
        ClueUnlockEngine clueUnlockEngine = new ClueUnlockEngine();

        // Member 2: Analysis & Extraction Services
        StatementRegexExtractor regexExtractor = new StatementRegexExtractor();
        ContradictionDetectorService contradictionDetector = new ContradictionDetectorService();

        // Member 3: Infrastructure Services
        ConcurrentAnalysisManager analysisManager = new ConcurrentAnalysisManager();
        FileStorageService fileStorage = new FileStorageService();
        ReportGeneratorService reportGenerator = new ReportGeneratorService();

        // Member 4: Solver & Visual Visualization
        CaseSolverEngine solverEngine = new CaseSolverEngine();

        // =========================================================================
        // PRESENTATION PORTS: PRODUCTION ADAPTER WIRING
        // =========================================================================
        CaseViewPort casePort = new IntegratedCaseAdapter(caseService);
        SuspectViewPort suspectPort = new IntegratedSuspectAdapter(SampleDataFactory.createSampleSuspects());
        EvidenceViewPort evidencePort = new IntegratedEvidenceAdapter(clueUnlockEngine, SampleDataFactory.createSampleEvidence(), SampleDataFactory.createSampleClues());
        TimelineViewPort timelinePort = new IntegratedTimelineAdapter();
        InterrogationViewPort interrogationPort = new IntegratedInterrogationAdapter(regexExtractor, contradictionDetector);
        ConnectionViewPort connectionPort = new IntegratedConnectionAdapter();
        AnalysisViewPort analysisPort = new IntegratedAnalysisAdapter(analysisManager, contradictionDetector);
        DeductionViewPort deductionPort = new IntegratedDeductionAdapter(solverEngine, caseService);
        ReportViewPort reportPort = new IntegratedReportAdapter(reportGenerator, fileStorage, caseService);
        ProfileViewPort profilePort = new IntegratedProfileAdapter(caseService);

        return new MainFrame(
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
    }
}
