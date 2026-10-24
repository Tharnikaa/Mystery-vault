package com.mysteryvault.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Module 4 pure data model representing a connection/relationship
 * between two investigative nodes in the Clue Network Graph.
 *
 * Owned by Member 4.
 */
public class Connection implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String sourceId;
    private final String targetId;
    private final String relationType;
    private final boolean strongLead;

    public Connection(String id, String sourceId, String targetId, String relationType, boolean strongLead) {
        this.id = Objects.requireNonNull(id, "Connection id cannot be null");
        this.sourceId = Objects.requireNonNull(sourceId, "Source id cannot be null");
        this.targetId = Objects.requireNonNull(targetId, "Target id cannot be null");
        this.relationType = relationType != null ? relationType : "LINKED";
        this.strongLead = strongLead;
    }

    public Connection(String sourceId, String targetId, String relationType, boolean strongLead) {
        this(sourceId + "_" + targetId, sourceId, targetId, relationType, strongLead);
    }

    public Connection(String sourceId, String targetId, String relationType) {
        this(sourceId, targetId, relationType, false);
    }

    public String getId() {
        return id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getRelationType() {
        return relationType;
    }

    public boolean isStrongLead() {
        return strongLead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "id='" + id + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", relationType='" + relationType + '\'' +
                ", strongLead=" + strongLead +
                '}';
    }
}
