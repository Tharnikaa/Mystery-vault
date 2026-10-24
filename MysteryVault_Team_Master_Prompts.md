# MYSTERY VAULT — Four-Person Master Development Prompts

**Version:** 1.0 | **Language:** Java 17+ | **Interface:** Swing/AWT + Java2D | **Build:** Maven | **App type:** One desktop Java process

> **Use this document on all four devices before anyone writes code.** The four member prompts below are designed for use with Antigravity or another coding assistant. Copy the **COMMON TEAM RULES** and your **MEMBER PROMPT** into your coding assistant. Member 4 should also attach the original 12-screen reference image. This is a project specification, not a claim that the modules already exist or compile.

## A. Final agreed division (from the last split in the shared chat)

| Member | Module numbers | Ownership | Focus |
|---|---|---|---|
| **1** | **1, 2, 3** | Case management; suspect/person profiles; evidence & clues | Domain entities and case/evidence APIs |
| **2** | **5, 6, 7** | Timeline; statement regex extraction; contradictions | Analysis data, extraction, conflict detection |
| **3** | **8, 9, 10, 12** | Concurrent analysis; notes/storage; reports; system diagnostics/JDBC stub | Infrastructure & analysis orchestration |
| **4** | **4, 11, 13** | Connection graph; final deduction/solver; application shell & Swing UI | **All 12 frontend screens**, visualisation, navigation, final deduction |

**Important correction:** Earlier messages suggest other preliminary splits and even call the clue graph “Module 14.” The definitive **13-module** list places the graph in **Module 4** and the UI in **Module 13**. Use the **last/final split above**, not the earlier tentative splits. Member 4 has 3 modules but owns the *complete* frontend, which is substantial.

## B. COMMON TEAM RULES — copy to every coding assistant

```text
PROJECT: MysteryVault, one Java 17+ desktop application.

FINAL MODULE OWNERSHIP:
Member 1 -> Modules 1,2,3: Case management; person/suspect; evidence/clues.
Member 2 -> Modules 5,6,7: Timeline; statement parsing; contradiction detection.
Member 3 -> Modules 8,9,10,12: Concurrent analysis; file storage; reporting; read-only diagnostics and JDBC stub.
Member 4 -> Modules 4,11,13: Connection graph visualization; final deduction solver; complete Swing desktop UI.

SHARED TECH STACK:
- Java 17+; standard java.*, javax.swing.*, java.awt.* APIs.
- Maven with ONE root pom.xml maintained by integration lead (Member 4); no module-specific competing build files.
- The app runs as a desktop JFrame; do not build React/HTML/JavaFX/Electron/Spring Boot.
- Keep the core playable with deterministic in-memory sample data; JDBC/PostgreSQL adapter is optional, not a startup requirement. Module 12 is read-only diagnostics, not the main UI.
- Use package prefix com.mysteryvault, with a single source root src/main/java and tests in src/test/java.
- Only one production main class: com.mysteryvault.MysteryVaultApplication (Member 4).
- Each person edits ONLY files owned by them. Never duplicate another member's model, interface, or implementation just to satisfy compilation.
- Communicate API changes before changing public method signatures. Follow the shared API contract below; when mismatches are discovered, document and agree on changes before implementing.
- No production package may import com.mysteryvault.ui.* except presentation / app composition code. Core services never refer to Swing types.
- Prefer immutable IDs (String) across module boundaries rather than circular references. Keep model objects as DTOs without UI or database operations.
- Never hardcode file-system paths, usernames, passwords, ports, or credentials. Resources load from classpath.
- Use UTF-8, clear null validation, meaningful exceptions, typed Collections, and consistent IDs.
- UI updates must run on the Swing Event Dispatch Thread. Background analysis/file/network tasks must not block the EDT.
- Do not implement other members' domain rules or quietly overwrite their files. For independent UI previews, use mocks/adapters under ui.mock and remove/rebind them at integration.
- Make deterministic tests or at minimum a self-contained demo, and document how to run it.

INTEGRATION PRACTICE:
1. Start from the shared root repository (not four unrelated projects).
2. Each member uses a feature branch: feature/member1-core, feature/member2-analysis, feature/member3-infra, feature/member4-ui.
3. Merge shared API contracts/models first; then merge dependent features in stages.
4. Before each merge: compile, run smoke checks, review duplicate files and imports.
5. The app must boot and all 12 screens must navigate using mocks BEFORE connecting backend implementations.
6. Replace mocks only at application composition root; never wire services directly inside painted UI components.

OUTPUT REQUIRED FROM EACH MEMBER:
- Owned Java source files only, with package declarations.
- A README detailing classes, public API, dependencies, demo instructions, sample data, and limitations.
- A short INTEGRATION_NOTES.md with required inputs, outputs, exceptions, and assumptions.
- Basic validation/tests without depending on another member's unfinished code.
- No fake "fully integrated" claims; document outstanding bindings precisely.
```

