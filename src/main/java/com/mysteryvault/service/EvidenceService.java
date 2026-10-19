package com.mysteryvault.service;

import com.mysteryvault.exception.CaseValidationException;
import com.mysteryvault.exception.ClueLockedException;
import com.mysteryvault.model.Clue;
import com.mysteryvault.model.Evidence;
import com.mysteryvault.repository.ClueRepository;
import com.mysteryvault.repository.EvidenceRepository;
import com.mysteryvault.repository.impl.ClueMemoryRepository;
import com.mysteryvault.repository.impl.EvidenceMemoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Service gateway for Evidence & Clue Management (Module 3).
 * Integrates polymorphic evidence persistence, investigation discovery tracking,
 * and deterministic prerequisite-based clue unlocking.
 */
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final ClueRepository clueRepository;
    private final ClueUnlockEngine unlockEngine;

    public EvidenceService() {
        this(new EvidenceMemoryRepository(), new ClueMemoryRepository(), new ClueUnlockEngine());
    }

    public EvidenceService(EvidenceRepository evidenceRepository, ClueRepository clueRepository) {
        this(evidenceRepository, clueRepository, new ClueUnlockEngine());
    }

    public EvidenceService(EvidenceRepository evidenceRepository, ClueRepository clueRepository, ClueUnlockEngine unlockEngine) {
        this.evidenceRepository = Objects.requireNonNull(evidenceRepository, "EvidenceRepository cannot be null");
        this.clueRepository = Objects.requireNonNull(clueRepository, "ClueRepository cannot be null");
        this.unlockEngine = Objects.requireNonNull(unlockEngine, "ClueUnlockEngine cannot be null");
    }

    // -------------------------------------------------------------------------
    // Evidence Operations
    // -------------------------------------------------------------------------

    public Evidence createEvidence(Evidence evidence) {
        Objects.requireNonNull(evidence, "Evidence cannot be null");
        validateEvidence(evidence);

        if (evidenceRepository.existsById(evidence.getId())) {
            throw new CaseValidationException("Evidence with ID '" + evidence.getId() + "' already exists.");
        }

        return evidenceRepository.save(evidence);
    }

    public Evidence updateEvidence(Evidence evidence) {
        Objects.requireNonNull(evidence, "Evidence cannot be null");
        validateEvidence(evidence);

        if (!evidenceRepository.existsById(evidence.getId())) {
            throw new CaseValidationException("Cannot update non-existent evidence with ID: " + evidence.getId());
        }

        return evidenceRepository.save(evidence);
    }

    public Optional<Evidence> findEvidenceById(String evidenceId) {
        if (evidenceId == null || evidenceId.trim().isEmpty()) {
            return Optional.empty();
        }
        return evidenceRepository.findById(evidenceId.trim());
    }

    public List<Evidence> listCanonicalEvidenceByCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return List.of();
        }
        return evidenceRepository.findByCaseId(caseId.trim());
    }

    public List<Evidence> listDiscoveredEvidenceByCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return List.of();
        }
        return evidenceRepository.findDiscoveredByCaseId(caseId.trim());
    }

    public void markEvidenceDiscovered(String evidenceId, boolean discovered) {
        Objects.requireNonNull(evidenceId, "Evidence ID cannot be null");
        evidenceRepository.markDiscovered(evidenceId.trim(), discovered);
    }

    public void linkEvidenceToCase(String caseId, String evidenceId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(evidenceId, "Evidence ID cannot be null");
        evidenceRepository.linkEvidenceToCase(caseId.trim(), evidenceId.trim());
    }

    public boolean deleteEvidence(String evidenceId) {
        if (evidenceId == null || evidenceId.trim().isEmpty()) {
            return false;
        }
        return evidenceRepository.deleteById(evidenceId.trim());
    }

    // -------------------------------------------------------------------------
    // Clue Operations & Unlocking
    // -------------------------------------------------------------------------

    public Clue createClue(Clue clue) {
        Objects.requireNonNull(clue, "Clue cannot be null");
        validateClue(clue);

        if (clueRepository.existsById(clue.getId())) {
            throw new CaseValidationException("Clue with ID '" + clue.getId() + "' already exists.");
        }

        return clueRepository.save(clue);
    }

    public Clue updateClue(Clue clue) {
        Objects.requireNonNull(clue, "Clue cannot be null");
        validateClue(clue);

        if (!clueRepository.existsById(clue.getId())) {
            throw new CaseValidationException("Cannot update non-existent clue with ID: " + clue.getId());
        }

        return clueRepository.save(clue);
    }

    public Optional<Clue> findClueById(String clueId) {
        if (clueId == null || clueId.trim().isEmpty()) {
            return Optional.empty();
        }
        return clueRepository.findById(clueId.trim());
    }

    public List<Clue> listCanonicalCluesByCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return List.of();
        }
        return clueRepository.findByCaseId(caseId.trim());
    }

    public List<Clue> listUnlockedCluesByCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return List.of();
        }
        return clueRepository.findUnlockedByCaseId(caseId.trim());
    }

    /**
     * Evaluates prerequisites against discovered evidence IDs and unlocks the clue if eligible.
     *
     * @param clueId Clue ID to unlock
     * @param discoveredEvidenceIds Set of evidence IDs discovered in the active session
     * @return Updated unlocked Clue
     * @throws ClueLockedException if prerequisites are missing
     */
    public Clue unlockClue(String clueId, Set<String> discoveredEvidenceIds) throws ClueLockedException {
        Objects.requireNonNull(clueId, "Clue ID cannot be null");
        Clue clue = findClueById(clueId)
                .orElseThrow(() -> new CaseValidationException("Clue not found: " + clueId));

        unlockEngine.unlock(clue, discoveredEvidenceIds);
        clueRepository.updateUnlockedState(clue.getId(), true, LocalDateTime.now());
        return clueRepository.findById(clue.getId()).orElse(clue);
    }

    public boolean canUnlockClue(String clueId, Set<String> discoveredEvidenceIds) {
        if (clueId == null) return false;
        return findClueById(clueId)
                .map(clue -> unlockEngine.canUnlock(clue, discoveredEvidenceIds))
                .orElse(false);
    }

    public boolean deleteClue(String clueId) {
        if (clueId == null || clueId.trim().isEmpty()) {
            return false;
        }
        return clueRepository.deleteById(clueId.trim());
    }

    private void validateEvidence(Evidence e) {
        if (e.getId() == null || e.getId().trim().isEmpty()) {
            throw new CaseValidationException("Evidence ID cannot be null or blank.");
        }
        if (e.getName() == null || e.getName().trim().isEmpty()) {
            throw new CaseValidationException("Evidence name cannot be null or blank.");
        }
    }

    private void validateClue(Clue c) {
        if (c.getId() == null || c.getId().trim().isEmpty()) {
            throw new CaseValidationException("Clue ID cannot be null or blank.");
        }
        if (c.getTitle() == null || c.getTitle().trim().isEmpty()) {
            throw new CaseValidationException("Clue title cannot be null or blank.");
        }
    }
}
