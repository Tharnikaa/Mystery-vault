package com.mysteryvault.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Clue domain model (Module 3).
 * A deduction or investigative insight unlocked only when all required evidence
 * prerequisites have been discovered.
 */
public class Clue implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String caseId;
    private String title;
    private String description;
    private Set<String> requiredEvidenceIds;
    private boolean unlocked;
    private LocalDateTime unlockedAt;

    public Clue() {
        this.requiredEvidenceIds = new HashSet<>();
        this.unlocked = false;
    }

    public Clue(String id, String caseId, String title, String description) {
        this(id, caseId, title, description, Collections.emptySet());
    }

    public Clue(String id, String caseId, String title, String description, Set<String> requiredEvidenceIds) {
        this.id = Objects.requireNonNull(id, "Clue id cannot be null").trim();
        this.caseId = caseId != null ? caseId.trim() : "";
        this.title = Objects.requireNonNull(title, "Clue title cannot be null").trim();
        this.description = description != null ? description.trim() : "";
        this.requiredEvidenceIds = requiredEvidenceIds != null ? new HashSet<>(requiredEvidenceIds) : new HashSet<>();
        this.unlocked = false;
        this.unlockedAt = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "Clue id cannot be null").trim();
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId != null ? caseId.trim() : "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "Clue title cannot be null").trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public Set<String> getRequiredEvidenceIds() {
        return Collections.unmodifiableSet(requiredEvidenceIds);
    }

    public void setRequiredEvidenceIds(Set<String> requiredEvidenceIds) {
        this.requiredEvidenceIds = requiredEvidenceIds != null ? new HashSet<>(requiredEvidenceIds) : new HashSet<>();
    }

    public void addRequiredEvidenceId(String evidenceId) {
        if (evidenceId != null && !evidenceId.trim().isEmpty()) {
            this.requiredEvidenceIds.add(evidenceId.trim());
        }
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (unlocked && this.unlockedAt == null) {
            this.unlockedAt = LocalDateTime.now();
        } else if (!unlocked) {
            this.unlockedAt = null;
        }
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Clue clue)) return false;
        return Objects.equals(id, clue.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Clue{id='%s', title='%s', caseId='%s', unlocked=%s, requiredEvidenceCount=%d}",
                id, title, caseId, unlocked, requiredEvidenceIds.size());
    }
}