## C. Shared package/ownership layout

```text
MysteryVault/
  pom.xml                                   # Integration lead (Member 4)
  README.md                                 # integration lead
  src/main/java/com/mysteryvault/
    MysteryVaultApplication.java             # Member 4 (single main)
    model/
      Case.java                              # M1
      CaseStatus.java, Priority.java         # M1
      Person.java, Suspect.java              # M1
      InvestigationAsset.java                # M1
      Evidence.java, DigitalEvidence.java    # M1
      DocumentEvidence.java, Clue.java       # M1
      TimelineEvent.java, Statement.java     # M2
      Contradiction.java                     # M2
      Connection.java                        # M4 (pure graph data)
      InvestigationNote.java                 # M3
      InvestigationReport.java               # M3
    enums/Severity.java                      # M2
    exception/
      ClueLockedException.java               # M1
      ContradictionException.java            # M2
    repository/
      GenericRepository.java                 # M1
      impl/CaseMemoryRepository.java         # M1
      impl/CaseJdbcRepositoryStub.java       # M3 (stub only)
      impl/EvidenceArchiveDao.java           # M3 (stub or optional adapter)
    service/
      CaseService.java                       # M1
      ClueUnlockEngine.java                  # M1
      StatementRegexExtractor.java          # M2
      FileStorageService.java                # M3
      ReportGeneratorService.java            # M3
      CaseSolverEngine.java                  # M4
    analyzer/
      TimelineAnalyzerTask.java              # M2; coordinate adapter API with M3
      Analyzer.java                          # M3 (shared strategy interface)
      ConcurrentAnalysisManager.java         # M3
      EvidenceAnalyzerTask.java              # M3
      StatementAnalyzerTask.java             # M3
    util/
      DateTimeUtils.java, RegexUtils.java     # M2
      SerializerUtils.java                   # M3
    web/
      MysteryVaultWebServer.java             # M3
    ui/swing/
      MainFrame.java                         # M4
      screens/*.java                         # M4 (12 screens)
      components/CustomGraphCanvas.java      # M4
      components/*.java                      # M4
      dialogs/NewCaseDialog.java             # M4
      mock/*.java                            # M4 (UI previews only)
  src/main/resources/
    images/, icons/, fonts/                  # M4
  src/test/java/com/mysteryvault/            # respective module owners
  docs/
    API_CONTRACT.md                          # team-approved contract
    INTEGRATION_NOTES.md                     # aggregate integration checklist
```

**Ownership resolution:** Member 1 owns domain `Clue` (Module 3), Member 4 owns *rendering* `CustomGraphCanvas` and pure `Connection` (Module 4). Member 2 owns `TimelineAnalyzerTask` (Module 5), while Member 3 owns the cross-task `Analyzer` contract and orchestrator (Module 8). Finalize the method signatures for this adapter *before* both implementations proceed.

## D. Proposed shared API contract (team approval required before coding)

