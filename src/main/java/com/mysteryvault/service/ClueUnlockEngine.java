package com.mysteryvault.service;

import com.mysteryvault.exception.ClueLockedException;
import com.mysteryvault.model.Clue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Service engine responsible for evaluating prerequisite requirements
 * and unlocking investigation clues deterministically (Module 3).
 */
public class ClueUnlockEngine {

    /**
     * Checks whether all prerequisite evidence IDs for the given clue
     * are present within the set of discovered evidence IDs.
     *
     * @param clue Clue to check
     * @param discoveredEvidenceIds Set of evidence IDs discovered so far
     * @return true if all prerequisites are fulfilled or if none required; false otherwise
     */
    public boolean canUnlock(Clue clue, Set<String> discoveredEvidenceIds) {
        if (clue == null) {
            return false;
        }

        Set<String> required = clue.getRequiredEvidenceIds();
        if (required == null || required.isEmpty()) {
            return true;
        }

        if (discoveredEvidenceIds == null || discoveredEvidenceIds.isEmpty()) {
            return false;
        }

        return discoveredEvidenceIds.containsAll(required);
    }

    /**
     * Unlocks the given clue if all required evidence prerequisites have been discovered.
     * If prerequisites are missing, throws {@link ClueLockedException}.
     *
     * @param clue Clue to unlock
     * @param discoveredEvidenceIds Set of evidence IDs discovered so far
     * @throws ClueLockedException if one or more prerequisite evidence items have not been discovered
     * @throws NullPointerException if clue is null
     */
    public void unlock(Clue clue, Set<String> discoveredEvidenceIds) throws ClueLockedException {
        Objects.requireNonNull(clue, "Clue cannot be null");

        if (clue.isUnlocked()) {
            return; // Already unlocked, no-op
        }

        Set<String> missingPrerequisites = getMissingPrerequisites(clue, discoveredEvidenceIds);
        if (!missingPrerequisites.isEmpty()) {
            throw new ClueLockedException(clue.getId(), missingPrerequisites);
        }

        clue.setUnlocked(true);
        clue.setUnlockedAt(LocalDateTime.now());
    }

    /**
     * Calculates the set of prerequisite evidence IDs that have not yet been discovered.
     *
     * @param clue Clue to inspect
     * @param discoveredEvidenceIds Discovered evidence IDs
     * @return Unmodifiable set of missing evidence IDs
     */
    public Set<String> getMissingPrerequisites(Clue clue, Set<String> discoveredEvidenceIds) {
        if (clue == null) {
            return Collections.emptySet();
        }

        Set<String> required = clue.getRequiredEvidenceIds();
        if (required == null || required.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> missing = new HashSet<>(required);
        if (discoveredEvidenceIds != null) {
            missing.removeAll(discoveredEvidenceIds);
        }

        return Collections.unmodifiableSet(missing);
    }
}
