package com.mysteryvault.model;

import java.time.LocalDateTime;

/**
 * Physical or digitized paper document evidence (Module 3).
 * Represents reports, letters, contracts, autopsy findings, etc.
 */
public class DocumentEvidence extends Evidence {
    private static final long serialVersionUID = 1L;

    private String author;
    private int pageCount;
    private boolean classified;
    private String documentType; // e.g. "Toxicology Report", "Contract", "Handwritten Letter"

    public DocumentEvidence() {
        super();
        this.author = "";
        this.pageCount = 1;
        this.classified = false;
        this.documentType = "";
    }

    public DocumentEvidence(String id, String name, String caseId, String description,
                            LocalDateTime discoveredAt, String locationFound, String collectedBy,
                            boolean analyzed, String author, int pageCount,
                            boolean classified, String documentType) {
        super(id, name, caseId, description, discoveredAt, locationFound, collectedBy, analyzed);
        this.author = author != null ? author.trim() : "";
        setPageCount(pageCount);
        this.classified = classified;
        this.documentType = documentType != null ? documentType.trim() : "";
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author != null ? author.trim() : "";
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        if (pageCount < 0) {
            throw new IllegalArgumentException("Page count cannot be negative: " + pageCount);
        }
        this.pageCount = pageCount;
    }

    public boolean isClassified() {
        return classified;
    }

    public void setClassified(boolean classified) {
        this.classified = classified;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType != null ? documentType.trim() : "";
    }

    @Override
    public String getSummary() {
        return String.format("[DOCUMENT EVIDENCE] %s (ID: %s) | Type: %s | Author: %s | Pages: %d | Classified: %s | Collected By: %s | Location: %s | Analyzed: %s\nDetails: %s",
                getName(),
                getId(),
                documentType.isEmpty() ? "General Document" : documentType,
                author.isEmpty() ? "Unknown Author" : author,
                pageCount,
                classified ? "RESTRICTED/CONFIDENTIAL" : "Standard Access",
                getCollectedBy().isEmpty() ? "Unknown" : getCollectedBy(),
                getLocationFound().isEmpty() ? "Unknown" : getLocationFound(),
                isAnalyzed() ? "Yes" : "No",
                getDescription());
    }
}