The original chat names classes but does not fully specify constructors and public method signatures. The following is a **proposed contract** to prevent integration conflicts, **not an assertion of existing APIs**. Agree to it as a team, write it in `docs/API_CONTRACT.md`, and change it only through a reviewed merge request.

### Shared identifiers and basic data

- Use `String` IDs for Case, Person/Suspect, Evidence, Clue, Statement, TimelineEvent, Connection and InvestigationNote. IDs are never UI component references.
- In-memory persistence uses the same IDs the future JDBC adapter will use.
- All events use `java.time.LocalDateTime`; pass explicit time/date values, not locale-formatted display strings.
- All user-facing results returned from services must be plain Java types/DTOs, **not Swing components**.
- Domain models should be serializable as needed, with explicit `serialVersionUID`; do not serialize `JFrame`, `JPanel` or service objects.

### Suggested service-facing methods (illustrative signatures)

```java
// M1: service.CaseService
List<Case> listCases();
Optional<Case> findById(String caseId);
Case createCase(Case draft);             // validate required fields/unique IDs
Case updateCase(Case updated);

// M1: service.ClueUnlockEngine
boolean canUnlock(Clue clue, Set<String> discoveredEvidenceIds);
void unlock(Clue clue, Set<String> discoveredEvidenceIds) throws ClueLockedException;

// M2: service.StatementRegexExtractor
List<String> extractTimes(String statementText);
List<String> extractDates(String statementText);
List<String> extractReferences(String statementText);

// M3: analyzer.Analyzer — decide the precise return type together.
// RECOMMENDATION: prefer immutable AnalysisResult rather than side effects;
// M2 TimelineAnalyzerTask must implement this agreed interface, not another one.
AnalysisResult analyze(AnalysisContext context);

// M4: service.CaseSolverEngine — final deduction contract to be agreed.
DeductionResult evaluate(DeductionRequest request, CaseContext caseContext);

// M4: ui.swing.components.CustomGraphCanvas
void setGraph(List<GraphNode> nodes, List<Connection> connections);
// graph models must be plain Java data, no Swing in model package
```

`AnalysisContext`, `AnalysisResult`, `DeductionRequest`, `DeductionResult`, `CaseContext` and `GraphNode` are **proposed shared DTO names, not existing classes**. Choose their precise fields, owners and package locations in the shared contract before any feature depends on them. Do not let four coding assistants independently invent incompatible versions. For UI previews, Member 4 may instead use private `ui.mock` view models until agreed service APIs exist.

### UI adapters (Member 4 should define interfaces, but not implement domain logic)

```text
CaseViewPort           -> case listing/selection/progress
SuspectViewPort        -> suspect profiles
EvidenceViewPort       -> evidence/clue browsing
TimelineViewPort       -> events and conflicts
InterrogationViewPort  -> statements and selectable questions
ConnectionViewPort     -> graph nodes + edges
AnalysisViewPort       -> analysis progress / summary
DeductionViewPort      -> validate submitted accusation + return results
ReportViewPort         -> export/report display
```

All ports should be read-only data delivery where possible; actions like submitting a deduction or changing case status must call dedicated service methods. During UI development, provide deterministic `ui.mock.*` adapters. At integration, make production adapters that translate service outputs into view models, wired once in the app entrypoint.

## E. MEMBER 1 — copy/paste prompt (Modules 1, 2, 3)

