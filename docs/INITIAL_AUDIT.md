# MysteryVault — Initial Audit & Technical Assessment

**Date:** 2026-10-22  
**Workspace Root:** `c:\Users\varsh\OneDrive\Desktop\java prjct`  
**Java Runtime:** OpenJDK 25.0.3 (Temurin-25.0.3+9-LTS)  
**Build Tool:** Apache Maven 3.9.6  
**UI Framework:** Java Swing / AWT / Java2D  
**Database:** SQLite 3 via JDBC (`org.xerial:sqlite-jdbc:3.45.1.0`)  
**Test Framework:** JUnit Jupiter 5.10.2  

---

## 1. Environment & Workspace Status

- **Git Status:** Clean workspace. No uncommitted git writes or branch modifications permitted by user policy. Working tree has complete project files in place.
- **Java Version:** OpenJDK 25 LTS running in release 17 compatibility mode.
- **Maven Configuration:** A single root `pom.xml` with dependencies for JUnit Jupiter and SQLite JDBC. Clean compilation (`mvn test-compile`) and test suite (`mvn test`) verified.
- **Resources & Assets:** Real visual assets located in `src/main/resources/assets/` including:
  - `mysteryvault_collage.png` (reference collage)
  - Case cover artwork (`case_diamond.png`, `case_library.png`, `case_poison.png`, `case_prototype.png`)
  - Character and suspect artwork (`detective_portrait.jpg`, `suspect_arjun.jpg`, `suspect_meera.png`, `suspect_rohan.png`)
  - Evidence and menu artwork (`evidence_wineglass.png`, `menu_desk.jpg`, `reference_sheet.png`)

---

## 2. Existing Modules & Gap Analysis

| Module # | Description | Status | Gap / Required Work |
|---|---|---|---|
| **1** | Case Management | Foundation in place | Connect to SQLite case catalog and database |
| **2** | Suspect & Person | Domain models present | Expand fields (alibi, motive, occupation, suspicion) |
| **3** | Evidence & Clue Unlocking | Models and unlock engine | Database persistence and prerequisite graph |
| **4** | Relationship Graph | `CustomGraphCanvas` & `Connection` | Interactive graph traversal, path finding, BFS |
| **5** | Timeline Analysis | Viewport and basic events | Database-driven chronological sorting & conflict detection |
| **6** | Statement Regex Extraction | Needs full regex service | Extract timestamps, dates, locations, persons, facts |
| **7** | Contradiction Detection | Models present | Automated rule-based contradiction detection engine |
| **8** | Concurrent Analysis | Thread pool / Executor | Multi-task analysis engine with ExecutorService |
| **9** | Save/Load & Notes | Needs database-backed persistence | Durable SQLite persistence across app restarts |
| **10** | Report Generation | Export / Report service | Formatted plaintext & Markdown investigation dossier export |
| **11** | Final Deduction Solver | `CaseSolverEngine` | Deterministic rubric against canonical case solutions |
| **12** | Database & Diagnostics | Needs implementation | `CaseDatabaseManager`, SQLite schema migration & seeding |
| **13** | Swing Frontend (12 Screens) | 12 screen classes built | Connect all screens via production adapters |

---

## 3. Implementation Plan

1. **Database Foundation:**
   - Implement `DatabaseManager` / `CaseDatabaseManager` for SQLite.
   - Implement global catalog database (`data/mysteryvault_catalog.db`).
   - Implement per-case SQLite databases (`data/cases/case_101.db`, `case_102.db`, `case_103.db`, `case_104.db`).
   - Foreign-key enforcement, transactions, schema versioning.

2. **Case Storyline Specifications & Data Seeding:**
   - Author 4 complete original cases:
     - Case 101: *The Missing Diamond* (Museum evening exhibition)
     - Case 102: *The Library Code* (University rare archives)
     - Case 103: *The Poisoned Drink* (Private banquet dinner)
     - Case 104: *The Vanishing Prototype* (Technology robotics laboratory)
   - Store suspects, motives, alibis, evidence, clues, timeline events, interrogation Q&A, graph links, and canonical solutions.

3. **Core Domain Services & Analyzers:**
   - Statement regex parsing (`StatementRegexExtractor`).
   - Timeline conflict detector.
   - Rule-based contradiction engine.
   - Concurrent analysis manager using `ExecutorService`.
   - Case solver and deterministic scoring engine.

4. **Production Adapters & Wiring:**
   - Implement production viewports replacing mocks with real database queries.
   - Wire all 12 Swing screens to live data.

5. **Persistence, Tests & Verification:**
   - Save/load investigation state and player profile in SQLite.
   - Write comprehensive JUnit tests verifying all acceptance criteria.
