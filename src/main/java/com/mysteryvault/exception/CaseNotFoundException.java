package com.mysteryvault.exception;

/**
 * Thrown when a requested case cannot be found in the repository.
 */
public class CaseNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String caseId;

    public CaseNotFoundException(String caseId) {
        super("Case not found with ID: " + caseId);
        this.caseId = caseId;
    }

    public String getCaseId() {
        return caseId;
    }
}