```text
You are Member 1 of a 4-person Java 17+ project named MysteryVault. Read and obey the COMMON TEAM RULES and approved API_CONTRACT.md provided to you. Only implement Modules 1,2,3. Another member owns all Swing UI; do not write any UI classes.

MODULE 1 — CASE MANAGEMENT:
- Implement model.Case, CaseStatus, Priority.
- Implement repository.GenericRepository<T, ID>, repository.impl.CaseMemoryRepository, service.CaseService.
- CRUD, validation, legal status transitions, ID-based lookup and predictable sample case data.
- CaseService is the sole gateway through which other modules access the case repository.
- Document the allowed statuses, status-transition rules and error handling.

MODULE 2 — SUSPECT & PERSON:
- Implement model.Person and model.Suspect extends Person.
- Encapsulate names/roles and suspect-specific alibi, suspicion score, occupation, age as agreed.
- Keep the models free of UI, SQL and circular Case/Evidence references; use String IDs for relationships.

MODULE 3 — EVIDENCE & CLUES:
- Implement serializable base model.InvestigationAsset and abstract model.Evidence.
- Implement model.DigitalEvidence and model.DocumentEvidence with polymorphic getSummary().
- Implement model.Clue, service.ClueUnlockEngine, exception.ClueLockedException.
- Deterministic clue unlocking based on discovered evidence ID prerequisites.
- Keep evidence and clue objects data-focused; no Swing or DB access.

INTEGRATION CONTRACT:
- Match the shared API for CaseService, IDs, model constructors and ClueUnlockEngine.
- Confirm class/member names with Members 2,3,4 before freezing the API.
- Member 4 needs cases/suspects/evidence/clues via service methods or view adapters.
- Never create another implementation of Connection, TimelineEvent or CaseSolverEngine.

DELIVER:
Owned source files, unit/demo tests for CRUD and clues, mock sample data for Case #103 'The Poisoned Drink' (avoid inventing 'canon' facts; label sample), README and INTEGRATION_NOTES.md. Ensure no Swing imports.
```

## F. MEMBER 2 — copy/paste prompt (Modules 5, 6, 7)

```text
You are Member 2 of a 4-person Java 17+ project named MysteryVault. Read COMMON TEAM RULES and approved API_CONTRACT.md. Implement only Modules 5,6,7. All output should be plain Java data; no Swing UI or direct JDBC access.

MODULE 5 — TIMELINE:
- Implement model.TimelineEvent with String id/caseId and LocalDateTime timestamp.
- Implement util.DateTimeUtils with pure static temporal comparison, ordering, gap/overlap helpers.
- Implement analyzer.TimelineAnalyzerTask; coordinate with Member 3 so it conforms to the shared analyzer.Analyzer contract rather than creating a competing Analyzer interface.
- Provide deterministic timeline ordering/conflict samples and tests.

MODULE 6 — STATEMENTS & REGEX:
- Implement model.Statement, util.RegexUtils, service.StatementRegexExtractor.
- Extract structured dates, times, IDs and locations from statement text with java.util.regex.
- Regex results must be clearly defined (e.g. values + source snippet) and handle missing/invalid fields gracefully.
- No UI-specific string formatting inside core extraction logic.

MODULE 7 — CONTRADICTIONS:
- Implement model.Contradiction, enums.Severity, exception.ContradictionException and contradiction checking logic in a service owned by this module as approved by the team.
- Compare passed statements and timeline/evidence facts without importing CaseService, Swing or repositories.
- Return structured contradictions with supporting source IDs and severity; never mark a case solved.
- Ensure demo false-positive/true-positive examples are testable.

INTEGRATION CONTRACT:
- Coordinate TimelineAnalyzerTask with Member 3 before starting.
- Use Members 1 and 3 types only through the shared, agreed contracts.
- Member 4 needs timeline entries, parsed interrogation statements and contradiction results for screens 7,8,9,10.
- Never recreate Case, Evidence, Person, or Connection.

DELIVER:
Owned source files, regex/timeline/contradiction tests, README, integration note with example inputs/outputs, and a pure Java demo that does not require an actual GUI or database.
```

## G. MEMBER 3 — copy/paste prompt (Modules 8, 9, 10, 12)

