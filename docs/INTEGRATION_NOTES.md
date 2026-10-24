# MysteryVault Integration Notes

**Author:** Integration Engineering  
**Application Architecture:** Clean Architecture / Hexagonal Architecture with Dynamic Screen Lifecycle  

---

## 1. Architectural Design & Hexagonal Ports

The user interface layer is decoupled from business logic and database access via Java interfaces ("ViewPorts"). This enables full testability and modularity:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Java Swing GUI (12 Screens)                     │
│  MainMenuPanel, CaseSelectionPanel, SuspectPanel, EvidencePanel, etc.  │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ (uses ViewPorts)
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Ports (Interfaces)                 │
│  CaseViewPort, SuspectViewPort, EvidenceViewPort, TimelineViewPort,    │
│  InterrogationViewPort, ConnectionViewPort, AnalysisViewPort, etc.    │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ (implemented by)
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Integrated Adapters                             │
│  IntegratedCaseAdapter, IntegratedSuspectAdapter,                      │
│  IntegratedEvidenceAdapter, IntegratedTimelineAdapter,                 │
│  IntegratedInterrogationAdapter, IntegratedConnectionAdapter,          │
│  IntegratedAnalysisAdapter, IntegratedDeductionAdapter, etc.           │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ (queries via JDBC)
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                  Database & Domain Services Layer                      │
│  CaseDatabaseManager, CaseSolverEngine, ClueUnlockEngine,              │
│  ConcurrentAnalysisManager, ReportGeneratorService                     │
│                                                                        │
│  SQLite Databases:                                                     │
│  ├── data/mysteryvault_catalog.db                                      │
│  └── data/cases/case_101.db ... case_104.db                            │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Port-to-Adapter Mapping Matrix

| Presentation Port | Production Adapter Class | Backing Data Source / Service | Screen Panel Consumers |
|---|---|---|---|
| `CaseViewPort` | `IntegratedCaseAdapter` | `mysteryvault_catalog.db` (`cases_catalog`) | `MainMenuPanel`, `CaseSelectionPanel`, `InvestigationHubPanel` |
| `ProfileViewPort` | `IntegratedProfileAdapter` | `mysteryvault_catalog.db` (`player_profile`) | `DetectiveProfilePanel` |
| `SuspectViewPort` | `IntegratedSuspectAdapter` | `case_XXX.db` (`suspects`) | `SuspectPanel`, `InterrogationPanel` |
| `EvidenceViewPort` | `IntegratedEvidenceAdapter` | `case_XXX.db` (`evidence`, `clues`) | `EvidencePanel`, `InvestigationHubPanel` |
| `TimelineViewPort` | `IntegratedTimelineAdapter` | `case_XXX.db` (`timeline_events`) | `TimelinePanel`, `InvestigationHubPanel` |
| `InterrogationViewPort` | `IntegratedInterrogationAdapter` | `case_XXX.db` (`interrogation_dialogues`) | `InterrogationPanel` |
| `ConnectionViewPort` | `IntegratedConnectionAdapter` | `case_XXX.db` (`connections`, `evidence`, `suspects`) | `ConnectionPanel` |
| `AnalysisViewPort` | `IntegratedAnalysisAdapter` | `ConcurrentAnalysisManager` | `AnalysisPanel` |
| `DeductionViewPort` | `IntegratedDeductionAdapter` | `CaseSolverEngine`, `canonical_solution` | `DeductionPanel`, `ResultPanel` |
| `ReportViewPort` | `IntegratedReportAdapter` | `ReportGeneratorService` | `ResultPanel` |

---

## 3. Dynamic Case Switching & Screen Refresh Lifecycle

When a player selects a different case on `CaseSelectionPanel`:
1. `ScreenManager.setActiveCase(caseId)` is invoked.
2. `CaseDatabaseManager.setActiveCaseId(caseId)` updates the global active database pointer.
3. Every screen implementing `RefreshableScreen` is refreshed dynamically via `refreshData()`.
4. As a result, when navigating between Case 101, 102, 103, and 104, all suspects, evidence, timelines, network nodes, and deduction options update immediately without restarting the application.

---

## 4. Multi-Threading & Concurrency Safety

1. **Background Tasks:** `ConcurrentAnalysisManager` uses an `ExecutorService` pool with 4 daemon worker threads.
2. **Database Concurrency:** SQLite is accessed using short-lived, connection-per-task patterns inside `try-with-resources`. WAL mode is configured where applicable and transactions are safely isolated.
3. **EDT Safety:** All Swing UI updates and screen transitions are dispatched on the Event Dispatch Thread via `SwingUtilities.invokeLater(...)`.
