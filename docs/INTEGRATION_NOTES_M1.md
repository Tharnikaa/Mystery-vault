# Integration Notes — Member 1 Deliverables (Modules 1, 2, 3)

## Implemented Modules & Architecture

### Module 1: Case Management
- **`model.Case`**: Core investigation entity with immutable String ID, title, description, priority, lifecycle status, and relationship ID sets (`suspectIds`, `evidenceIds`, `clueIds`).
- **`enums.CaseStatus`**: State machine controlling legal transitions (`OPEN` -> `ACTIVE` -> `COLD` / `SOLVED` -> `ARCHIVED`).
- **`repository.impl.CaseJdbcRepository`**: Full JDBC persistence with transactions, prepared statements, and association synchronization.
- **`repository.impl.CaseJdbcRepositoryStub`**: Replaced stub methods throwing `UnsupportedOperationException` with active JDBC persistence delegation while preserving Member 3 compatibility.
- **`service.CaseService`**: Gateway service with ID tolerance (handling `CASE-103` and `CASE_103`), status transitions, and production database bootstrapping (`createProductionService()`).

### Module 2: Suspect & Person Profiles
- **`model.Person`**: Base individual profile entity (name, role, notes).
- **`model.Suspect`**: Extends `Person` with occupation, age (0–150), alibi, suspicion score (0–100), and alibi verification.
- **`repository.SuspectRepository`**: Interface with `findByCaseId(caseId)`, `searchByName(query)`, and case linking/unlinking methods.
- **`repository.impl.SuspectJdbcRepository`**: Production JDBC repository ensuring suspect records are scoped to cases and do not leak across case boundaries.
- **`service.SuspectService`**: Service gateway for creating, updating, searching, and case-associating suspects.

### Module 3: Evidence & Clue Management
- **`model.InvestigationAsset`**: Base asset model.
- **`model.Evidence`**: Abstract polymorphic evidence entity requiring `getSummary()`.
- **`model.DocumentEvidence`**: Paper and forensic document evidence (author, page count, classification, document type).
- **`model.DigitalEvidence`**: Forensic digital evidence (device type, SHA-256 file hash, IP address, file size).
- **`model.Clue`**: Investigation clue tracking unlocked status, timestamp, and required prerequisite evidence IDs.
- **`repository.EvidenceRepository`**: Interface distinguishing canonical case evidence from active investigation discoveries.
- **`repository.impl.EvidenceJdbcRepository`**: Production JDBC repository persisting and reconstructing polymorphic evidence.
- **`repository.ClueRepository`**: Interface managing clues and prerequisite relationships.
- **`repository.impl.ClueJdbcRepository`**: Production JDBC repository persisting clues and prerequisites in `clue_prerequisites`.
- **`repository.impl.EvidenceArchiveDao`**: Upgraded to support real database persistence in table `evidence_archive`.
- **`service.ClueUnlockEngine`**: Evaluates discovered evidence sets against prerequisite constraints and unlocks clues.
- **`service.EvidenceService`**: Service gateway for evidence, clues, and unlocking workflows.

### Infrastructure & Configuration
- **`config.DatabaseConfig`**: Reads `MYSTERYVAULT_DB_URL`, `MYSTERYVAULT_DB_USERNAME`, `MYSTERYVAULT_DB_PASSWORD`. Masks credentials in URLs and logs. Executes `SELECT 1` connectivity checks.
- **`config.DatabaseConnectionFactory`**: Manages connection pooling/vending and executes `schema.sql` migrations.
- **`exception.DatabaseConfigurationException`**: Meaningful error reports for missing or invalid database settings.
- **`exception.DataAccessException`**: Clean exception wrapping SQL exceptions without swallowing errors.

---

## APIs for Team Members

### For Member 4 (Swing UI Screens & Application Shell)
```java
// Bootstrapping
CaseService caseService = CaseService.createProductionService(); // or CaseService.createInMemoryService()
SuspectService suspectService = new SuspectService(new SuspectJdbcRepository(factory));
EvidenceService evidenceService = new EvidenceService(new EvidenceJdbcRepository(factory), new ClueJdbcRepository(factory));

// Case Gallery (Screen 3)
List<Case> availableCases = caseService.listCases();

// Suspect Screen (Screen 5)
List<Suspect> caseSuspects = suspectService.listSuspectsByCase(selectedCaseId);

// Evidence & Clue Screen (Screen 6)
List<Evidence> canonicalEvidence = evidenceService.listCanonicalEvidenceByCase(selectedCaseId);
List<Evidence> discoveredEvidence = evidenceService.listDiscoveredEvidenceByCase(selectedCaseId);
List<Clue> unlockedClues = evidenceService.listUnlockedCluesByCase(selectedCaseId);

// Status update upon final deduction (Screen 11 / CaseSolverEngine)
caseService.transitionCaseStatus(selectedCaseId, CaseStatus.SOLVED);
```

### For Member 2 (Timeline & Statements)
- Query case timeline entities with stable String case IDs (`CASE-103`).
- Reference suspects via stable IDs (`SUS-103-1`, etc.).

### For Member 3 (Analysis & Reports)
- Read Case, Suspects, Evidence, and Clues via `CaseService`, `SuspectService`, and `EvidenceService`.
- Archive reports and evidence using `EvidenceArchiveDao`.
