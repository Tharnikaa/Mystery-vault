# Mystery Vault — Frontend Integration Guide

**Integration Branch:** `integration/all-members`  
**Platform:** Java 17+ | Swing / AWT / Java2D | Maven  
**App Entry Point:** `com.mysteryvault.MysteryVaultApplication`

---

## 1. Architecture Overview

The **Mystery Vault** desktop investigation game is built with **Java Swing, AWT, and Java 2D**, running in a single `JFrame` with `CardLayout` navigation.

The application follows the **Hexagonal Ports & Adapters Architecture**:
- **Screens (`ui.swing.screens.*Panel`)**: Pure presentation components; they never directly query SQL, repositories, or background threads.
- **Ports (`ui.swing.ports.*`)**: Interface-driven view contracts that provide data and accept user commands.
- **Production Adapters (`ui.swing.adapters.*`)**: Real service adapters that translate domain entities and service operations into view models.
- **Mock Adapters (`ui.swing.mock.*`)**: Deterministic presentation fixtures available as fallback for UI regression testing (`--demo` flag).
- **Composition Root (`MysteryVaultApplication.java`)**: Central factory that instantiates backend services and wires production adapters into `MainFrame`.

---

## 2. Screen-to-Module Integration Map

| Screen # | Screen Title | Screen Class | Presentation Port | Integrated Production Backend |
|---|---|---|---|---|
| **1** | **Main Menu** | `MainMenuPanel` | *Shell Navigation* | `FileStorageService` (save/load hooks) |
| **2** | **Detective Profile** | `DetectiveProfilePanel` | `ProfileViewPort` | `IntegratedProfileAdapter` (ranks, XP, case count from `CaseService`) |
| **3** | **Case Selection** | `CaseSelectionPanel` | `CaseViewPort` | `IntegratedCaseAdapter` (`CaseService`, Module 1) |
| **4** | **Investigation Hub** | `InvestigationHubPanel` | `CaseViewPort` | Hub navigation tile menu with dynamic case status |
| **5** | **Suspect Screen** | `SuspectPanel` | `SuspectViewPort` | `IntegratedSuspectAdapter` (`Suspect`, Module 2) |
| **6** | **Evidence Investigation** | `EvidencePanel` | `EvidenceViewPort` | `IntegratedEvidenceAdapter` (`Evidence`, `ClueUnlockEngine`, Module 3) |
| **7** | **Interrogation** | `InterrogationPanel` | `InterrogationViewPort` | `IntegratedInterrogationAdapter` (`StatementRegexExtractor`, `ContradictionDetectorService`, M6/M7) |
| **8** | **Investigation Timeline** | `TimelinePanel` | `TimelineViewPort` | `IntegratedTimelineAdapter` (`TimelineAnalyzerTask`, `DateTimeUtils`, M5) |
| **9** | **Case Connection Engine** | `ConnectionPanel` | `ConnectionViewPort` | `IntegratedConnectionAdapter` (`CustomGraphCanvas`, `Connection`, M4) |
| **10** | **Case Analysis** | `AnalysisPanel` | `AnalysisViewPort` | `IntegratedAnalysisAdapter` (`ConcurrentAnalysisManager`, M8) |
| **11** | **Final Deduction** | `DeductionPanel` | `DeductionViewPort` | `IntegratedDeductionAdapter` (`CaseSolverEngine`, M11 + `CaseService`, M1) |
| **12** | **Case Result** | `ResultPanel` | `DeductionViewPort`, `ReportViewPort` | `IntegratedReportAdapter` (`ReportGeneratorService`, M10) |

---

## 3. Composition Root & Execution Modes

In [`MysteryVaultApplication.java`](file:///f:/mystry-vault/src/main/java/com/mysteryvault/MysteryVaultApplication.java):
```java
// Production default:
MainFrame frame = MysteryVaultApplication.createMainFrame(AppMode.INTEGRATED);

// Mock demonstration / regression fallback:
MainFrame frame = MysteryVaultApplication.createMainFrame(AppMode.DEMO);
```

Pass `--demo` or `--mock` as a command-line argument to run with synthetic mock adapters.
