package com.mysteryvault.model;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.exception.InvalidCaseTransitionException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Case domain model (Module 1).
 * Root entity for a criminal investigation.
 * Maintains state machine lifecycle, priority, timestamps, and relational String IDs.
 */
public class Case implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String description;
    private CaseStatus status;
    private Priority priority;
    private String leadInvestigator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relational references stored via immutable String IDs (prevent circular references)
    private Set<String> suspectIds;
    private Set<String> evidenceIds;
    private Set<String> clueIds;

    public Case() {
        this.status = CaseStatus.OPEN;
        this.priority = Priority.MEDIUM;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.suspectIds = new HashSet<>();
        this.evidenceIds = new HashSet<>();
        this.clueIds = new HashSet<>();
    }

    public Case(String id, String title, String description, Priority priority, String leadInvestigator) {
        this(id, title, description, CaseStatus.OPEN, priority, leadInvestigator,
                LocalDateTime.now(), LocalDateTime.now(), null, null, null);
    }

    public Case(String id, String title, String description, CaseStatus status, Priority priority,
                String leadInvestigator, LocalDateTime createdAt, LocalDateTime updatedAt,
                Set<String> suspectIds, Set<String> evidenceIds, Set<String> clueIds) {
        this.id = Objects.requireNonNull(id, "Case id cannot be null").trim();
        this.title = Objects.requireNonNull(title, "Case title cannot be null").trim();
        this.description = description != null ? description.trim() : "";
        this.status = status != null ? status : CaseStatus.OPEN;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.leadInvestigator = leadInvestigator != null ? leadInvestigator.trim() : "";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.suspectIds = suspectIds != null ? new HashSet<>(suspectIds) : new HashSet<>();
        this.evidenceIds = evidenceIds != null ? new HashSet<>(evidenceIds) : new HashSet<>();
        this.clueIds = clueIds != null ? new HashSet<>(clueIds) : new HashSet<>();
    }

    /**
     * Copy constructor for safe defensive duplication.
     */
    public Case(Case source) {
        Objects.requireNonNull(source, "Source case cannot be null");
        this.id = source.id;
        this.title = source.title;
        this.description = source.description;
        this.status = source.status;
        this.priority = source.priority;
        this.leadInvestigator = source.leadInvestigator;
        this.createdAt = source.createdAt;
        this.updatedAt = source.updatedAt;
        this.suspectIds = new HashSet<>(source.suspectIds);
        this.evidenceIds = new HashSet<>(source.evidenceIds);
        this.clueIds = new HashSet<>(source.clueIds);
    }

    /**
     * Transitions this case to a new status according to legal state machine rules.
     *
     * @param targetStatus Target lifecycle status
     * @throws InvalidCaseTransitionException if transition is not permitted
     */
    public void transitionTo(CaseStatus targetStatus) {
        Objects.requireNonNull(targetStatus, "Target status cannot be null");
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidCaseTransitionException(this.id, this.status, targetStatus);
        }
        this.status = targetStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "Case id cannot be null").trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "Case title cannot be null").trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public CaseStatus getStatus() {
        return status;
    }

    public void setStatus(CaseStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
    }

    public String getLeadInvestigator() {
        return leadInvestigator;
    }

    public void setLeadInvestigator(String leadInvestigator) {
        this.leadInvestigator = leadInvestigator != null ? leadInvestigator.trim() : "";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public Set<String> getSuspectIds() {
        return Collections.unmodifiableSet(suspectIds);
    }

    public void setSuspectIds(Set<String> suspectIds) {
        this.suspectIds = suspectIds != null ? new HashSet<>(suspectIds) : new HashSet<>();
    }

    public void addSuspectId(String suspectId) {
        if (suspectId != null && !suspectId.trim().isEmpty()) {
            this.suspectIds.add(suspectId.trim());
        }
    }

    public void removeSuspectId(String suspectId) {
        if (suspectId != null) {
            this.suspectIds.remove(suspectId.trim());
        }
    }

    public Set<String> getEvidenceIds() {
        return Collections.unmodifiableSet(evidenceIds);
    }

    public void setEvidenceIds(Set<String> evidenceIds) {
        this.evidenceIds = evidenceIds != null ? new HashSet<>(evidenceIds) : new HashSet<>();
    }

    public void addEvidenceId(String evidenceId) {
        if (evidenceId != null && !evidenceId.trim().isEmpty()) {
            this.evidenceIds.add(evidenceId.trim());
        }
    }

    public void removeEvidenceId(String evidenceId) {
        if (evidenceId != null) {
            this.evidenceIds.remove(evidenceId.trim());
        }
    }

    public Set<String> getClueIds() {
        return Collections.unmodifiableSet(clueIds);
    }

    public void setClueIds(Set<String> clueIds) {
        this.clueIds = clueIds != null ? new HashSet<>(clueIds) : new HashSet<>();
    }

    public void addClueId(String clueId) {
        if (clueId != null && !clueId.trim().isEmpty()) {
            this.clueIds.add(clueId.trim());
        }
    }

    public void removeClueId(String clueId) {
        if (clueId != null) {
            this.clueIds.remove(clueId.trim());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Case aCase)) return false;
        return Objects.equals(id, aCase.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Case{id='%s', title='%s', status=%s, priority=%s, suspects=%d, evidence=%d, clues=%d}",
                id, title, status, priority, suspectIds.size(), evidenceIds.size(), clueIds.size());
    }
}
