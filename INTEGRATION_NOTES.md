# Mystery Vault — Integration Notes
**Author:** Member 4 (Frontend Developer and Integration Lead)  
**Branch:** `feature/member4-ui-refactor`

---

## 1. Implemented Modules Overview

### MODULE 4 — CLUE NETWORK GRAPH
- **Model:** [`com.mysteryvault.model.Connection`](src/main/java/com/mysteryvault/model/Connection.java)
  - Pure data model (serializable) storing `id`, `sourceId`, `targetId`, `relationType`, and `strongLead` flag.
  - No UI or database dependencies.
- **Renderer:** [`com.mysteryvault.ui.swing.components.CustomGraphCanvas`](src/main/java/com/mysteryvault/ui/swing/components/CustomGraphCanvas.java)
  - Java 2D graph node/edge renderer with interactive hit-testing, node selection events, glowing golden links for strong leads, and category avatars.
  - Public API:
    ```java
    void setGraph(List<GraphNode> nodes, List<Connection> connections);
    void setNodeSelectionListener(Consumer<GraphNode> listener);
    void setSelectedNode(GraphNode node);
    GraphNode getSelectedNode();
    ```
- **Interactive UI Integration:** [`com.mysteryvault.ui.swing.screens.ConnectionPanel`](src/main/java/com/mysteryvault/ui/swing/screens/ConnectionPanel.java)
  - Node selection callback updates the right sidebar entity header and related connected links in real-time.
  - **Data Safety Guaranteed:** Graph node selection strictly inspects presentation models and never alters underlying case data, nodes, or connection lists.

