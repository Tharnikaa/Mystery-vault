package com.mysteryvault.service;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.exception.CaseNotFoundException;
import com.mysteryvault.exception.CaseValidationException;
import com.mysteryvault.exception.InvalidCaseTransitionException;
import com.mysteryvault.model.Case;
import com.mysteryvault.model.Priority;
import com.mysteryvault.repository.GenericRepository;
import com.mysteryvault.repository.impl.CaseMemoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service gateway for Case Management (Module 1).
 * Serves as the sole entry point for creating, retrieving, updating,
 * and transitioning cases within the repository.
 *
 * Enforces:
 * - Title and ID uniqueness and non-blank validation
 * - State machine transition validation rules
 * - Defensive copying and decoupled access
 */
public class CaseService {

    private final GenericRepository<Case, String> caseRepository;

    /**
     * Constructs CaseService with default in-memory repository.
     */
    public CaseService() {
        this(new CaseMemoryRepository());
    }

    /**
     * Constructs CaseService with injected repository.
     *
     * @param caseRepository Repository implementation
     */
    public CaseService(GenericRepository<Case, String> caseRepository) {
        this.caseRepository = Objects.requireNonNull(caseRepository, "Repository cannot be null");
    }

    /**
     * Factory method for creating CaseService backed by a real database connection factory.
     */
    public static CaseService createDatabaseService(com.mysteryvault.config.DatabaseConnectionFactory connectionFactory) {
        Objects.requireNonNull(connectionFactory, "DatabaseConnectionFactory cannot be null");
        return new CaseService(new com.mysteryvault.repository.impl.CaseJdbcRepository(connectionFactory));
    }

    /**
     * Factory method for production mode reading configuration from environment variables.
     * Fails fast if database is unreachable; does NOT silently fall back to memory.
     */
    public static CaseService createProductionService() {
        com.mysteryvault.config.DatabaseConfig config = com.mysteryvault.config.DatabaseConfig.fromEnvironment();
        config.checkConnectivity();
        com.mysteryvault.config.DatabaseConnectionFactory factory = new com.mysteryvault.config.DatabaseConnectionFactory(config);
        factory.initializeSchema();
        return createDatabaseService(factory);
    }

    /**
     * Factory method for testing with an in-memory repository.
     */
    public static CaseService createInMemoryService() {
        return new CaseService(new CaseMemoryRepository());
    }

    /**
     * Returns an unmodifiable list of all active or archived cases.
     */
    public List<Case> listCases() {
        return caseRepository.findAll();
    }

    /**
     * Finds a case by unique ID with tolerance for hyphen/underscore variance (e.g. CASE-103 vs CASE_103).
     */
    public Optional<Case> findById(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return Optional.empty();
        }
        String trimmed = caseId.trim();
        Optional<Case> direct = caseRepository.findById(trimmed);
        if (direct.isPresent()) {
            return direct;
        }

        // Check alternate delimiter format (e.g. CASE-103 <-> CASE_103)
        if (trimmed.contains("-")) {
            Optional<Case> alt = caseRepository.findById(trimmed.replace('-', '_'));
            if (alt.isPresent()) return alt;
        } else if (trimmed.contains("_")) {
            Optional<Case> alt = caseRepository.findById(trimmed.replace('_', '-'));
            if (alt.isPresent()) return alt;
        }

