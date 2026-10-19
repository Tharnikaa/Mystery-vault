package com.mysteryvault.service;

import com.mysteryvault.exception.CaseValidationException;
import com.mysteryvault.model.Suspect;
import com.mysteryvault.repository.SuspectRepository;
import com.mysteryvault.repository.impl.SuspectMemoryRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service gateway for Suspect & Person profiles (Module 2).
 * Enforces business validation rules and scopes suspects to their respective cases.
 */
public class SuspectService {

    private final SuspectRepository suspectRepository;

    public SuspectService() {
        this(new SuspectMemoryRepository());
    }

    public SuspectService(SuspectRepository suspectRepository) {
        this.suspectRepository = Objects.requireNonNull(suspectRepository, "SuspectRepository cannot be null");
    }

    /**
     * Creates and persists a new Suspect.
     */
    public Suspect createSuspect(Suspect suspect) {
        Objects.requireNonNull(suspect, "Suspect cannot be null");
        validateSuspect(suspect);

        if (suspectRepository.existsById(suspect.getId())) {
            throw new CaseValidationException("Suspect with ID '" + suspect.getId() + "' already exists.");
        }

        return suspectRepository.save(suspect);
    }

    /**
     * Updates an existing Suspect profile.
     */
    public Suspect updateSuspect(Suspect suspect) {
        Objects.requireNonNull(suspect, "Suspect cannot be null");
        validateSuspect(suspect);

        if (!suspectRepository.existsById(suspect.getId())) {
            throw new CaseValidationException("Cannot update non-existent suspect with ID: " + suspect.getId());
        }

        return suspectRepository.save(suspect);
    }

    /**
     * Finds a suspect by ID.
     */
    public Optional<Suspect> findById(String suspectId) {
        if (suspectId == null || suspectId.trim().isEmpty()) {
            return Optional.empty();
        }
        return suspectRepository.findById(suspectId.trim());
    }

    /**
     * Lists all suspects associated with a specific case.
     */
    public List<Suspect> listSuspectsByCase(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return List.of();
        }
        return suspectRepository.findByCaseId(caseId.trim());
    }

    /**
     * Searches suspects by name substring.
     */
    public List<Suspect> searchByName(String nameQuery) {
        return suspectRepository.searchByName(nameQuery);
    }

    /**
     * Associates a suspect with a case.
     */
    public void linkSuspectToCase(String caseId, String suspectId) {
        Objects.requireNonNull(caseId, "Case ID cannot be null");
        Objects.requireNonNull(suspectId, "Suspect ID cannot be null");
        suspectRepository.linkSuspectToCase(caseId.trim(), suspectId.trim());
    }

    /**
     * Unlinks a suspect from a case.
     */
    public void unlinkSuspectFromCase(String caseId, String suspectId) {
        if (caseId != null && suspectId != null) {
            suspectRepository.unlinkSuspectFromCase(caseId.trim(), suspectId.trim());
        }
    }

    /**
     * Deletes a suspect by ID.
     */
    public boolean deleteSuspect(String suspectId) {
        if (suspectId == null || suspectId.trim().isEmpty()) {
            return false;
        }
        return suspectRepository.deleteById(suspectId.trim());
    }

    /**
     * Returns all suspects across all cases.
     */
    public List<Suspect> listAllSuspects() {
        return suspectRepository.findAll();
    }

    private void validateSuspect(Suspect suspect) {
        if (suspect.getId() == null || suspect.getId().trim().isEmpty()) {
            throw new CaseValidationException("Suspect ID cannot be null or blank.");
        }
        if (suspect.getName() == null || suspect.getName().trim().isEmpty()) {
            throw new CaseValidationException("Suspect name cannot be null or blank.");
        }
        if (suspect.getAge() < 0 || suspect.getAge() > 150) {
            throw new CaseValidationException("Suspect age must be between 0 and 150. Given: " + suspect.getAge());
        }
        if (suspect.getSuspicionScore() < 0 || suspect.getSuspicionScore() > 100) {
            throw new CaseValidationException("Suspicion score must be between 0 and 100. Given: " + suspect.getSuspicionScore());
        }
    }
}
