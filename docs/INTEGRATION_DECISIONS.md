# Mystery Vault — Integration Decisions & Architecture Log

**Document Version:** 1.0.0  
**Integration Branch:** `integration/all-members`  
**Base Repository / Branch:** `feature/member4-ui-refactor`  
**Target Platform:** Java 17+ | Apache Maven | Desktop Swing Single Process

---

## 1. Executive Summary

This document records all architectural alignment decisions, module ownership assignments, API reconciliation steps, duplicate class resolutions, and adapter patterns applied during the four-member codebase integration of Mystery Vault.

All four members' modules were merged into a single cohesive Maven desktop project without sacrificing Member 4's authoritative 12-screen Swing UI, assets, or EDT responsiveness.

---

## 2. Inventory & Deduplication Decisions

### 2.1 Elimination of 20 Identical Files (Member 1 vs Member 3)
- **Problem:** Member 3's branch (`origin/member-3`) contained 20 exact duplicate Java source files copied from Member 1 (`Case.java`, `Suspect.java`, `Evidence.java`, `DigitalEvidence.java`, `DocumentEvidence.java`, `InvestigationAsset.java`, `Person.java`, `Priority.java`, `Clue.java`, `CaseStatus.java`, `CaseNotFoundException.java`, `CaseValidationException.java`, `ClueLockedException.java`, `InvalidCaseTransitionException.java`, `GenericRepository.java`, `CaseMemoryRepository.java`, `CaseService.java`, `ClueUnlockEngine.java`, `SampleDataFactory.java`, `Member1VerificationDemo.java`).
- **Decision:** Member 1 is the canonical owner of Modules 1, 2, and 3. The 20 duplicate classes were imported strictly from `origin/member-1`. Redundant copies in Member 3 were omitted to prevent duplicate classpath symbols and divergent drift.

### 2.2 Entry Point Collision (`Main.java` vs `MysteryVaultApplication.java`)
- **Problem:** Both Member 2 and Member 4 had a `com/mysteryvault/Main.java`. Member 2's `Main.java` instantiated obsolete legacy screen classes. Member 4 owns `com.mysteryvault.MysteryVaultApplication` as the canonical production application entry point and composition root.
- **Decision:** Retained `com.mysteryvault.MysteryVaultApplication` as the sole authoritative launcher. Member 4's `Main.java` was preserved as a lightweight backward-compatibility delegator calling `MysteryVaultApplication.main(args)`. Member 2's legacy launcher was discarded.

### 2.3 Legacy UI Classes (Member 2)
- **Problem:** Member 2's branch included legacy prototypes in `com.mysteryvault.screens`, `components`, `navigation`, and `theme`.
- **Decision:** Discarded completely. Member 4 owns all 12 Swing screens (`ui.swing.screens.*`), navigation (`ScreenManager`), custom components (`CustomGraphCanvas`, `GlassPanel`, etc.), and visual themes. Only Member 2's pure analysis classes (Modules 5, 6, 7) were imported.

---

## 3. Analyzer Contract Harmonization (Module 5 & 8)

- **Problem:**
  - Member 3 defined the `@FunctionalInterface Analyzer` with method `AnalysisResult analyze(AnalysisContext context) throws Exception`.
  - Member 2 defined `TimelineAnalyzerTask` with method `TimelineAnalysisResult execute()`.
- **Decision:**
  - Updated `TimelineAnalyzerTask` to implement `Analyzer`.
  - Kept the domain-specific `execute()` returning `TimelineAnalysisResult` for standalone tests and timeline calculations.
  - Implemented `analyze(AnalysisContext)` which runs the timeline analysis, computes metrics/temporal collisions, and packages findings into an immutable `AnalysisResult`.
  - Result: `ConcurrentAnalysisManager` can run `EvidenceAnalyzerTask`, `StatementAnalyzerTask`, and `TimelineAnalyzerTask` in parallel across worker threads without freezing the Swing EDT.

---

## 4. State Machine Transition & Deduction Persistence (Modules 1 & 11)

- **Problem:**
  - Member 4's `CaseSolverEngine` evaluates deduction requests against rules and computes verdicts/scores.
  - Member 1's `CaseService` is the sole authoritative gateway enforcing legal `CaseStatus` transitions (`OPEN -> ACTIVE -> SOLVED -> ARCHIVED`).
- **Decision:**
  - `CaseSolverEngine` remains a pure functional evaluation engine without database or repository side-effects.
  - `IntegratedDeductionAdapter` coordinates the two: upon receiving a successful verdict (`CASE SOLVED`), it invokes `CaseService.transitionCaseStatus(caseId, CaseStatus.SOLVED)` through the repository state machine.
  - The UI panel never mutates domain status directly.

---

## 5. ViewPort Adapter Architecture (Module 13 Composition Root)

All 12 Swing screens consume presentation ports (`com.mysteryvault.ui.swing.ports.*`), completely decoupled from domain repositories:
1. `IntegratedCaseAdapter` -> Connects `CaseViewPort` to `CaseService` (Module 1).
2. `IntegratedSuspectAdapter` -> Connects `SuspectViewPort` to `Suspect` / `SampleDataFactory` (Module 2).
3. `IntegratedEvidenceAdapter` -> Connects `EvidenceViewPort` to `Evidence`, `Clue`, and `ClueUnlockEngine` (Module 3).
4. `IntegratedTimelineAdapter` -> Connects `TimelineViewPort` to `TimelineEvent`, `DateTimeUtils`, and `TimelineAnalyzerTask` (Module 5).
5. `IntegratedInterrogationAdapter` -> Connects `InterrogationViewPort` to `StatementRegexExtractor` and `ContradictionDetectorService` (Modules 6 & 7).
6. `IntegratedConnectionAdapter` -> Connects `ConnectionViewPort` to `Connection` graph nodes (Module 4).
7. `IntegratedAnalysisAdapter` -> Connects `AnalysisViewPort` to `ConcurrentAnalysisManager` and `ContradictionDetectorService` (Modules 7 & 8).
8. `IntegratedDeductionAdapter` -> Connects `DeductionViewPort` to `CaseSolverEngine` (Module 11) and `CaseService` (Module 1).
9. `IntegratedReportAdapter` -> Connects `ReportViewPort` to `ReportGeneratorService` (Module 10) and `FileStorageService` (Module 9).
10. `IntegratedProfileAdapter` -> Connects `ProfileViewPort` to case repository metrics.

A startup flag (`--demo` or `--mock`) allows instant fallback to `Mock*ViewPort` implementations for regression and visual testing.

---

## 6. Persistence & Diagnostics Status (Module 9 & 12)

- **File Storage:** Active and fully operational via `FileStorageService` (saving UTF-8 investigation notes and dossier reports to `./storage/` or temporary data directories).
- **PostgreSQL / JDBC:** Member 3's `CaseJdbcRepositoryStub` and `EvidenceArchiveDao` are maintained as explicit stubs. PostgreSQL is **not** required for desktop execution. Runtime state is stored via `CaseMemoryRepository` and `FileStorageService`.
- **Diagnostics Web Server:** `MysteryVaultWebServer` is optional and disabled by default on startup.
