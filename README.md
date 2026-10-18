# Mystery Vault - Member 1 Modules (Core Domain, Repository, Services & Verification)

This repository contains **Member 1's complete implementation** (Modules 1, 2, and 3) for the **Mystery Vault** project.

## Architecture & Module Breakdown

### Module 1: Core Domain Entities & Case Lifecycle
- **[CaseStatus.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/enums/CaseStatus.java)**: Strict finite-state machine defining transitions (`OPEN` -> `ACTIVE` -> `COLD` / `SOLVED` -> `ARCHIVED`). Terminal state `ARCHIVED` cannot transition to any state.
- **[Case.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Case.java)** & **[Priority.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Priority.java)**: Case entity managing immutable ID, titles, priority, and lists of suspects, evidence, and clues.
- **Exceptions**:
  - `CaseNotFoundException`
  - `CaseValidationException`
  - `InvalidCaseTransitionException`

### Module 2: Person & Suspect Domain Model
- **[Person.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Person.java)**: Base entity encapsulating personal attributes and role.
- **[Suspect.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Suspect.java)**: Extends `Person` with occupation, age (0–150 validation), suspicion score (0–100 validation), alibi, and verified status.

### Module 3: Polymorphic Evidence Hierarchy & Clue Unlock Engine
- **[InvestigationAsset.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/InvestigationAsset.java)**: Abstract base class for all assets.
- **[Evidence.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Evidence.java)**: Abstract class extending `InvestigationAsset`, requiring polymorphic `getSummary()`.
- **[DocumentEvidence.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/DocumentEvidence.java)**: Concrete evidence class detailing document type, author, page count, classification, collected by, and location.
- **[DigitalEvidence.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/DigitalEvidence.java)**: Concrete evidence class detailing device type, file hash (SHA-256), size, and location.
- **[Clue.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/model/Clue.java)** & **[ClueLockedException.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/exception/ClueLockedException.java)**: Clue entity tracking unlock state and prerequisite evidence IDs.
- **[ClueUnlockEngine.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/service/ClueUnlockEngine.java)**: Evaluates collected evidence IDs against clue prerequisites and manages unlocks.

### Repositories & Data Services
- **[GenericRepository.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/GenericRepository.java)** & **[CaseMemoryRepository.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/repository/impl/CaseMemoryRepository.java)**: Thread-safe in-memory generic repository.
- **[CaseService.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/service/CaseService.java)**: High-level business operations for Case CRUD, lifecycle transitions, and relationship association.
- **[SampleDataFactory.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/service/SampleDataFactory.java)**: Factory producing sample cases, notably **Case #103: The Poisoned Drink** complete with 3 suspects, 3 pieces of evidence, and 2 unlockable clues.

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
1. Compiles all `src/main/java` classes to `target/classes`.
2. Compiles all `src/test/java` unit test classes to `target/test-classes`.
3. Runs the standalone reflection test runner: **[Member1TestSuite.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/test/java/com/mysteryvault/Member1TestSuite.java)** (executing all 20 JUnit test cases).
4. Launches the full verification demo: **[Member1VerificationDemo.java](file:///c:/Users/varsh/OneDrive/Desktop/2025503558/Mystery-vault/src/main/java/com/mysteryvault/Member1VerificationDemo.java)**.

---

## Test Coverage Summary

- **CaseStatusTest**: 5 tests covering all legal transitions, illegal transitions, and terminal archived states.
- **CaseServiceTest**: 7 tests covering CRUD, validations (duplicate IDs, blank titles), entity retrieval, status transitions, and Case #103 loader.
- **SuspectTest**: 3 tests covering inheritance and boundaries (age 0-150, suspicion 0-100).
- **EvidenceTest**: 2 tests covering polymorphic `getSummary()` contracts for digital and document evidence.
- **ClueUnlockEngineTest**: 3 tests covering prerequisite checking, locked exceptions, and unlocked status.
- **Total**: **20 passing tests (100% success rate)**.