```text
You are Member 3 of a 4-person Java 17+ project named MysteryVault. Read COMMON TEAM RULES and approved API_CONTRACT.md. Implement only Modules 8,9,10,12, without modifying frontend screens or domain classes owned by other members.

MODULE 8 — CONCURRENT ANALYSIS:
- Own analyzer.Analyzer and coordinate its exact method signature with Member 2, who implements TimelineAnalyzerTask.
- Implement analyzer.ConcurrentAnalysisManager, EvidenceAnalyzerTask, StatementAnalyzerTask.
- Run independent analyzers concurrently with proper exception capture and controlled coordination (Thread/join or ExecutorService as team agrees).
- Return aggregated structured results; no Swing calls from worker threads.
- Do not put shared mutable collections in unsynchronized access paths.

MODULE 9 — NOTES / STORAGE:
- Implement model.InvestigationNote, service.FileStorageService, util.SerializerUtils.
- Implement UTF-8 text notes and Serializable-based save/load, with version or schema considerations.
- Use try-with-resources; surface IOException without freezing/altering UI.
- Files live under a configurable application-data directory, never a hardcoded personal path.

MODULE 10 — REPORTS:
- Implement model.InvestigationReport and service.ReportGeneratorService.
- Build clear text report output from provided finished domain data/analysis, without rerunning analyzers.
- Include case summary, suspects, timeline, selected evidence, contradictions, deduction and score when available.
- Give Member 4 a simple report API for the Case Result -> Case Report button.

MODULE 12 — DIAGNOSTICS / JDBC STUB:
- Implement web.MysteryVaultWebServer as optional read-only diagnostics using JDK HttpServer (no servlet container required).
- Implement repository.impl.CaseJdbcRepositoryStub and EvidenceArchiveDao as explicit stubs/interfaces or optional adapters; do not pretend JDBC is functioning.
- Never require Postgres or diagnostics server for desktop app startup. Keep server disabled by default and avoid leaking personal data.
- Do not create a competing CaseService or production Main method.

INTEGRATION CONTRACT:
- Agree exact analyzer DTO contract with Members 1 and 2 before coding.
- Deliver reports and analyses as plain Java data for Member 4's UI adapters.
- Clarify what needs persistence and what remains mock-only. Do not modify Members 1,2,4 owned source files.

DELIVER:
Owned sources, concurrency/storage/report demos, README, INTEGRATION_NOTES.md, instructions for optional diagnostics, and a documented JDBC future-adapter plan.
```

## H. MEMBER 4 (YOU) — copy/paste prompt (Modules 4, 11, 13 + every screen)

**Attach the original 12-screen reference image with this prompt.**

