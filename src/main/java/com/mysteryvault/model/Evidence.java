package com.mysteryvault.model;

import java.time.LocalDateTime;

/**
 * Abstract evidence domain model extending InvestigationAsset.
 * Requires concrete subclasses to supply polymorphic getSummary().
 */
public abstract class Evidence extends InvestigationAsset {
    private static final long serialVersionUID = 1L;

    private String locationFound;
    private String collectedBy;
    private boolean analyzed;

    protected Evidence() {
        super();
        this.locationFound = "";
        this.collectedBy = "";
        this.analyzed = false;
    }

    protected Evidence(String id, String name, String caseId, String description,
                       LocalDateTime discoveredAt, String locationFound, String collectedBy, boolean analyzed) {
        super(id, name, caseId, description, discoveredAt);
        this.locationFound = locationFound != null ? locationFound.trim() : "";
        this.collectedBy = collectedBy != null ? collectedBy.trim() : "";
        this.analyzed = analyzed;
    }

    public String getLocationFound() {
        return locationFound;
    }

    public void setLocationFound(String locationFound) {
        this.locationFound = locationFound != null ? locationFound.trim() : "";
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy != null ? collectedBy.trim() : "";
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    /**
     * Polymorphic summary method implemented by concrete evidence subclasses.
     *
     * @return Formatted domain summary of this piece of evidence
     */
    public abstract String getSummary();
}
