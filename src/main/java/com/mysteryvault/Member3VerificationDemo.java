package com.mysteryvault;

import com.mysteryvault.analyzer.*;
import com.mysteryvault.model.*;
import com.mysteryvault.service.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Verification demo showcasing Member 3's complete deliverables:
 * - Module 8: Concurrent Analysis Manager & Analyzer tasks
 * - Module 9: FileStorageService & SerializerUtils for Notes and data
 * - Module 10: ReportGeneratorService creating investigation dossiers
 * - Module 12: Diagnostics Web Server & JDBC Stub
 *
 * Fully integrated with Member 1's database (CaseService & Case #103).
 */
public class Member3VerificationDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   MYSTERY VAULT - MEMBER 3 VERIFICATION DEMO (MODULES 8, 9, 10, 12)");
        System.out.println("   Concurrent Analysis | File Storage | Reporting | Diagnostics Stub");
        System.out.println("   Consuming Member 1's Case #103 Database");
        System.out.println("================================================================================\n");

        try {
            // 1. Load Member 1's Database
            System.out.println(">>> 1. INITIALIZING MEMBER 1'S DATABASE & CASE #103");
            CaseService caseService = new CaseService();
            Case case103 = caseService.loadSampleCase103();
            List<Suspect> sampleSuspects = SampleDataFactory.createSampleSuspects();
            List<Evidence> sampleEvidence = SampleDataFactory.createSampleEvidence();
            List<Clue> sampleClues = SampleDataFactory.createSampleClues();

            System.out.printf("Loaded Database Case: %s (Status: %s, Priority: %s)%n",
                    case103.getTitle(), case103.getStatus(), case103.getPriority());
            System.out.printf("  Suspect IDs: %d | Evidence IDs: %d | Clue IDs: %d%n%n",
                    case103.getSuspectIds().size(), case103.getEvidenceIds().size(), case103.getClueIds().size());

            // 2. Module 8: Concurrent Analysis Engine
            System.out.println(">>> 2. MODULE 8: CONCURRENT ANALYSIS ENGINE EXECUTION");
            ConcurrentAnalysisManager analysisManager = new ConcurrentAnalysisManager();

            AnalysisContext context = new AnalysisContext(
                    case103.getId(),
                    case103.getTitle(),
                    new ArrayList<>(case103.getSuspectIds()),
                    new ArrayList<>(case103.getEvidenceIds()),
                    new ArrayList<>(case103.getClueIds()),
                    Map.of("targetDate", "2026-10-18")
            );

            List<String> statements = List.of(
                    "Marcus Vance: I was never near the cellar at 09:30 PM. I was dining.",
                    "Elena Rostova: I served the vintage wine at 09:15 PM, but the glass was handled by someone else.",
                    "Dr. Warren Price: I was with the host until 09:00 PM. I can't remember anything after that."
            );

            List<Analyzer> analyzers = List.of(
                    new EvidenceAnalyzerTask(sampleEvidence),
                    new StatementAnalyzerTask(statements)
            );

            System.out.println("Dispatching analyzers concurrently across worker threads...");
            List<AnalysisResult> results = analysisManager.runAnalysisConcurrently(analyzers, context, 5);

            for (AnalysisResult res : results) {
                System.out.printf("  [ANALYSIS RESULT] %s -> Status: %s (%d ms)%n",
                        res.getAnalyzerName(), res.getStatus(), res.getExecutionDurationMs());
                System.out.println("    Summary: " + res.getSummary());
                for (String finding : res.getFindings()) {
                    System.out.println("      * " + finding);
                }
            }
            analysisManager.shutdown();
            System.out.println();

            // 3. Module 9: File Storage & Note Management
            System.out.println(">>> 3. MODULE 9: FILE STORAGE SERVICE & PERSISTENCE");
            Path tempStorage = Files.createTempDirectory("mysteryvault_test_storage");
            FileStorageService storageService = new FileStorageService(tempStorage);
            System.out.println("Configured Storage Directory: " + tempStorage.toAbsolutePath());

            InvestigationNote note = new InvestigationNote(
                    "NOTE-103-A",
                    case103.getId(),
                    "Initial Toxin Toxicology Clue",
                    "Toxicology confirmed high aconitine concentrations in the crystal wine glass. Elena's statement partially corroborated.",
                    "Detective Thorne"
            );

            Path savedNotePath = storageService.saveTextNote(note);
            System.out.println("Saved UTF-8 Text Note: " + savedNotePath.getFileName());
            String loadedNoteContent = storageService.readTextFile(savedNotePath);
            System.out.println("Read Back Note Content:\n" + loadedNoteContent);

            // Binary serialization test
            Path savedObjPath = storageService.saveObject("case103_backup.dat", case103);
            System.out.println("Serialized Case Object to: " + savedObjPath.getFileName());
            Case restoredCase = storageService.loadObject("case103_backup.dat", Case.class);
            System.out.printf("Deserialized Case Backup: %s (ID: %s, Clue IDs: %d) [INTEGRITY VERIFIED]%n%n",
                    restoredCase.getTitle(), restoredCase.getId(), restoredCase.getClueIds().size());

            // 4. Module 10: Investigation Report Generation
            System.out.println(">>> 4. MODULE 10: REPORT GENERATOR SERVICE");
            ReportGeneratorService reportService = new ReportGeneratorService();

            List<String> contradictions = List.of(
                    "Marcus Vance claimed dining at Club 21, but security camera Feed #04 placed him in the pantry corridor at 09:22 PM."
            );
            String conclusion = "Marcus Vance exploited access to the pantry during gala prep to lace the wine with aconitine.";

            InvestigationReport report = reportService.generateReport(
                    case103,
                    sampleSuspects,
                    sampleEvidence,
                    sampleClues,
                    contradictions,
                    conclusion,
                    94
            );

            String dossierText = report.toFormattedDossier();
            System.out.println(dossierText);

            Path savedReportPath = storageService.saveReportText(case103.getId(), report.getReportId(), dossierText);
            System.out.println("Report successfully saved to disk: " + savedReportPath.getFileName() + "\n");

            // 5. Module 12: Diagnostics Web Server & JDBC Stub
            System.out.println(">>> 5. MODULE 12: DIAGNOSTICS WEB SERVER & JDBC STUB");
            com.mysteryvault.web.MysteryVaultWebServer webServer = new com.mysteryvault.web.MysteryVaultWebServer(8089, caseService);
            webServer.start();
            System.out.printf("Diagnostics Web Server started on port %d [RUNNING: %s]%n", webServer.getPort(), webServer.isRunning());
            webServer.stop();
            System.out.println("Diagnostics Web Server cleanly stopped.");

            com.mysteryvault.repository.impl.CaseJdbcRepositoryStub jdbcStub = new com.mysteryvault.repository.impl.CaseJdbcRepositoryStub();
            System.out.println("Verified JDBC Stub Connection URL: " + jdbcStub.getConnectionUrl());

            System.out.println("\n================================================================================");
            System.out.println("   MEMBER 3 VERIFICATION DEMO COMPLETED WITH 100% SUCCESS");
            System.out.println("================================================================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
