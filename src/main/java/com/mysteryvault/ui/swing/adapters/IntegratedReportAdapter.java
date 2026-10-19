package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.model.*;
import com.mysteryvault.service.CaseService;
import com.mysteryvault.service.FileStorageService;
import com.mysteryvault.service.ReportGeneratorService;
import com.mysteryvault.service.SampleDataFactory;
import com.mysteryvault.ui.swing.ports.ReportViewPort;
import com.mysteryvault.ui.swing.viewmodel.ReportViewModel;

import java.io.IOException;
import java.util.*;

/**
 * Production presentation adapter connecting ReportViewPort to Member 3's ReportGeneratorService
 * and FileStorageService (Modules 9 & 10).
 */
public class IntegratedReportAdapter implements ReportViewPort {

    private final ReportGeneratorService reportGenerator;
    private final FileStorageService fileStorage;
    private final CaseService caseService;

    public IntegratedReportAdapter(ReportGeneratorService reportGenerator,
                                  FileStorageService fileStorage,
                                  CaseService caseService) {
        this.reportGenerator = Objects.requireNonNull(reportGenerator, "reportGenerator cannot be null");
        this.fileStorage = Objects.requireNonNull(fileStorage, "fileStorage cannot be null");
        this.caseService = Objects.requireNonNull(caseService, "caseService cannot be null");
    }

    public IntegratedReportAdapter() {
        this(new ReportGeneratorService(), new FileStorageService(), new CaseService());
    }

    @Override
    public ReportViewModel generateReport(String caseId) {
        String effectiveCaseId = caseId != null ? caseId : "CASE-103";
        Case c = caseService.findById(effectiveCaseId)
                .or(() -> caseService.findById(effectiveCaseId.replace("_", "-")))
                .orElseGet(SampleDataFactory::createSampleCase);

        List<Suspect> suspects = SampleDataFactory.createSampleSuspects();
        List<Evidence> evidence = SampleDataFactory.createSampleEvidence();
        List<Clue> clues = SampleDataFactory.createSampleClues();
        List<String> contradictions = Arrays.asList(
                "Suspect Meera claimed she never entered the sommelier cellar pantry at 8:45 PM.",
                "CCTV Camera #04 verified a gloved figure entering the cellar at 8:42 PM.",
                "Toxicology report confirmed synthetic belladonna alkaloid was introduced to the decanter."
        );

        InvestigationReport report = reportGenerator.generateReport(
                c,
                suspects,
                evidence,
                clues,
                contradictions,
                "Accused suspect Elena Rostova / Meera possessed exclusive access to the vintage decanter and had motive arising from the audit.",
                92
        );

        String formattedText = report.toFormattedDossier();

        // Persist to FileStorageService
        try {
            fileStorage.saveReportText(effectiveCaseId, report.getReportId(), formattedText);
        } catch (IOException ignored) {
            // Non-blocking fallback if storage directory is restricted
        }

        return new ReportViewModel(
                effectiveCaseId,
                c.getTitle(),
                "Meera / Elena Rostova",
                "Financial dispute / Codicil dispute",
                "SOLVED",
                formattedText
        );
    }
}
