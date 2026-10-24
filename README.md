# MysteryVault: Uncover the Truth
**Complete Desktop Detective Investigation Game**

MysteryVault is an immersive desktop detective game built with **Java 17+, Java Swing, AWT, Java2D, and SQLite via JDBC**. Players analyze evidence, interrogate suspects, reconstruct timelines, identify contradictions, discover connections on an interactive clue network graph, and formulate final deduction theories with rigorous benchmark scoring.

---

## 🔍 Features & Architecture Overview

- **100% Native Java Desktop Architecture:** Zero web frameworks, zero Spring, zero HTML/React/Python/JavaFX. Pure Java Swing & Java2D graphics with custom dark noir styling and smooth anti-aliased rendering.
- **Isolated SQLite Storage per Case:**
  - `data/mysteryvault_catalog.db`: Global case directory, player profile (XP, rank, accuracy, best score), and global saves.
  - `data/cases/case_101.db`: Case #101 — *The Missing Diamond* (Marcus Vance heist).
  - `data/cases/case_102.db`: Case #102 — *The Library Code* (Dr. Harrison Vance cipher fraud).
  - `data/cases/case_103.db`: Case #103 — *The Poisoned Drink* (Vivienne Sinclair gala poisoning).
  - `data/cases/case_104.db`: Case #104 — *The Vanishing Prototype* (Dr. Elena Rostova corporate espionage).
- **Automated Database Seeding:** Clean programmatic schema migration and storyline population via Java JDBC. No external SQL scripts or manual DB operations required.
- **Multi-threaded Background Analytics:** `ConcurrentAnalysisManager` coordinates parallel analysis tasks (`EvidenceAnalyzerTask`, `TimelineAnalyzerTask`, `StatementAnalyzerTask`) via `ExecutorService`.
- **Deterministic Clue & Deduction Engine:** Prerequisite dependency validation (`ClueUnlockEngine`) and strict scoring rubrics (`CaseSolverEngine`) enforcing accurate suspect, motive, and decisive evidence matching.
- **Self-Contained Executable Fat JAR:** Includes SQLite JDBC and SLF4J, packaged with `maven-shade-plugin`.

---

## 🖥️ The 12 Screen Panels

| # | Screen Name | Panel Class | Functionality |
|---|---|---|---|
| **1** | **Main Menu** | `MainMenuPanel` | Banner collage, Resume Investigation, Select Case, View Dossier, Exit. |
| **2** | **Detective Profile** | `DetectiveProfilePanel` | Detective badge, XP, rank progression, accuracy, case statistics. |
| **3** | **Case Selection** | `CaseSelectionPanel` | Grid of 4 cases with difficulty badges, summaries, and launch triggers. |
| **4** | **Investigation Hub** | `InvestigationHubPanel` | Case mission overview, recent timeline alert, quick action cards. |
| **5** | **Suspect Screen** | `SuspectPanel` | Suspect cards, alibi statements, suspicion levels, portraits. |
| **6** | **Evidence Investigation** | `EvidencePanel` | Physical & digital evidence catalog, location traces, examination notes. |
| **7** | **Interrogation** | `InterrogationPanel` | Interactive suspect dialog, live question prompts, contradiction detection. |
| **8** | **Timeline** | `TimelinePanel` | Chronological event list, timestamped actions, conflict highlights. |
| **9** | **Clue Network Graph** | `ConnectionPanel` | Interactive Java2D physics node graph linking suspects, evidence, and clues. |
| **10** | **Case Analysis** | `AnalysisPanel` | Real-time concurrent progress meters across evidence, timeline, statements. |
| **11** | **Final Deduction** | `DeductionPanel` | Accusation assembly: select suspect, motive, supporting evidence, and submit. |
| **12** | **Case Result** | `ResultPanel` | Verdict outcome (SOLVED / UNRESOLVED), score breakdown, rank, dossier export. |

---

## 🚀 Prerequisites & Build Instructions

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or newer (Tested on OpenJDK 25).
- **Apache Maven:** Version 3.8+ (Automatically resolved via `build.ps1` / `run.ps1` if installed locally).

### 1. Build and Test
Run Maven to compile all 82 source files and execute all 39 JUnit 5 test cases:
```powershell
mvn test
```

### 2. Package Executable Fat JAR
Build the standalone shaded JAR:
```powershell
mvn package -DskipTests
```
The output file is generated at `target/mystery-vault-1.0.0.jar`.

### 3. Launch the Application
Run using the universal PowerShell launcher:
```powershell
.\run.ps1
```
Or run directly via `java -jar`:
```powershell
java -jar target/mystery-vault-1.0.0.jar
```
Or double-click:
```cmd
run.bat
```

---

## 🧪 Automated Test Suite

MysteryVault includes 39 automated unit and integration tests covering all critical engine layers:

| Test Class | Focus Area | Tests | Status |
|---|---|---|---|
| `CaseDatabaseManagerTest` | DB creation, foreign key isolation, safe re-initialization | 5 | PASSED |
| `ClueUnlockEngineTest` | Clue unlock prerequisite validation & DB updates | 5 | PASSED |
| `StatementRegexExtractorTest` | Regex time/date/location parsing from statements | 4 | PASSED |
| `ConcurrentAnalysisManagerTest` | Parallel analytical tasks via `ExecutorService` | 2 | PASSED |
| `CaseSolverEngineTest` | Deduction scoring rubric, suspect/motive validation | 8 | PASSED |
| `ReportGeneratorServiceTest` | Official dossier compilation & file export | 2 | PASSED |
| `IntegratedAdaptersTest` | All 10 Swing adapters verified against SQLite databases | 9 | PASSED |
| `ConnectionPanelTest` | Graph canvas rendering & relationship metrics | 1 | PASSED |
| `ResultPanelTest` | Case results & verdict presentation | 3 | PASSED |
| **Total** | | **39** | **100% Pass** |

---

## 🎮 Navigation & Game Controls

- **Intuitive In-Game Flow:** Buttons on each screen guide players naturally (e.g., *Inspect Evidence* ➔ *Examine* ➔ *Interrogate* ➔ *Deduce*).
- **Navigation Bar & Back Button (`<`):** Navigate backward through screen history.
- **Quick-Jump Switcher:** A discrete bottom toolbar allows instant switching between any of the 12 screens during evaluation or review.
- **Keyboard Shortcuts:**
  - Keys `1` through `9`: Screens 1–9
  - Key `0`: Screen 10 (Analysis)
  - Key `-`: Screen 11 (Deduction)
  - Key `=`: Screen 12 (Result)
  - `Ctrl + N`: New Case Dialog
