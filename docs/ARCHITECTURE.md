# MysteryVault — System Architecture & Technical Design

**Version:** 1.0  
**Target Platform:** Java 17+ Desktop (Java Swing, AWT, Java2D, SQLite JDBC, JUnit 5)  

---

## 1. Architectural Philosophy

MysteryVault is designed following clean, layered, domain-driven design principles:
- **Separation of Concerns:** Presentation (Swing UI) is completely decoupled from business domain logic and database operations.
- **Port and Adapter (Hexagonal Architecture):** The 12 UI screens communicate exclusively through typed interface ports (`CaseViewPort`, `SuspectViewPort`, `EvidenceViewPort`, `TimelineViewPort`, `InterrogationViewPort`, `ConnectionViewPort`, `AnalysisViewPort`, `DeductionViewPort`, `ReportViewPort`, `ProfileViewPort`). Production adapters implement these ports by delegating to services and repositories.
- **Physical Case Database Isolation:** Each mystery case resides in its own isolated SQLite database file (`data/cases/case_{ID}.db`), managed by a thread-safe `CaseDatabaseManager`. Global player profiles and case registry reside in `data/mysteryvault_catalog.db`.
- **Restricted Canonical Solution Security:** The true canonical solution, culprit, and secret facts are stored in private internal schema tables, never queried or exposed by player-facing inspection endpoints.

---

## 2. Logical Module Structure (13 Modules)

```
com.mysteryvault
├── MysteryVaultApplication.java        // Module 13: Entry point & composition root
├── database/                           // Module 12: SQLite JDBC connection & migrations
│   ├── CaseDatabaseManager.java
│   ├── DatabaseInitializer.java
│   └── DatabasePopulator.java
├── model/                              // Modules 1, 2, 3, 4, 5, 6, 7, 9
│   ├── Case.java
│   ├── Person.java
│   ├── Suspect.java
│   ├── Evidence.java
│   ├── DigitalEvidence.java
│   ├── DocumentEvidence.java
│   ├── Clue.java
│   ├── Connection.java
│   ├── TimelineEvent.java
│   ├── Statement.java
│   ├── Contradiction.java
│   ├── InvestigationNote.java
│   ├── InvestigationReport.java
│   └── PlayerProfile.java
├── enums/                              // Shared domain enumerations
│   ├── CaseStatus.java
│   ├── ClueStatus.java
│   ├── EvidenceType.java
│   ├── Priority.java
│   └── Severity.java
├── repository/                         // Data access layer
│   ├── GenericRepository.java
│   ├── CatalogRepository.java
│   └── CaseDatabaseRepository.java
├── service/                            // Core domain services
│   ├── CaseService.java                // Module 1
│   ├── SuspectService.java             // Module 2
│   ├── EvidenceService.java            // Module 3
│   ├── ClueUnlockEngine.java           // Module 3
│   ├── InterrogationService.java       // Module 2 & 6
│   ├── StatementRegexExtractor.java    // Module 6
│   ├── TimelineService.java            // Module 5
│   ├── ContradictionService.java       // Module 7
│   ├── CaseSolverEngine.java           // Module 11
│   ├── ReportGeneratorService.java     // Module 10
│   └── SaveLoadService.java            // Module 9
├── analyzer/                           // Concurrency and analysis engine (Module 8)
│   ├── Analyzer.java
│   ├── AnalysisResult.java
│   ├── ConcurrentAnalysisManager.java
│   ├── EvidenceAnalyzerTask.java
│   ├── TimelineAnalyzerTask.java
│   └── StatementAnalyzerTask.java
├── graph/                              // Connection graph algorithms (Module 4)
│   └── RelationshipGraph.java
├── ui/swing/                           // Complete UI layer (Module 13)
│   ├── MainFrame.java
│   ├── navigation/ScreenManager.java
│   ├── ports/                          // ViewPort interfaces
│   ├── adapters/                       // Production adapters bridging UI & services
│   ├── components/                     // Reusable Swing components (Buttons, Cards, GraphCanvas)
│   ├── screens/                        // All 12 Screen Panels
│   ├── theme/                          // Dark Navy, Charcoal & Gold theme tokens
│   └── viewmodel/                      // View models consumed by screens
└── util/
    ├── DateTimeUtils.java
    └── RegexUtils.java
```

---

## 3. UI Navigation & Screen Lifecycle (CardLayout)

All 12 screens are managed by `ScreenManager` using a `CardLayout` inside `MainFrame`:

1. **Screen 1: Main Menu** (`MainMenuPanel`) -> New Game, Load Game, Settings, Profile, Quit.
2. **Screen 2: Detective Profile** (`DetectiveProfilePanel`) -> Player stats, XP, rank, accuracy, case records.
3. **Screen 3: Case Selection** (`CaseSelectionPanel`) -> Case cards, difficulty, description, progress.
4. **Screen 4: Investigation Hub** (`InvestigationHubPanel`) -> Central navigation dashboard for active case.
5. **Screen 5: Suspect Screen** (`SuspectPanel`) -> Suspect cards, alibis, interrogation logs, suspicion metrics.
6. **Screen 6: Evidence Investigation** (`EvidencePanel`) -> Evidence catalog, clue unlocking, details.
7. **Screen 7: Interrogation** (`InterrogationPanel`) -> Dynamic Q&A dialogue, statement extraction.
8. **Screen 8: Investigation Timeline** (`TimelinePanel`) -> Chronological events, alibi vs fact conflicts.
9. **Screen 9: Case Connection Engine** (`ConnectionPanel`) -> Interactive 2D relationship graph.
10. **Screen 10: Case Analysis** (`AnalysisPanel`) -> Multi-threaded consistency and contradiction check.
11. **Screen 11: Final Deduction** (`DeductionPanel`) -> Suspect, motive, evidence submission.
12. **Screen 12: Case Result** (`ResultPanel`) -> Verdict, score, explanation, report generator.

---

## 4. Concurrency & Threading Model

- **Event Dispatch Thread (EDT):** All Swing component updates, repaint cycles, and user events occur strictly on the EDT.
- **Background Tasks:** Long-running operations (concurrent case analysis, database reads/writes, report exports) execute via `ConcurrentAnalysisManager` using a fixed thread pool (`Executors.newFixedThreadPool(4)`).
- **Callback Synchronization:** Results from asynchronous tasks are safely marshalled back to the EDT via `SwingUtilities.invokeLater(...)`.
