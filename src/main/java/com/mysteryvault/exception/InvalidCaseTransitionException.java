package com.mysteryvault.exception;

import com.mysteryvault.enums.CaseStatus;

/**
 * Thrown when an illegal lifecycle transition is attempted on a case.
 */
public class InvalidCaseTransitionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String caseId;
    private final CaseStatus currentStatus;
    private final CaseStatus attemptedStatus;

    public InvalidCaseTransitionException(String caseId, CaseStatus currentStatus, CaseStatus attemptedStatus) {
        super(String.format("Illegal status transition for Case '%s': cannot transition from %s to %s",
                caseId, currentStatus, attemptedStatus));
        this.caseId = caseId;
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public String getCaseId() {
        return caseId;
    }

    public CaseStatus getCurrentStatus() {
        return currentStatus;
    }

    public CaseStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}