### MODULE 11 — FINAL DEDUCTION / CASE SOLVER
- **Service:** [`com.mysteryvault.service.CaseSolverEngine`](src/main/java/com/mysteryvault/service/CaseSolverEngine.java)
  - Evaluates player deduction submissions against case rules without accessing Swing or mutating repositories.
  - **Quality Improvements Applied:**
    1. **Strict Input Validation:** Null requests, empty/whitespace motives, blank suspect IDs, and null evidence collections throw documented `IllegalArgumentException` / `NullPointerException`.
    2. **No Fallback to Synthetic Case:** Removed the legacy fallback that evaluated unknown cases using Case #103 rules. Unknown case IDs now return an explicit unsupported-case result (`score=0`, `verdictTitle="CASE UNRESOLVED"`, `status="UNSUPPORTED"`, `rank="UNRANKED"`) or throw documented `IllegalArgumentException` via `evaluateStrict()`.
    3. **Precise Motive Matching:** Exact case-insensitive normalized matching replaces broad substring searches (e.g. `contains("financial")`), preventing false positives for motives like "Corporate blackmail", "Personal vendetta", or "Non-financial feud".
    4. **Configurable Separation:** Separated case evaluation logic from configurable rules via `CaseSolutionRule`. Synthetic Case #103 rules are retained only for default mock/demo mode.
    5. **Documented Scoring & Dynamic Stats:** Documented scoring formula (max 92 benchmark for Case #103, capped partial score at 70 for unresolved submissions). Investigation statistics are derived dynamically from `CaseContext` rather than hardcoding static numbers.
  - Public API:
    ```java
    DeductionResult evaluate(DeductionRequest request, CaseContext caseContext);
    DeductionResult evaluateStrict(DeductionRequest request, CaseContext caseContext);
    void registerCaseRule(CaseSolutionRule rule);
    boolean hasCaseRule(String caseId);
    CaseSolutionRule getCaseRule(String caseId);
    CaseSolutionRule removeCaseRule(String caseId);
    void clearCaseRules();
    ```
- **DTOs:**
  - `DeductionRequest(String caseId, String accusedSuspectId, String motive, Set<String> supportingEvidenceIds, String reasoning)`
  - `DeductionResult(boolean solved, int score, String verdictTitle, String caseTitle, String explanation, String recommendedCaseStatus, ...)`
  - `CaseContext(String caseId, String caseTitle, Set<String> discoveredEvidenceIds, Set<String> discoveredClueIds, Set<String> uncoveredContradictionIds)`

### MODULE 13 — APPLICATION SHELL / USER INTERFACE
- **Application Entry Point:** [`com.mysteryvault.MysteryVaultApplication`](src/main/java/com/mysteryvault/MysteryVaultApplication.java)
  - Authoritative production entry point and composition root.
  - Configures high-DPI scaling, antialiasing, and initializes presentation ports and mock adapters.
- **Compatibility Launcher:** [`com.mysteryvault.Main`](src/main/java/com/mysteryvault/Main.java)
  - Dedicated compatibility launcher forwarding to `MysteryVaultApplication.main(args)`.
- **Window Shell:** [`com.mysteryvault.ui.swing.MainFrame`](src/main/java/com/mysteryvault/ui/swing/MainFrame.java)
  - 1440x900 resolution with CardLayout container, ScreenManager navigation, and evaluation quick-bar.
- **All 12 Screen Panels:** Preserved under `com.mysteryvault.ui.swing.screens.*Panel`.
- **Case Result Screen:** [`com.mysteryvault.ui.swing.screens.ResultPanel`](src/main/java/com/mysteryvault/ui/swing/screens/ResultPanel.java)
  - Dynamic refresh: Listens to CardLayout `ComponentEvent.COMPONENT_SHOWN`, `HierarchyListener`, and `ScreenManager.showScreen()` calls.
  - Refreshes verdict, score pill, case title, rank banner, investigation statistics, and trophy styling from `deductionPort.getLastResult()`.
  - Submitting a wrong deduction now displays **`CASE UNRESOLVED`** in red conflict styling rather than retaining a previous `CASE SOLVED` outcome.

---

## 2. Ports & Mock Adapters Implemented

| Port Interface | Default Mock Adapter | Purpose |
|---|---|---|
| `CaseViewPort` | `MockCaseViewPort` | Case list & selection (Case #101 to #104) |
| `SuspectViewPort` | `MockSuspectViewPort` | Profiles for Arjun, Meera, Rohan |
| `EvidenceViewPort` | `MockEvidenceViewPort` | Evidence E01–E05 with examination / lab data |
| `TimelineViewPort` | `MockTimelineViewPort` | Chronological event list with conflict detection |
| `InterrogationViewPort` | `MockInterrogationViewPort` | Question asking & statement challenge |
| `ConnectionViewPort` | `MockConnectionViewPort` | Graph nodes, connections, and summaries |
| `AnalysisViewPort` | `MockAnalysisViewPort` | Progress bars and analysis metrics |
| `DeductionViewPort` | `MockDeductionViewPort` | Evaluates deduction using `CaseSolverEngine` |
| `ReportViewPort` | `MockReportViewPort` | Official police dossier report generation |
| `ProfileViewPort` | `MockProfileViewPort` | Player rank, XP, accuracy, and historical stats |

---

## 3. Required Inputs & Expected Outputs

### Correct Deduction Submission (Screen 11 -> Screen 12):
- **Input:**
  - `caseId`: `"CASE_103"`
  - `accusedSuspectId`: `"MEERA"`
  - `motive`: `"Financial dispute"`
  - `supportingEvidenceIds`: `["E01", "E03"]`
- **Output:**
  - `isSolved()`: `true`
  - `getScore()`: `92`
  - `getVerdictTitle()`: `"CASE SOLVED"`
  - `getRecommendedCaseStatus()`: `"SOLVED"`
  - `getRankTitle()`: `"MASTER DETECTIVE"`
  - `getWrongAccusations()`: `0`

### Wrong Deduction Submission (Screen 11 -> Screen 12):
- **Input:**
  - `caseId`: `"CASE_103"`
  - `accusedSuspectId`: `"ROHAN"`
  - `motive`: `"Accidental poisoning"`
  - `supportingEvidenceIds`: `["E01"]`
- **Output:**
  - `isSolved()`: `false`
  - `getScore()`: `<= 70`
  - `getVerdictTitle()`: `"CASE UNRESOLVED"`
  - `getRecommendedCaseStatus()`: `"IN_PROGRESS"`
  - `getRankTitle()`: `"SENIOR DETECTIVE"` / `"DETECTIVE INSPECTOR"`
  - `getWrongAccusations()`: `1`

---

## 4. Test Verification Summary

### Automated Test Suite (JUnit 5 + Maven Surefire)
Executed via `mvn clean test` and `./build.ps1`:
- **`CaseSolverEngineTest` (8 tests):**
  1. `testCorrectSuspectMotiveAndEvidence`: Solves Case #103 with benchmark score 92, rank "MASTER DETECTIVE", 0 wrong accusations.
  2. `testWrongSuspect`: Returns `CASE UNRESOLVED` with 1 wrong accusation and score capped at 70.
  3. `testWrongMotive`: Verifies strict motive matching against "Corporate blackmail", "Personal vendetta", and non-matching phrases.
  4. `testMissingRequiredEvidence`: Returns `CASE UNRESOLVED` when required evidence E03 is omitted.
  5. `testEmptyEvidenceSelection`: Returns `CASE UNRESOLVED` when player provides no evidence.
  6. `testUnknownCaseId`: Verifies no fallback to Case #103; returns explicit unsupported result with score 0 and status "UNSUPPORTED". Also verifies `evaluateStrict()` throws `IllegalArgumentException`.
  7. `testInvalidOrNullInputs`: Verifies rejection of null requests, null case ID, blank suspects, empty motives, and null rules.
  8. `testMultipleIndependentCases`: Configures and tests two distinct cases ("CASE_101" and "CASE_102") verifying independent evaluation with zero cross-contamination.
- **`ResultPanelTest` (3 tests):**
  1. `testResultPanelRefreshAfterConsecutiveDeductions`: Verifies that submitting a correct deduction displays `CASE SOLVED`, and a subsequent wrong deduction immediately displays `CASE UNRESOLVED` with red styling and 1 wrong accusation.
  2. `testResultPanelHandlesNullResultGracefully`: Verifies robust fallback when no deduction is recorded.
  3. `testComponentShownListenerTriggersRefresh`: Verifies Swing CardLayout component-shown event triggers automatic refresh.
- **`ConnectionPanelTest` (1 test):**
  1. `testNodeSelectionUpdatesDetailsWithoutMutatingData`: Verifies selecting nodes (MEERA, E01, ROHAN) updates the sidebar with specific connected leads without altering underlying connection or node data.

**Total Test Results:** 12 tests run, 0 failures, 0 errors, 0 skipped.

---

## 5. Current Limitations & Standalone Assumptions
- The application currently runs in **standalone mock mode** using presentation view ports so that Member 4's UI and solver engine can be demonstrated independently of other members' work.
- All investigation mock data reflects **Case #103 — The Poisoned Drink** from the design reference sheet.
- Database access (PostgreSQL/JDBC) is not required for UI launch, graph rendering, or deduction evaluation.

---

## 6. Pending Backend Dependencies (For Team Merge)
1. **Case Service (Member 1):**
   - Provide production implementation for `CaseViewPort` fetching active cases from the database.
   - Wire `CaseService.updateCaseStatus(caseId, status)` when `CaseSolverEngine` recommends `SOLVED`.
2. **Suspect & Timeline Services (Member 2):**
   - Provide production implementations for `SuspectViewPort` and `TimelineViewPort`.
   - Provide interrogation question/dialogue tree providers.
3. **Evidence & Analysis Services (Member 3):**
   - Provide production implementations for `EvidenceViewPort` and `AnalysisViewPort`.
   - Supply real graph edges for `ConnectionViewPort` extracted from forensics and interrogation notes.
