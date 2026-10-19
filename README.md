# Mystery Vault - Member 1 Modules (Core Domain, Database Persistence & Services)

This repository contains **Member 1's complete implementation** (Modules 1, 2, and 3) for the **Mystery Vault** project.

---

## Database Configuration

Mystery Vault uses **PostgreSQL 14+** in production, configured via environment variables:

| Environment Variable | Default Value | Description |
|----------------------|---------------|-------------|
| `MYSTERYVAULT_DB_URL` | `jdbc:postgresql://localhost:5432/mysteryvault` | PostgreSQL JDBC connection URL |
| `MYSTERYVAULT_DB_USERNAME` | `postgres` | Database username |
| `MYSTERYVAULT_DB_PASSWORD` | *(empty string)* | Database user password |

- **Schema Initialization**: Automated via `schema.sql` (located at `src/main/resources/schema.sql`).
- **Connectivity Check**: Proactively verifies database connectivity using `SELECT 1`.
- **Security**: Sensitive credentials and passwords in connection strings are masked across logs and exceptions.
- **Test Isolation**: In-memory test doubles (`CaseMemoryRepository`, `SuspectMemoryRepository`, etc.) and disposable H2 database fixtures ensure unit and integration tests run deterministically without requiring an active PostgreSQL daemon.

See [DATABASE_SETUP.md](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/docs/DATABASE_SETUP.md) for full setup instructions.

---

## Architecture & Module Breakdown

### Module 1: Case Management & Lifecycle
- **[CaseStatus.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/enums/CaseStatus.java)**: Finite-state machine defining legal lifecycle transitions (`OPEN` -> `ACTIVE` -> `COLD` / `SOLVED` -> `ARCHIVED`). Terminal state `ARCHIVED` cannot transition to any state.
- **[Case.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Case.java)** & **[Priority.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Priority.java)**: Case entity managing immutable ID, title, description, priority, and relational ID sets (`suspectIds`, `evidenceIds`, `clueIds`).
- **[CaseJdbcRepository.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/impl/CaseJdbcRepository.java)**: Real PostgreSQL/ANSI SQL persistence with prepared statements and transactional relationship synchronization.
- **[CaseJdbcRepositoryStub.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/impl/CaseJdbcRepositoryStub.java)**: Upgraded from unactivated stub to full JDBC persistence delegation.
- **[CaseService.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/service/CaseService.java)**: Service gateway with ID delimiter tolerance (e.g. `CASE-103` vs `CASE_103`), status transitions, and production database bootstrap.

### Module 2: Person & Suspect Domain Model
- **[Person.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Person.java)**: Base individual profile entity (name, role, notes).
- **[Suspect.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Suspect.java)**: Extends `Person` with occupation, age (0–150 validation), suspicion score (0–100 validation), alibi, and verified status.
- **[SuspectRepository.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/SuspectRepository.java)** & **[SuspectJdbcRepository.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/impl/SuspectJdbcRepository.java)**: Database-backed suspect profile persistence scoping suspects to cases and preventing cross-case leakage.
- **[SuspectService.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/service/SuspectService.java)**: Business operations for creating, updating, searching, and case-associating suspects.

### Module 3: Polymorphic Evidence Hierarchy & Clue Unlock Engine
- **[InvestigationAsset.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/InvestigationAsset.java)**: Abstract base class for all assets.
- **[Evidence.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Evidence.java)**: Abstract polymorphic evidence entity requiring `getSummary()`.
- **[DocumentEvidence.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/DocumentEvidence.java)**: Paper/report evidence with document type, author, page count, and classification.
- **[DigitalEvidence.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/DigitalEvidence.java)**: Digital forensic evidence with device type, SHA-256 hash, IP address, and file size.
- **[Clue.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Clue.java)** & **[ClueLockedException.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/exception/ClueLockedException.java)**: Clue tracking unlock state and prerequisite evidence IDs.
- **[EvidenceJdbcRepository.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/impl/EvidenceJdbcRepository.java)**: Persists polymorphic evidence and maintains distinction between canonical case evidence and active investigation discoveries.
- **[ClueJdbcRepository.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/impl/ClueJdbcRepository.java)**: Persists clues and prerequisites in table `clue_prerequisites`.
- **[EvidenceArchiveDao.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/impl/EvidenceArchiveDao.java)**: Persistent evidence archive backed by `evidence_archive` table.
- **[ClueUnlockEngine.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/service/ClueUnlockEngine.java)**: Evaluates collected evidence against clue prerequisites and unlocks clues.
- **[EvidenceService.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/service/EvidenceService.java)**: Service gateway managing evidence, clues, and unlocking workflows.

---

## Running and Verifying

### Quick Run
Execute the batch file in Windows Command Prompt:
```cmd
run.bat
```
Or execute PowerShell:
```powershell
.\build.ps1
```

### What `build.ps1` does:
1. Compiles all `src/main/java` classes with PostgreSQL driver to `target/classes`.
2. Compiles all `src/test/java` unit and integration test classes to `target/test-classes`.
3. Runs **[Member1TestSuite.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/test/java/com/mysteryvault/Member1TestSuite.java)** (executing all 41 test cases).
4. Launches the full verification demo: **[Member1VerificationDemo.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/Member1VerificationDemo.java)**.

---

## Test Coverage Summary

- **CaseStatusTest**: 5 tests covering all legal transitions, illegal transitions, and terminal archived states.
- **CaseServiceTest**: 7 tests covering CRUD, validations (duplicate IDs, blank titles), entity retrieval, status transitions, and Case #103 loader.
- **SuspectTest**: 3 tests covering inheritance and boundaries (age 0-150, suspicion 0-100).
- **EvidenceTest**: 2 tests covering polymorphic `getSummary()` contracts for digital and document evidence.
- **ClueUnlockEngineTest**: 3 tests covering prerequisite checking, locked exceptions, and unlocked status.
- **CaseJdbcRepositoryTest**: 6 tests covering real SQL CRUD, updates, associations, and persistence across connection restarts.
- **SuspectJdbcRepositoryTest**: 4 tests covering profile persistence, updates, search, and strict case isolation (no cross-case leakage).
- **EvidenceJdbcRepositoryTest**: 5 tests covering polymorphic document/digital evidence, canonical vs discovered separation, clue unlocking, and evidence archiving.
- **DatabaseConfigTest**: 5 tests covering environment parsing, secret masking in URLs/logs, SELECT 1 connectivity check, and descriptive error handling.
- **PostgreSqlIntegrationTest**: 1 test validating live PostgreSQL database integration when running.
- **Total**: **41 passing tests (100% success rate)**.
