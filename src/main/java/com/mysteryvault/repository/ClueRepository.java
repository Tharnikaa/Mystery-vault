package com.mysteryvault.repository;

import com.mysteryvault.model.Clue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Repository interface for Clue entities and unlock prerequisites (Module 3).
 */
public interface ClueRepository extends GenericRepository<Clue, String> {

    /**
     * Finds all clues belonging to a given case.
     */
    List<Clue> findByCaseId(String caseId);

    /**
     * Finds all currently unlocked clues for a given case.
     */
    List<Clue> findUnlockedByCaseId(String caseId);

    /**
     * Persists prerequisite evidence IDs required to unlock this clue.
     */
    void savePrerequisites(String clueId, Set<String> requiredEvidenceIds);

    /**
     * Retrieves the set of prerequisite evidence IDs for a clue.
     */
    Set<String> findPrerequisites(String clueId);

    /**
     * Updates the unlocked status and timestamp for a clue.
     */
    void updateUnlockedState(String clueId, boolean unlocked, LocalDateTime unlockedAt);

    /**
     * Links a clue to a case.
     */
    void linkClueToCase(String caseId, String clueId);
}
