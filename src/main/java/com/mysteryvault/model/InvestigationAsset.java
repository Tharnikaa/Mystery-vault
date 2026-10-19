package com.mysteryvault.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base domain model for any tangible or intangible investigative asset.
 * Serializable, immutable ID, and completely detached from UI and SQL logic.
 */
public abstract class InvestigationAsset implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String caseId;
    private String description;
    private LocalDateTime discoveredAt;

    protected InvestigationAsset() {
        this.discoveredAt = LocalDateTime.now();
    }

    protected InvestigationAsset(String id, String name, String caseId, String description, LocalDateTime discoveredAt) {
        this.id = Objects.requireNonNull(id, "Asset id cannot be null").trim();
        this.name = Objects.requireNonNull(name, "Asset name cannot be null").trim();
        this.caseId = caseId != null ? caseId.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.discoveredAt = discoveredAt != null ? discoveredAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "Asset id cannot be null").trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Asset name cannot be null").trim();
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId != null ? caseId.trim() : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public LocalDateTime getDiscoveredAt() {
        return discoveredAt;
    }

    public void setDiscoveredAt(LocalDateTime discoveredAt) {
        this.discoveredAt = discoveredAt != null ? discoveredAt : LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvestigationAsset that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', caseId='%s'}", getClass().getSimpleName(), id, name, caseId);
    }
}
