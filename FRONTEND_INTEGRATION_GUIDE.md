# Mystery Vault — Frontend Integration Guide
**Member 4 (Frontend Developer and Integration Lead)**

---

## 1. Frontend Architecture Overview

The **Mystery Vault** frontend is built with **Java Swing, AWT, and Java 2D**, running in a single `JFrame` with `CardLayout` navigation. 

To enable parallel development with teammates (Members 1, 2, and 3) while keeping the frontend 100% playable standalone, the UI adheres to the **Hexagonal Ports & Adapters Architecture**:
- **Screens (`ui.swing.screens.*Panel`)** contain only presentation and layout logic; they **never** access JDBC, databases, or concrete backend implementations directly.
- **Ports (`ui.swing.ports.*`)** define clean, interface-driven contracts through which screens request data or trigger user actions.
- **Mock Adapters (`ui.swing.mock.*`)** provide deterministic in-memory fixtures (featuring Case #103 *The Poisoned Drink*) for development and testing.
- **Composition Root (`MysteryVaultApplication.java`)** wires adapters to ports. During final integration, mock adapters are simply replaced by production adapters calling backend services.

---

## 2. The 12 Screen Panels

| # | Screen Title | Panel Class | Associated ViewPort | Backend Team Owner |
|---|---|---|---|---|
| **1** | **Main Menu** | `MainMenuPanel` | *None (Shell)* | **Member 3** (FileStorageService / Save & Load) |
| **2** | **Detective Profile** | `DetectiveProfilePanel` | `ProfileViewPort` | *Pending Agreement* (Player / XP Data Owner) |
| **3** | **Case Selection** | `CaseSelectionPanel` | `CaseViewPort` | **Member 1** (CaseService / Module 1) |
| **4** | **Main Investigation Hub** | `InvestigationHubPanel` | `CaseViewPort` | **Members 1, 2, 3** (Summaries & Counts) |
| **5** | **Suspect Screen** | `SuspectPanel` | `SuspectViewPort` | **Member 1** (Person/Suspect / Module 2) |
| **6** | **Evidence Investigation** | `EvidencePanel` | `EvidenceViewPort` | **Member 1** (Evidence/Clue / Module 3) |
| **7** | **Interrogation** | `InterrogationPanel` | `InterrogationViewPort`, `SuspectViewPort` | **Member 2** (Statement/Regex / Module 6) |
| **8** | **Investigation Timeline** | `TimelinePanel` | `TimelineViewPort` | **Member 2** (Timeline & Contradictions / M5 & M7) |
| **9** | **Case Connection Engine** | `ConnectionPanel` | `ConnectionViewPort` | **Member 4** (`CustomGraphCanvas` & `Connection`) |
| **10** | **Case Analysis** | `AnalysisPanel` | `AnalysisViewPort` | **Member 3** (ConcurrentAnalysisManager) & **Member 2** |
| **11** | **Final Deduction** | `DeductionPanel` | `DeductionViewPort` | **Member 4** (`CaseSolverEngine`) & **Member 1** (`CaseService`) |
| **12** | **Case Result** | `ResultPanel` | `DeductionViewPort`, `ReportViewPort` | **Member 4** (`DeductionResult`) & **Member 3** (`ReportGeneratorService`) |

---

## 3. Navigation System

- **Controller:** `com.mysteryvault.ui.swing.navigation.ScreenManager`
- **Container:** `CardLayout` inside `MainFrame.java`
- **In-Game Flow:**
  - `New Game` ➔ `Detective Profile` ➔ `Case Selection` ➔ `Investigation Hub` ➔ Sub-screens (`Suspects`, `Evidence`, `Timeline`, `Interrogation`, `Connections`, `Analysis`, `Deduction`) ➔ `Case Result` ➔ `Main Menu`.
- **Back Button (`<`):**
  - Integrated in `ScreenHeader` on all sub-screens, tracks a navigation history stack to smoothly step backwards.
- **Evaluation Quick-Bar & Hotkeys:**
  - Bottom dropdown selector lets testers jump to any of the 12 screens instantly.
  - Keyboard shortcuts: `1` to `9` (Screens 1–9), `0` (Screen 10), `-` (Screen 11), `=` (Screen 12), and `Ctrl+N` (New Case Dialog).

---

## 4. How to Connect Teammate Modules at Integration

To integrate a teammate's finished module, create a production adapter in `ui.swing.adapters` (or implement the respective port) and wire it in [`MysteryVaultApplication.java`](file:///f:/mystry-vault/src/main/java/com/mysteryvault/MysteryVaultApplication.java):

### A. Connecting Member 1 (Case, Suspect, Evidence)
1. Implement `ProductionCaseAdapter implements CaseViewPort` calling `CaseService.listCases()`.
2. Implement `ProductionSuspectAdapter implements SuspectViewPort` querying Member 1's `Suspect` entities.
3. Implement `ProductionEvidenceAdapter implements EvidenceViewPort` querying Member 1's `Evidence` entities and `ClueUnlockEngine`.
4. Replace `MockCaseViewPort`, `MockSuspectViewPort`, and `MockEvidenceViewPort` in `MysteryVaultApplication.java`.

### B. Connecting Member 2 (Timeline, Statements, Contradictions)
1. Implement `ProductionTimelineAdapter implements TimelineViewPort` calling `TimelineEvent` records and checking `Contradiction` flags from Member 2's contradiction checker.
2. Implement `ProductionInterrogationAdapter implements InterrogationViewPort` delegating dialogue text to Member 2's `StatementRegexExtractor`.
3. Replace `MockTimelineViewPort` and `MockInterrogationViewPort` in `MysteryVaultApplication.java`.

### C. Connecting Member 3 (Concurrent Analysis, Storage, Reports)
1. Implement `ProductionAnalysisAdapter implements AnalysisViewPort` that delegates to `ConcurrentAnalysisManager.runAllAnalyzers()`.
2. Implement `ProductionReportAdapter implements ReportViewPort` that calls `ReportGeneratorService.generateReport()`.
3. Wire save/load actions in `MainMenuPanel` to `FileStorageService.saveGame()` / `loadGame()`.
4. Replace `MockAnalysisViewPort` and `MockReportViewPort` in `MysteryVaultApplication.java`.

---

## 5. How to Run Frontend Independently

### Using PowerShell Scripts:
```powershell
# Compile main & test sources, run tests, and package JAR
.\build.ps1

# Run the application
.\run.ps1
```

### Using Pre-packaged JAR:
```powershell
java -jar target/mystery-vault-1.0.0.jar
```

### Using Maven:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.mysteryvault.MysteryVaultApplication"
```