        return Optional.empty();
    }

    /**
     * Retrieves a case by ID or throws {@link CaseNotFoundException}.
     */
    public Case getCaseOrThrow(String caseId) {
        return findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException(caseId));
    }

    /**
     * Creates and persists a new Case from draft.
     * Validates:
     * - Case ID is present and unique
     * - Case Title is present and not blank
     *
     * @param draft Draft case to create
     * @return Saved Case
     * @throws CaseValidationException if validation checks fail
     */
    public Case createCase(Case draft) {
        Objects.requireNonNull(draft, "Draft case cannot be null");

        validateCaseDraft(draft);

        if (caseRepository.existsById(draft.getId())) {
            throw new CaseValidationException("A case with ID '" + draft.getId() + "' already exists.");
        }

        LocalDateTime now = LocalDateTime.now();
        draft.setCreatedAt(now);
        draft.setUpdatedAt(now);

        return caseRepository.save(draft);
    }

    /**
     * Updates an existing Case.
     *
     * @param updated Updated case
     * @return Saved Case
     * @throws CaseNotFoundException if case does not exist
     * @throws CaseValidationException if validation fails
     */
    public Case updateCase(Case updated) {
        Objects.requireNonNull(updated, "Updated case cannot be null");

        validateCaseDraft(updated);

        Case existing = getCaseOrThrow(updated.getId());

        // Validate status transition if status has changed
        if (existing.getStatus() != updated.getStatus()) {
            if (!existing.getStatus().canTransitionTo(updated.getStatus())) {
                throw new InvalidCaseTransitionException(updated.getId(), existing.getStatus(), updated.getStatus());
            }
        }

        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());

        return caseRepository.save(updated);
    }

    /**
     * Transitions a case to a new status according to state machine rules.
     *
     * @param caseId Case ID
     * @param newStatus Target status
     * @return Updated case with new status
     * @throws CaseNotFoundException if case is not found
     * @throws InvalidCaseTransitionException if transition is illegal
     */
    public Case transitionCaseStatus(String caseId, CaseStatus newStatus) {
        Objects.requireNonNull(newStatus, "Target status cannot be null");
        Case currentCase = getCaseOrThrow(caseId);

        currentCase.transitionTo(newStatus); // validates legality internally
        return caseRepository.save(currentCase);
    }

    /**
     * Adds a suspect ID to a case.
     */
    public Case addSuspectToCase(String caseId, String suspectId) {
        Objects.requireNonNull(suspectId, "Suspect ID cannot be null");
        Case c = getCaseOrThrow(caseId);
        c.addSuspectId(suspectId);
        c.setUpdatedAt(LocalDateTime.now());
        return caseRepository.save(c);
    }

    /**
     * Adds an evidence ID to a case.
     */
    public Case addEvidenceToCase(String caseId, String evidenceId) {
        Objects.requireNonNull(evidenceId, "Evidence ID cannot be null");
        Case c = getCaseOrThrow(caseId);
        c.addEvidenceId(evidenceId);
        c.setUpdatedAt(LocalDateTime.now());
        return caseRepository.save(c);
    }

    /**
     * Adds a clue ID to a case.
     */
    public Case addClueToCase(String caseId, String clueId) {
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        Case c = getCaseOrThrow(caseId);
        c.addClueId(clueId);
        c.setUpdatedAt(LocalDateTime.now());
        return caseRepository.save(c);
    }

    /**
     * Deletes a case by ID.
     */
    public boolean deleteCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return false;
        }
        return caseRepository.deleteById(caseId.trim());
    }

    /**
     * Loads the canonical sample Case #103 'The Poisoned Drink' into the repository.
     *
     * @return Created sample case
     */
    public Case loadSampleCase103() {
        Case sample = new Case(
                "CASE-103",
                "The Poisoned Drink",
                "Billionaire Julian Vance collapsed during a private gallery gala after drinking poisoned vintage wine.",
                CaseStatus.ACTIVE,
                Priority.HIGH,
                "Detective Adrian Miller",
                LocalDateTime.of(2026, 10, 15, 20, 30),
                LocalDateTime.of(2026, 10, 15, 21, 0),
                null, null, null
        );

        sample.addSuspectId("SUS-103-1"); // Marcus Vance
        sample.addSuspectId("SUS-103-2"); // Elena Rostova
        sample.addSuspectId("SUS-103-3"); // Dr. Warren Price

        sample.addEvidenceId("EVD-103-1"); // Autopsy Toxicology Screen
        sample.addEvidenceId("EVD-103-2"); // Camera Feed #04
        sample.addEvidenceId("EVD-103-3"); // Estate Will & Testament

        sample.addClueId("CLU-103-1"); // Pantry Intruder Identification
        sample.addClueId("CLU-103-2"); // Lethal Motive & Toxin Connection

        return caseRepository.save(sample);
    }

    private void validateCaseDraft(Case c) {
        if (c.getId() == null || c.getId().trim().isEmpty()) {
            throw new CaseValidationException("Case ID must not be null or blank.");
        }
        if (c.getTitle() == null || c.getTitle().trim().isEmpty()) {
            throw new CaseValidationException("Case title must not be null or blank.");
        }
    }
}
