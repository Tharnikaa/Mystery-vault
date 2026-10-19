# Mystery Vault — Approved Production API Contract

**Status:** APPROVED & FULLY INTEGRATED  
**Language / Platform:** Java 17+ | Single Maven Desktop Application  
**Package Prefix:** `com.mysteryvault`

---

## 1. Module Ownership Matrix

| Module | Name | Owner | Canonical Packages / Classes |
| :--- | :--- | :--- | :--- |
| **Module 1** | Case Management | Member 1 | `model.Case`, `enums.CaseStatus`, `model.Priority`, `repository.GenericRepository`, `repository.impl.CaseMemoryRepository`, `service.CaseService` |
| **Module 2** | Suspect & Person | Member 1 | `model.Person`, `model.Suspect`, `service.SampleDataFactory` |
| **Module 3** | Evidence & Clues | Member 1 | `model.InvestigationAsset`, `model.Evidence`, `model.DigitalEvidence`, `model.DocumentEvidence`, `model.Clue`, `service.ClueUnlockEngine`, `exception.ClueLockedException` |
| **Module 4** | Connection Graph | Member 4 | `model.Connection`, `ui.swing.components.CustomGraphCanvas`, `ui.swing.viewmodel.GraphNode` |
| **Module 5** | Timeline & Chronology | Member 2 | `model.TimelineEvent`, `util.DateTimeUtils`, `analyzer.TimelineAnalyzerTask`, `analyzer.TimelineAnalysisResult` |
| **Module 6** | Statements & Regex | Member 2 | `model.Statement`, `model.ExtractedEntity`, `model.ExtractedStatementData`, `service.StatementRegexExtractor`, `util.RegexUtils` |
| **Module 7** | Contradiction Detection | Member 2 | `model.Contradiction`, `enums.Severity`, `exception.ContradictionException`, `service.ContradictionDetectorService` |
| **Module 8** | Concurrent Analysis | Member 3 | `analyzer.Analyzer`, `analyzer.AnalysisContext`, `analyzer.AnalysisResult`, `analyzer.ConcurrentAnalysisManager`, `analyzer.EvidenceAnalyzerTask`, `analyzer.StatementAnalyzerTask` |
| **Module 9** | Notes & Storage | Member 3 | `model.InvestigationNote`, `service.FileStorageService`, `util.SerializerUtils` |
| **Module 10** | Reports | Member 3 | `model.InvestigationReport`, `service.ReportGeneratorService` |
| **Module 11** | Final Deduction / Solver | Member 4 | `service.CaseSolverEngine`, `service.dto.CaseContext`, `service.dto.DeductionRequest`, `service.dto.DeductionResult` |
| **Module 12** | Diagnostics / JDBC Stub | Member 3 | `repository.impl.CaseJdbcRepositoryStub`, `repository.impl.EvidenceArchiveDao`, `web.MysteryVaultWebServer` |
| **Module 13** | App Shell & 12 Screens | Member 4 | `MysteryVaultApplication` (Main), `ui.swing.MainFrame`, `ui.swing.screens.*` (all 12 screens), `ui.swing.adapters.*`, `ui.swing.ports.*` |

---

## 2. Actual Public Service Signatures

### Member 1: Core Domain Services
```java
// com.mysteryvault.service.CaseService
public List<Case> listCases();
public Optional<Case> findById(String caseId);
public Case getCaseOrThrow(String caseId);
public Case createCase(Case draft);
public Case updateCase(Case updated);
public Case transitionCaseStatus(String caseId, CaseStatus newStatus);
public Case loadSampleCase103();

// com.mysteryvault.service.ClueUnlockEngine
public boolean canUnlock(Clue clue, Set<String> discoveredEvidenceIds);
public void unlock(Clue clue, Set<String> discoveredEvidenceIds) throws ClueLockedException;
```

### Member 2: Analysis & Extraction Services
```java
// com.mysteryvault.service.StatementRegexExtractor
public ExtractedStatementData extract(Statement statement);
public List<ExtractedEntity<LocalTime>> extractTimes(String text);
public List<ExtractedEntity<LocalDate>> extractDates(String text);
public List<ExtractedEntity<String>> extractReferenceIds(String text);
public List<ExtractedEntity<String>> extractLocations(String text);

// com.mysteryvault.service.ContradictionDetectorService
public List<Contradiction> detectContradictions(String caseId, List<Statement> statements, List<TimelineEvent> timelineEvents);

// com.mysteryvault.analyzer.TimelineAnalyzerTask implements Analyzer
public TimelineAnalysisResult execute();
public AnalysisResult analyze(AnalysisContext context) throws Exception;
```

### Member 3: Concurrency, Storage, & Reports
```java
// com.mysteryvault.analyzer.ConcurrentAnalysisManager
public List<AnalysisResult> runAnalysisConcurrently(List<Analyzer> analyzers, AnalysisContext context, long timeoutSeconds);
public void shutdown();

// com.mysteryvault.service.FileStorageService
public Path saveTextNote(InvestigationNote note) throws IOException;
public Path saveReportText(String caseId, String reportId, String formattedReportText) throws IOException;
public String readTextFile(Path filePath) throws IOException;

// com.mysteryvault.service.ReportGeneratorService
public InvestigationReport generateReport(Case caseEntity, List<Suspect> suspects, List<Evidence> evidenceList, List<Clue> clues, List<String> contradictions, String deductionConclusion, int confidenceScore);
```

### Member 4: Graph, Deduction Solver, & UI Ports
```java
// com.mysteryvault.service.CaseSolverEngine
public DeductionResult evaluate(DeductionRequest request, CaseContext caseContext);
public void registerCaseRule(CaseSolutionRule rule);

// com.mysteryvault.ui.swing.ports
public interface CaseViewPort { List<CaseCardViewModel> getAvailableCases(); CaseCardViewModel getActiveCase(); void selectCase(String caseId); }
public interface SuspectViewPort { List<SuspectViewModel> getSuspectsForCase(String caseId); SuspectViewModel getSuspectById(String suspectId); }
public interface EvidenceViewPort { List<EvidenceViewModel> getEvidenceForCase(String caseId); EvidenceViewModel getEvidenceById(String evidenceId); String examineEvidence(String evidenceId); String sendToLab(String evidenceId); }
public interface TimelineViewPort { List<TimelineEventViewModel> getTimelineForCase(String caseId); void addEvent(String caseId, String time, String description); }
public interface InterrogationViewPort { List<ChatMessageViewModel> getDialogueHistory(String suspectId); ChatMessageViewModel askQuestion(String suspectId, String question); ChatMessageViewModel showEvidence(String suspectId, String evidenceId); ChatMessageViewModel challengeStatement(String suspectId, String contradictionId); }
public interface ConnectionViewPort { List<GraphNode> getGraphNodes(String caseId); List<Connection> getConnections(String caseId); List<String> getDiscoveredConnectionSummaries(String caseId); }
public interface AnalysisViewPort { AnalysisViewModel getAnalysisResults(String caseId); }
public interface DeductionViewPort { DeductionResult submitDeduction(DeductionRequest request); DeductionResult getLastResult(); }
public interface ReportViewPort { ReportViewModel generateReport(String caseId); }
public interface ProfileViewPort { ProfileViewModel getPlayerProfile(); }
```