```text
You are Member 4 of a 4-person Java 17+ project named MysteryVault. Read COMMON TEAM RULES and approved API_CONTRACT.md. I own Modules 4,11,13, INCLUDING THE ENTIRE 12-SCREEN SWING FRONTEND. I will attach one master image showing the 12 target screens. The image is the authoritative visual reference: match its composition, layouts, component proportions, cinematic dark navy/charcoal/gold theme, typography, details and button positions as closely as Swing/Java2D permits. Do not redesign it as a web dashboard.

SCOPE NOW: Implement polished UI and mock adapters first. Implement Module 11's solver contract separately, but DO NOT guess the real answer to the mystery or hardcode canon scoring. UI demos may use explicitly labeled sample output. Other team members are independently building backend modules; integration happens later.

MODULE 13 — APPLICATION SHELL / COMPLETE UI:
- Own com.mysteryvault.MysteryVaultApplication (the ONLY app main), ui.swing.MainFrame, ui.swing.dialogs.NewCaseDialog, and ALL 12 screen panels.
- Use a single JFrame and CardLayout for navigation; custom Swing components and Java2D for premium game-like styling.
- Build the EXACT 12 screens identified by the image: (1) Main Menu, (2) Detective Profile, (3) Case Selection, (4) Main Investigation Hub, (5) Suspect Screen, (6) Evidence Investigation, (7) Interrogation, (8) Investigation Timeline, (9) Case Connection Engine, (10) Case Analysis, (11) Final Deduction, (12) Case Result.
- Main Menu includes New Game/Load Game/Settings/Quit; profile includes progress/XP; case selection has illustrated cards; hub is a tile menu; suspects have portrait cards; evidence screen is list + large preview; interrogation has transcript + input; timeline is chronological with conflict indicator; connection screen has a graph and links panel; analysis has progress bars and results; deduction allows suspect/motive/evidence selection; results show score/rank/report/menu.
- Make navigation, Back, case selection, card selection, evidence selection, dialog openings and mock state work reliably; avoid dead buttons. Show informative placeholders for unimplemented backend features instead of falsely claiming they work.
- Load image assets from src/main/resources. Do NOT use the original collage as one background image. All functional elements must be real Swing components.
- Responsive layout for 1440x900 preferred; minimum 1200x750; support resizing without overlapping/clipping. Use Swing Timer for UI animation only; don't block EDT.
- Structure panels so they consume interface-based view data rather than directly reading repositories or invoking DB/network calls.
- Create UI-only deterministic mock data in ui.mock, and a small set of UI view models/ports; other members must not depend on ui.mock.

MODULE 4 — CLUE NETWORK GRAPH:
- Own pure-data model.Connection (fields defined in shared contract, e.g., node IDs and relation type) plus ui.swing.components.CustomGraphCanvas.
- Implement Graphics2D nodes/edges, selectable/highlighted nodes, labels, clickable selection; renderer only draws given data and emits selection events.
- Graph must NOT infer guilt, mutate cases, query DB, or import analyzer classes. Provide setGraph(...) / selection callback API.

MODULE 11 — FINAL DEDUCTION / SOLVER:
- Own service.CaseSolverEngine and plain DTOs for DeductionRequest/DeductionResult only after team agrees package/fields in API_CONTRACT.md.
- Input = chosen suspect ID, motive, selected evidence IDs, case context. Output = verdict, explanation, score and status recommendation.
- Only CaseService should commit/persist case status, via its validated transition API. Clarify orchestration rule with Member 1; do not directly set Case.status or write repositories from solver.
- Provide deterministic unit tests with synthetic fixtures. Avoid showing an incorrect-canonical 'CASE SOLVED' claim in a live case if deduction isn't correct.

INTEGRATION SEAMS:
- Implement GUI presentation ports: CaseViewPort, SuspectViewPort, EvidenceViewPort, TimelineViewPort, InterrogationViewPort, ConnectionViewPort, AnalysisViewPort, DeductionViewPort, ReportViewPort. Precise signatures must follow team-approved contract.
- Build mock implementations in ui.mock so the app compiles with NO backend, database or diagnostics server.
- At the composition root, wire production adapters later: Member 1 -> cases/suspects/evidence/clues; Member 2 -> statements/timeline/contradictions; Member 3 -> analysis/storage/reports; Member 4 -> graph/solver.
- No Swing class is imported by any backend module; do not modify source files owned by other members.
- Ensure all UI event handlers validate inputs, show errors gracefully, and delegate long-running work outside EDT.

DELIVERABLES:
1) Maven-ready full frontend code and resources for all 12 screens.
2) Reusable theme/components, screen navigation, mock data and UI ports.
3) Module 4 graph model/renderer; Module 11 solver contract + synthetic tests.
4) README with build/run steps, class map and image assets used.
5) INTEGRATION_NOTES.md listing exactly which production service adapter is needed per screen.
6) Run app, verify every route and dialog, fix errors, do not call it integrated until production adapters are merged.

DEVELOPMENT ORDER: Project structure/theme -> screens 1-4 -> screens 5-8 -> screens 9-12 -> graph interactions -> solver mock contract -> smoke tests -> API adapter handoff. Always prioritize matching attached image over making a new design.
```

## I. Screen-to-module dependency matrix (for integration)

