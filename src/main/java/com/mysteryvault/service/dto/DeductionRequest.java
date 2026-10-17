package com.mysteryvault.service.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Module 11 DTO representing the player's formal deduction submission.
 */
public class DeductionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String caseId;
    private final String accusedSuspectId;
    private final String motive;
    private final Set<String> supportingEvidenceIds;
    private final String reasoning;

    public DeductionRequest(String caseId, String accusedSuspectId, String motive, Set<String> supportingEvidenceIds, String reasoning) {
        this.caseId = Objects.requireNonNull(caseId, "caseId cannot be null");
        this.accusedSuspectId = Objects.requireNonNull(accusedSuspectId, "accusedSuspectId cannot be null");
        this.motive = motive != null ? motive.trim() : "";
        this.supportingEvidenceIds = supportingEvidenceIds != null ? Collections.unmodifiableSet(new HashSet<>(supportingEvidenceIds)) : Collections.emptySet();
        this.reasoning = reasoning != null ? reasoning.trim() : "";
    }

    public String getCaseId() {
        return caseId;
    }

    public String getAccusedSuspectId() {
        return accusedSuspectId;
    }

    public String getMotive() {
        return motive;
    }

    public Set<String> getSupportingEvidenceIds() {
        return supportingEvidenceIds;
    }

    public String getReasoning() {
        return reasoning;
    }

    @Override
    public String toString() {
        return "DeductionRequest{" +
                "caseId='" + caseId + '\'' +
                ", accusedSuspectId='" + accusedSuspectId + '\'' +
                ", motive='" + motive + '\'' +
                ", supportingEvidenceIds=" + supportingEvidenceIds +
                '}';
    }
}
