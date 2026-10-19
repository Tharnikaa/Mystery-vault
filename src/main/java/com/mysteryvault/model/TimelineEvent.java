package com.mysteryvault.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a discrete event or interval within a criminal investigation timeline.
 * <p>
 * Complies with MysteryVault specifications:
 * - String ID and case ID
 * - {@link LocalDateTime} timestamps
 * - Deterministic comparison and equality
 * </p>
 */
public class TimelineEvent implements Comparable<TimelineEvent> {

    private final String id;
    private final String caseId;
    private final LocalDateTime timestamp;
    private final LocalDateTime endTimestamp;
    private final String description;
    private final String source;
    private final String actor;
    private final String location;

    /**
     * Constructs a point-in-time TimelineEvent.
     *
     * @param id          Unique identifier for the event (e.g. "EVT-001")
     * @param caseId      Identifier for the case (e.g. "CASE-404")
     * @param timestamp   Exact timestamp of the event
     * @param description Narrative description of what happened
     * @param source      Evidence or statement source (e.g. "CCTV_LOBBY", "STATEMENT_W01")
     * @param actor       Subject/suspect/witness involved (e.g. "S02")
     * @param location    Physical location (e.g. "Laboratory")
     */
    public TimelineEvent(String id,
                         String caseId,
                         LocalDateTime timestamp,
                         String description,
                         String source,
                         String actor,
                         String location) {
        this(id, caseId, timestamp, timestamp, description, source, actor, location);
    }

    /**
     * Constructs a TimelineEvent spanning an interval [timestamp, endTimestamp].
     *
     * @param id           Unique identifier for the event
     * @param caseId       Identifier for the case
     * @param timestamp    Start timestamp of the event
     * @param endTimestamp End timestamp of the event (defaults to timestamp if null)
     * @param description  Narrative description
     * @param source       Evidence/statement source
     * @param actor        Subject involved
     * @param location     Physical location
     */
    public TimelineEvent(String id,
                         String caseId,
                         LocalDateTime timestamp,
                         LocalDateTime endTimestamp,
                         String description,
                         String source,
                         String actor,
                         String location) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("TimelineEvent id must not be null or blank");
        }
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("TimelineEvent caseId must not be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("TimelineEvent timestamp must not be null");
        }

        LocalDateTime resolvedEnd = (endTimestamp == null) ? timestamp : endTimestamp;
        if (resolvedEnd.isBefore(timestamp)) {
            throw new IllegalArgumentException("TimelineEvent endTimestamp (" + resolvedEnd
                    + ") cannot precede start timestamp (" + timestamp + ")");
        }

        this.id = id.trim();
        this.caseId = caseId.trim();
        this.timestamp = timestamp;
        this.endTimestamp = resolvedEnd;
        this.description = description == null ? "" : description.trim();
        this.source = source == null ? "" : source.trim();
        this.actor = actor == null ? "" : actor.trim();
        this.location = location == null ? "" : location.trim();
    }

    public String getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public String getActor() {
        return actor;
    }

    public String getLocation() {
        return location;
    }

    /**
     * Indicates whether this event represents an instantaneous point in time
     * rather than an extended duration.
     *
     * @return true if timestamp equals endTimestamp
     */
    public boolean isInstant() {
        return timestamp.equals(endTimestamp);
    }

    /**
     * Computes the duration of this event.
     *
     * @return {@link Duration} between start and end timestamps (zero if instantaneous)
     */
    public Duration getDuration() {
        return Duration.between(timestamp, endTimestamp);
    }

    @Override
    public int compareTo(TimelineEvent other) {
        if (other == null) {
            return 1;
        }
        int cmp = this.timestamp.compareTo(other.timestamp);
        if (cmp != 0) {
            return cmp;
        }
        cmp = this.endTimestamp.compareTo(other.endTimestamp);
        if (cmp != 0) {
            return cmp;
        }
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimelineEvent that)) return false;
        return Objects.equals(id, that.id) &&
               Objects.equals(caseId, that.caseId) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(endTimestamp, that.endTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, caseId, timestamp, endTimestamp);
    }

    @Override
    public String toString() {
        return "TimelineEvent{" +
                "id='" + id + '\'' +
                ", caseId='" + caseId + '\'' +
                ", timestamp=" + timestamp +
                ", endTimestamp=" + endTimestamp +
                ", actor='" + actor + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