| UI screen | Member 4 provides | Backend owner to connect later |
|---|---|---|
| 1 Main Menu | navigation, Load/Settings UI | M3 file save/load (M9) |
| 2 Detective Profile | visual profile, XP display | agreed shared player/profile data owner (not specified in original module list) |
| 3 Case Selection | case card gallery | M1 CaseService (M1) |
| 4 Investigation Hub | grid navigation + counters | M1 data; M2 conflict counts; M3 analysis |
| 5 Suspect Screen | cards / detail / selection | M1 Person/Suspect (M2) |
| 6 Evidence | evidence list, preview, actions | M1 Evidence/Clue (M3) |
| 7 Interrogation | conversation UI / selected suspect | M2 Statement/Regex (M6); content/questions need a separate agreed provider |
| 8 Timeline | timeline visual | M2 timeline + conflicts (M5/M7) |
| 9 Connection Engine | CustomGraphCanvas | M4 Connection data; sources M1/M2 |
| 10 Case Analysis | progress/results UI | M3 analysis manager (M8), M2 conflicts |
| 11 Final Deduction | form, validation, request UI | M4 CaseSolverEngine (M11), M1 CaseService for approved status change |
| 12 Case Result | result, score, report action | M4 solver result; M3 report (M10) |

**Unassigned domain details:** The 13-module list does **not** explicitly assign Player/XP/profile management, a canonical case content author, suspect dialogue scripting, or SCORE/INVESTIGATION SQL tables. Before integration, the team must agree the owning member, schema and public contract for these. Keep them in Member 4's `ui.mock` only until resolved; do not silently invent production storage responsibilities.

## J. Integration checklist and merge order

1. **Agreement meeting:** Save the 13-module assignment and approved Java DTO/API signatures into `docs/API_CONTRACT.md`. Resolve Analyzer interface/TimelineAnalyzerTask pairing, graph Connection type, case-status ownership and unassigned Player/score concepts.
2. **Repo setup:** One Git repo, Java 17+ Maven wrapper or shared Maven installation, one `pom.xml`, one source tree, separate feature branches. Avoid zip-copy overwrite merges.
3. **First merge:** Member 1 base models/CaseService plus Member 2 timeline/statement models and approved shared DTOs. Resolve compilation before subsequent merges.
4. **Second merge:** Member 3 analyzers, storage and reports; attach Member 2 TimelineAnalyzerTask to Member 3 Analyzer via agreed signature; keep diagnostics disabled.
5. **Third merge:** Member 4 graph, solver and all UI panels, initially wired to mocks. UI must run independently of JDBC.
6. **Adapter integration:** Replace UI mocks one at a time at the composition root. Establish adapters for Case, Suspect, Evidence, Timeline, Statement, Graph, Analysis, Deduction, Report.
7. **State behavior:** Only CaseService (Member 1) persists validated status changes; CaseSolverEngine (Member 4) computes a deduction result. Ensure double submission/duplicate scoring is handled.
8. **Testing:** Case #103 opens; suspect/evidence/timeline render; graph draws nodes; analysis runs without freezing UI; deduction handles wrong/correct outcomes; report creates valid output; save/load works; errors are displayed without crashing.
9. **Optional services:** Enable diagnostics only when requested; JDBC adapter added behind existing service/repository contracts without changing Swing panels.
10. **Final checks:** `mvn clean test` and `mvn package` pass; launch entrypoint; check no duplicate class names, hardcoded paths, merge conflicts, model/view cyclic imports or UI calls from background threads.

## K. A short message each teammate should send back after implementation

```text
Member number:
Implemented module numbers:
Files/classes created:
Public methods / DTO signatures:
Assumptions / deviations from agreed API:
Dependencies on other members:
Mock vs complete implementations:
How to run tests/demo:
What is NOT implemented yet:
Git branch / commit ID:
```

**Next task for the team:** Approve the shared API contract before development. Without that step, prompts can reduce integration risk but cannot guarantee trouble-free integration.
