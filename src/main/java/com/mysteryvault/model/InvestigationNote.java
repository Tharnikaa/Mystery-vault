package com.mysteryvault.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing an investigator's note on a case.
 * Owned by Member 3 (Module 9: Notes / Storage).
 */
public class InvestigationNote implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String caseId;
    private String title;
    private String content;
    private final String author;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InvestigationNote(String id, String caseId, String title, String content, String author) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Note ID cannot be null or blank");
        }
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("Case ID cannot be null or blank");
        }
        this.id = id;
        this.caseId = caseId;
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.author = author != null ? author : "Detective";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public InvestigationNote(String id, String caseId, String title, String content, String author, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Note ID cannot be null");
        this.caseId = Objects.requireNonNull(caseId, "Case ID cannot be null");
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.author = author != null ? author : "Detective";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvestigationNote)) return false;
        InvestigationNote that = (InvestigationNote) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "InvestigationNote{" +
                "id='" + id + '\'' +
                ", caseId='" + caseId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
