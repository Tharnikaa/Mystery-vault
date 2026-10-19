package com.mysteryvault.exception;

import java.util.Collections;
import java.util.Set;

/**
 * Thrown when an attempt is made to unlock or access a clue before
 * all prerequisite evidence IDs have been discovered and registered.
 */
public class ClueLockedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String clueId;
    private final Set<String> missingEvidenceIds;

    public ClueLockedException(String clueId, Set<String> missingEvidenceIds) {
        super(String.format("Clue '%s' is locked. Missing required evidence: %s",
                clueId, missingEvidenceIds != null ? missingEvidenceIds : Collections.emptySet()));
        this.clueId = clueId;
        this.missingEvidenceIds = missingEvidenceIds != null
                ? Collections.unmodifiableSet(missingEvidenceIds)
                : Collections.emptySet();
    }

    public ClueLockedException(String message) {
        super(message);
        this.clueId = "UNKNOWN";
        this.missingEvidenceIds = Collections.emptySet();
    }

    public String getClueId() {
        return clueId;
    }

    public Set<String> getMissingEvidenceIds() {
        return missingEvidenceIds;
    }
}
