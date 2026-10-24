# Mystery Vault — API Contract Specification

**Status:** PROVISIONAL DRAFT (Pending Formal Team Review & Sign-Off)  
**Target Platform:** Java 17+ | Single Desktop Process  
**Prefix:** `com.mysteryvault`

---

## 1. Shared Identifiers & General Rules

1. **Entity Identifiers:**
   - All IDs across modules must be immutable `String` values (e.g. `"CASE_103"`, `"SUSPECT_MEERA"`, `"E01"`, `"T1"`, `"C1"`).
   - IDs must **never** be UI component references or database-specific auto-increment integers.
2. **Date & Time:**
   - All temporal values across module boundaries must use `java.time.LocalDateTime` or `java.time.LocalTime`.
3. **Decoupling:**
   - Backend modules (`model`, `service`, `analyzer`, `repository`, `util`) must **never** import `com.mysteryvault.ui.*` or `javax.swing.*`.
   - Core domain models must be pure serializable DTOs without embedded database queries or UI logic.

---

## 2. Implemented Member 4 API Contracts

### Module 4 — Connection Graph
```java
package com.mysteryvault.model;

public class Connection implements Serializable {
    public Connection(String id, String sourceId, String targetId, String relationType, boolean strongLead);
    public String getId();
    public String getSourceId();
    public String getTargetId();
    public String getRelationType();
    public boolean isStrongLead();
}
```

```java
package com.mysteryvault.ui.swing.components;

public class CustomGraphCanvas extends JComponent {
    public void setGraph(List<GraphNode> nodes, List<Connection> connections);
    public void setNodeSelectionListener(Consumer<GraphNode> listener);
    public void setSelectedNode(GraphNode node);
    public GraphNode getSelectedNode();
}
```

### Module 11 — Final Deduction / Solver Engine
```java
package com.mysteryvault.service;

public class CaseSolverEngine {
    public DeductionResult evaluate(DeductionRequest request, CaseContext caseContext);
    public void registerCaseRule(CaseSolutionRule rule);
}
```

```java
package com.mysteryvault.service.dto;

public class DeductionRequest implements Serializable {
    public DeductionRequest(String caseId, String accusedSuspectId, String motive, Set<String> supportingEvidenceIds, String reasoning);
    public String getCaseId();
    public String getAccusedSuspectId();
    public String getMotive();
    public Set<String> getSupportingEvidenceIds();
    public String getReasoning();
}

public class DeductionResult implements Serializable {
    public boolean isSolved();
    public int getScore();
    public String getVerdictTitle();
    public String getCaseTitle();
    public String getExplanation();
    public String getRecommendedCaseStatus();
    public int getEvidenceFoundCount();
    public int getTotalEvidenceCount();
    public int getImportantCluesCount();
    public int getTotalCluesCount();
    public int getContradictionsFound();
    public int getTotalContradictions();
    public int getWrongAccusations();
    public String getRankTitle();
}

public class CaseContext implements Serializable {
    public CaseContext(String caseId, String caseTitle, Set<String> discoveredEvidenceIds, Set<String> discoveredClueIds, Set<String> uncoveredContradictionIds);
    public String getCaseId();
    public String getCaseTitle();
    public Set<String> getDiscoveredEvidenceIds();
    public Set<String> getDiscoveredClueIds();
    public Set<String> getUncoveredContradictionIds();
}
```

---

## 3. Proposed Backend Contracts (From Master Prompt)

### Member 1: Case Management & Evidence (Modules 1, 2, 3)
```java
package com.mysteryvault.service;

public class CaseService {
    public List<Case> listCases();
    public Optional<Case> findById(String caseId);
    public Case createCase(Case draft);
    public Case updateCase(Case updated);
}

public class ClueUnlockEngine {
    public boolean canUnlock(Clue clue, Set<String> discoveredEvidenceIds);
    public void unlock(Clue clue, Set<String> discoveredEvidenceIds) throws ClueLockedException;
}
```

### Member 2: Timeline & Statement Analysis (Modules 5, 6, 7)
```java
package com.mysteryvault.service;

public class StatementRegexExtractor {
    public List<String> extractTimes(String statementText);
    public List<String> extractDates(String statementText);
    public List<String> extractReferences(String statementText);
}
```

### Member 3: Concurrent Analysis & Reports (Modules 8, 9, 10, 12)
```java
package com.mysteryvault.analyzer;

public interface Analyzer {
    AnalysisResult analyze(AnalysisContext context);
}

public class ConcurrentAnalysisManager {
    public AnalysisResult runAllAnalyzers(AnalysisContext context);
}
```

```java
package com.mysteryvault.service;

public class ReportGeneratorService {
    public InvestigationReport generateReport(String caseId);
}

public class FileStorageService {
    public void saveGame(String saveSlot, Object gameState) throws IOException;
    public Object loadGame(String saveSlot) throws IOException, ClassNotFoundException;
}
```

---

## 4. Pending Team Approval (Action Items for Integration Meeting)

> [!IMPORTANT]
> The following items represent provisional specifications that must be confirmed by all four members before production merging.

1. **Player Profile & XP Contract (Screen 2):**
   - **Current Status:** Implemented in frontend via `ProfileViewPort` / `MockProfileViewPort`.
   - **Open Question:** Who persists player XP, rank, and career accuracy? Member 1 or Member 3?
2. **TimelineAnalyzerTask & Analyzer Adapter (Modules 5 & 8):**
   - **Current Status:** Member 2 owns `TimelineAnalyzerTask` while Member 3 owns `Analyzer` interface.
   - **Open Question:** Confirm exact parameter and return type of `analyze(AnalysisContext)` so Member 2 and Member 3 compile without mismatch.
3. **Dialogue / Interrogation Scripting Provider (Module 6 & Screen 7):**
   - **Current Status:** Member 2 extracts dates/times from statements, while Member 4's `MockInterrogationViewPort` supplies conversational dialogue.
   - **Open Question:** How will conversational branching trees or questions be loaded from disk?
4. **Case Status Transition on Solver Completion (Modules 1 & 11):**
   - **Current Status:** `CaseSolverEngine` calculates `isSolved()` and recommends `"SOLVED"`.
   - **Open Question:** Does `CaseSolverEngine` return the recommendation to the UI, which calls `CaseService.updateCase(case)`, or will `CaseService` provide a dedicated method `solveCase(caseId, deductionResult)`?
