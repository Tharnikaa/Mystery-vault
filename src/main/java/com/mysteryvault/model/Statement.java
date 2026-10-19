package com.mysteryvault.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a testimony, witness interview, or suspect statement recorded during an investigation.
 */
public class Statement {

    private final String id;
    private final String caseId;
    private final String speaker;
    private final String rawText;
    private final LocalDateTime recordedTimestamp;
    private final String source;

    /**
     * Constructs a Statement.
     *
     * @param id                Unique statement identifier (e.g. "STMT-001")
     * @param caseId            Associated case identifier (e.g. "CASE-101")
     * @param speaker           Person who made the statement (e.g. "S02", "Witness Jane")
     * @param rawText           The verbatim or transcribed statement text
     * @param recordedTimestamp When the statement was taken (nullable)
     * @param source            Origin channel (e.g. "POLICE_INTERVIEW", "AFFIDAVIT")
     */
    public Statement(String id,
                     String caseId,
                     String speaker,
                     String rawText,
                     LocalDateTime recordedTimestamp,
                     String source) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Statement id must not be null or blank");
        }
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Statement caseId must not be null or blank");
        }
        if (rawText == null) {
            throw new IllegalArgumentException("Statement rawText must not be null");
        }

        this.id = id.trim();
        this.caseId = caseId.trim();
        this.speaker = (speaker == null) ? "" : speaker.trim();
        this.rawText = rawText.trim();
        this.recordedTimestamp = recordedTimestamp;
        this.source = (source == null) ? "" : source.trim();
    }

    public String getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getRawText() {
        return rawText;
    }

    public LocalDateTime getRecordedTimestamp() {
        return recordedTimestamp;
    }

    public String getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Statement statement)) return false;
        return Objects.equals(id, statement.id) &&
               Objects.equals(caseId, statement.caseId) &&
               Objects.equals(rawText, statement.rawText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, caseId, rawText);
    }

    @Override
    public String toString() {
        return "Statement{" +
                "id='" + id + '\'' +
                ", caseId='" + caseId + '\'' +
                ", speaker='" + speaker + '\'' +
                ", recordedTimestamp=" + recordedTimestamp +
                ", source='" + source + '\'' +
                ", rawText='" + rawText + '\'' +
                '}';
    }
}
