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

### MODULE 11 — FINAL DEDUCTION / CASE SOLVER
- **Service:** [`com.mysteryvault.service.CaseSolverEngine`](src/main/java/com/mysteryvault/service/CaseSolverEngine.java)
  - Evaluates player deduction submissions against case rules without accessing Swing or mutating repositories.
  - Public API:
    ```java
    DeductionResult evaluate(DeductionRequest request, CaseContext caseContext);
    void registerCaseRule(CaseSolutionRule rule);
    ```
- **DTOs:**
  - `DeductionRequest(String caseId, String accusedSuspectId, String motive, Set<String> supportingEvidenceIds, String reasoning)`
  - `DeductionResult(boolean solved, int score, String verdictTitle, String caseTitle, String explanation, String recommendedCaseStatus, ...)`
  - `CaseContext(String caseId, String caseTitle, Set<String> discoveredEvidenceIds, Set<String> discoveredClueIds, Set<String> uncoveredContradictionIds)`

### MODULE 13 — APPLICATION SHELL / USER INTERFACE
- **Application Entry Point:** [`com.mysteryvault.MysteryVaultApplication`](src/main/java/com/mysteryvault/MysteryVaultApplication.java)
  - Single production main class and composition root.
- **Window Shell:** [`com.mysteryvault.ui.swing.MainFrame`](src/main/java/com/mysteryvault/ui/swing/MainFrame.java)
  - 1440x900 resolution with CardLayout container, ScreenManager navigation, and evaluation quick-bar.
- **All 12 Screen Panels:** Under `com.mysteryvault.ui.swing.screens.*Panel`.
- **Dialogs:** [`com.mysteryvault.ui.swing.dialogs.NewCaseDialog`](src/main/java/com/mysteryvault/ui/swing/dialogs/NewCaseDialog.java)

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

### Deduction Submission (Screen 11 -> Screen 12):
- **Input:**
  - `accusedSuspectId`: `"MEERA"`
  - `motive`: `"Financial dispute"`
  - `supportingEvidenceIds`: `["E01", "E03"]`
- **Output:**
  - `isSolved()`: `true`
  - `getScore()`: `92`
  - `getVerdictTitle()`: `"CASE SOLVED"`
  - `getRecommendedCaseStatus()`: `"SOLVED"`
  - `getRankTitle()`: `"MASTER DETECTIVE"`

---

## 4. Known Limitations & Standalone Assumptions
- The application currently operates in **standalone mock mode** so that it runs without requiring unfinished backend classes from Members 1, 2, and 3.
- All investigation data reflects **Case #103 — The Poisoned Drink** from the design reference image.
- Database access (PostgreSQL/JDBC) is not required for application launch or frontend testing.

---

## 5. Unresolved API Decisions (For Team Meeting)
1. **Player Profile & XP Owner:**
   - The master 13-module assignment does not assign player XP, rank, or career statistics to any of the 4 members. Member 4 has created `ProfileViewPort` and `MockProfileViewPort`. The team needs to assign ownership or keep it in-memory.
2. **Case Status Transition Handoff:**
   - When a deduction succeeds, `CaseSolverEngine` recommends status `"SOLVED"`. The team must agree whether `CaseService.updateCase()` is called immediately or whether a dedicated `CaseService.solveCase(caseId, deductionResult)` method will be exposed.
3. **Dialogue Scripting Provider:**
   - Member 2 owns regex extraction from statements, but who authors/stores the tree of available questions and responses for each suspect during interrogation?
