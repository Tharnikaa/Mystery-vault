package com.mysteryvault.model;

import java.time.LocalDateTime;

/**
 * Digital forensic evidence (Module 3).
 * Represents electronic files, network logs, surveillance feeds, etc.
 */
public class DigitalEvidence extends Evidence {
    private static final long serialVersionUID = 1L;

    private String deviceType;
    private String fileHash; // e.g. SHA-256 integrity hash
    private String ipAddress;
    private long fileSizeBytes;

    public DigitalEvidence() {
        super();
        this.deviceType = "";
        this.fileHash = "";
        this.ipAddress = "";
        this.fileSizeBytes = 0L;
    }

    public DigitalEvidence(String id, String name, String caseId, String description,
                           LocalDateTime discoveredAt, String locationFound, String collectedBy,
                           boolean analyzed, String deviceType, String fileHash,
                           String ipAddress, long fileSizeBytes) {
        super(id, name, caseId, description, discoveredAt, locationFound, collectedBy, analyzed);
        this.deviceType = deviceType != null ? deviceType.trim() : "";
        this.fileHash = fileHash != null ? fileHash.trim() : "";
        this.ipAddress = ipAddress != null ? ipAddress.trim() : "";
        this.fileSizeBytes = Math.max(0L, fileSizeBytes);
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType != null ? deviceType.trim() : "";
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash != null ? fileHash.trim() : "";
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress != null ? ipAddress.trim() : "";
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = Math.max(0L, fileSizeBytes);
    }

    @Override
    public String getSummary() {
        String sizeFormatted = fileSizeBytes < 1024 ? fileSizeBytes + " B"
                : fileSizeBytes < 1024 * 1024 ? (fileSizeBytes / 1024) + " KB"
                : (fileSizeBytes / (1024 * 1024)) + " MB";

        return String.format("[DIGITAL EVIDENCE] %s (ID: %s) | Device: %s | SHA-256: %s | Size: %s | Location: %s | Analyzed: %s\nDetails: %s",
                getName(),
                getId(),
                deviceType.isEmpty() ? "Unknown Device" : deviceType,
                fileHash.isEmpty() ? "N/A" : fileHash,
                sizeFormatted,
                getLocationFound().isEmpty() ? "Unknown" : getLocationFound(),
                isAnalyzed() ? "Yes" : "No",
                getDescription());
    }
}
