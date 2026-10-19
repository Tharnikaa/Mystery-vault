package com.mysteryvault.model;

import com.mysteryvault.enums.Severity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a structured contradiction discovered during investigative analysis.
 * Contains conflicting source IDs, a natural-language reason, and an assigned severity.
 */
public class Contradiction implements Comparable<Contradiction> {

    private final String id;
    private final String caseId;
    private final List<String> sourceIds;
    private final String reason;
    private final Severity severity;
    private final String contradictionType;
    private final LocalDateTime conflictTimestamp;
    private final String actor;

    /**
     * Constructs a Contradiction.
     *
     * @param id                Unique contradiction identifier (e.g. "CTRD-001")
     * @param caseId            Identifier of the related case
     * @param sourceIds         List of conflicting entity IDs (statements, timeline events, evidence)
     * @param reason            Investigative explanation of why the sources conflict
     * @param severity          Severity rating (LOW, MEDIUM, HIGH, CRITICAL)
     * @param contradictionType Category/type of contradiction (e.g. "LOCATION_COLLISION")
     * @param conflictTimestamp Timestamp where the conflict occurs (nullable)
     * @param actor             Person/subject involved (nullable)
     */
    public Contradiction(String id,
                         String caseId,
                         List<String> sourceIds,
                         String reason,
                         Severity severity,
                         String contradictionType,
                         LocalDateTime conflictTimestamp,
                         String actor) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Contradiction id must not be null or blank");
        }
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Contradiction caseId must not be null or blank");
        }
        if (sourceIds == null || sourceIds.isEmpty()) {
            throw new IllegalArgumentException("Contradiction must have at least one source ID");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Contradiction reason must not be null or blank");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Contradiction severity must not be null");
        }

        this.id = id.trim();
        this.caseId = caseId.trim();
        this.sourceIds = Collections.unmodifiableList(new ArrayList<>(sourceIds));
        this.reason = reason.trim();
        this.severity = severity;
        this.contradictionType = (contradictionType == null) ? "GENERAL" : contradictionType.trim();
        this.conflictTimestamp = conflictTimestamp;
        this.actor = (actor == null) ? "" : actor.trim();
    }

    public String getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public List<String> getSourceIds() {
        return sourceIds;
    }

    public String getReason() {
        return reason;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getContradictionType() {
        return contradictionType;
    }

    public LocalDateTime getConflictTimestamp() {
        return conflictTimestamp;
    }

    public String getActor() {
        return actor;
    }

    @Override
    public int compareTo(Contradiction other) {
        if (other == null) return -1;
        // Primary sort: higher severity first
        int sevCmp = Integer.compare(other.severity.getWeight(), this.severity.getWeight());
        if (sevCmp != 0) {
            return sevCmp;
        }
        // Secondary sort: alphabetical by ID for deterministic ordering
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contradiction that)) return false;
        return Objects.equals(id, that.id) &&
               Objects.equals(caseId, that.caseId) &&
               Objects.equals(sourceIds, that.sourceIds) &&
               severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, caseId, sourceIds, severity);
    }

    @Override
    public String toString() {
        return "Contradiction{" +
                "id='" + id + '\'' +
                ", caseId='" + caseId + '\'' +
                ", severity=" + severity +
                ", type='" + contradictionType + '\'' +
                ", sources=" + sourceIds +
                ", actor='" + actor + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
