# Mystery Vault — Uncover the Truth
**Complete Four-Member Integrated Criminal Case Investigation Desktop Game**

---

## 🔍 Overview
**Mystery Vault – Uncover the Truth** is a cinematic desktop mystery detective game built with **Java Swing, AWT, and Java 2D** (Java 17+), managed by a single root Maven build.

This repository integrates all 13 modules built by the four-person team:
1. **Member 1 (Modules 1, 2, 3):** Canonical Case management, Suspect/Person models, polymorphic Evidence (Document & Digital), Clue entity, and prerequisite-based `ClueUnlockEngine`.
2. **Member 2 (Modules 5, 6, 7):** Chronological timeline processing (`TimelineEvent`, `DateTimeUtils`), statement regex extraction (`StatementRegexExtractor`), and contradiction conflict detection (`ContradictionDetectorService`).
3. **Member 3 (Modules 8, 9, 10, 12):** Multi-threaded concurrent analysis manager (`ConcurrentAnalysisManager`), UTF-8 file storage and serialization (`FileStorageService`), official dossier generation (`ReportGeneratorService`), and JDBC repository stub (`CaseJdbcRepositoryStub`).
4. **Member 4 (Modules 4, 11, 13):** Clue network Java2D graph visualization (`CustomGraphCanvas`), deduction solver engine (`CaseSolverEngine`), application shell, and all 12 Swing screens.

---

## 🎨 Aesthetic & Design System
- **Cinematic Noir Palette:**
  - Base Background: Deep dark slate / charcoal navy (`#0d111a`, `#090c13`)
  - Glassmorphic Cards: Translucent navy cards (`#131926`, `rgba(19, 25, 38, 0.92)`)
  - Golden Amber Accents: `#d4a359`, `#f0c47d`, `#a57834`
  - Cyan / Teal Highlights: `#19c3b8`, glowing status dots and completed milestones
  - Crimson / Warning Accents: `#e14b4b`, conflict alerts and timeline warnings
- **Java 2D Graphics:**
  - Subpixel anti-aliasing (`RenderingHints.KEY_TEXT_ANTIALIAS_ON` and `KEY_ANTIALIAS_ON`)
  - Custom rounded buttons with interactive hover/press states (`CustomButton`)
  - Interactive node network visualization (`CustomGraphCanvas`)

---

## 🖥️ The 12 Screen Panels

| # | Screen Name | Panel Class | Associated Port | Integrated Backend |
|---|---|---|---|---|
| **1** | **Main Menu** | `MainMenuPanel` | *Shell Navigation* | `FileStorageService` (Save/Load hooks) |
| **2** | **Detective Profile** | `DetectiveProfilePanel` | `ProfileViewPort` | Dynamic progression & solved cases |
| **3** | **Case Selection** | `CaseSelectionPanel` | `CaseViewPort` | `CaseService` (Module 1) |
| **4** | **Main Investigation Hub** | `InvestigationHubPanel` | `CaseViewPort` | Investigation navigation hub |
| **5** | **Suspect Screen** | `SuspectPanel` | `SuspectViewPort` | `Suspect` & `SampleDataFactory` (Module 2) |
| **6** | **Evidence Investigation** | `EvidencePanel` | `EvidenceViewPort` | `Evidence`, `ClueUnlockEngine` (Module 3) |
| **7** | **Interrogation** | `InterrogationPanel` | `InterrogationViewPort` | `StatementRegexExtractor` & Contradictions (M6 & M7) |
| **8** | **Investigation Timeline** | `TimelinePanel` | `TimelineViewPort` | `TimelineAnalyzerTask` & `DateTimeUtils` (M5) |
| **9** | **Case Connection Engine** | `ConnectionPanel` | `ConnectionViewPort` | `CustomGraphCanvas` & `Connection` (M4) |
| **10** | **Case Analysis** | `AnalysisPanel` | `AnalysisViewPort` | `ConcurrentAnalysisManager` (M8) |
| **11** | **Final Deduction** | `DeductionPanel` | `DeductionViewPort` | `CaseSolverEngine` (M11) & `CaseService` (M1) |
| **12** | **Case Result** | `ResultPanel` | `DeductionViewPort`, `ReportViewPort` | Solver Result & `ReportGeneratorService` (M10) |

---

## 🚀 Building & Testing

### Run All Unit and Integration Tests
```powershell
mvn clean test
```
All 50 unit and integration tests across 11 test suites pass with 0 failures, 0 errors, and 0 skipped.

### Package Executable JAR
```powershell
mvn clean package
```
Generates `target/mystery-vault-1.0.0.jar`.

---

## 🎮 Launching the Application

### Option 1: Running via Maven Exec
```powershell
mvn compile exec:java -Dexec.mainClass="com.mysteryvault.MysteryVaultApplication"
```

### Option 2: Running the Pre-Built JAR
```powershell
java -jar target/mystery-vault-1.0.0.jar
```

### Option 3: PowerShell Script
```powershell
.\run.ps1
```

### Optional Synthetic Demonstration Mode:
To launch using synthetic presentation mocks:
```powershell
java -jar target/mystery-vault-1.0.0.jar --demo
```

---

## ⌨️ Navigation & Hotkeys
- **In-Game Flow:** Navigate between screens seamlessly with interactive buttons.
- **Back Button (`<`):** Navigates back in history on all sub-screens.
- **Quick Jump Switcher:** Bottom dropdown selector to test any of the 12 screens directly.
- **Keyboard Shortcuts:**
  - `1` through `9`: Screens 1 to 9
  - `0`: Screen 10 (Case Analysis)
  - `-`: Screen 11 (Final Deduction)
  - `=`: Screen 12 (Case Result)
  - `Ctrl + N`: Open New Case Dialog

---

## 📚 Documentation
- **[`docs/API_CONTRACT.md`](docs/API_CONTRACT.md):** Verified signatures and module ownership mappings.
- **[`docs/INTEGRATION_DECISIONS.md`](docs/INTEGRATION_DECISIONS.md):** Architectural decisions, duplicate handling, and state machine persistence.
- **[`FRONTEND_INTEGRATION_GUIDE.md`](FRONTEND_INTEGRATION_GUIDE.md):** Hexagonal ports & adapters integration guide.
- **[`INTEGRATION_NOTES.md`](INTEGRATION_NOTES.md):** Detailed module audit and integration summary.
