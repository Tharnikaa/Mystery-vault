package com.mysteryvault.exception;

/**
 * Domain exception thrown when contradiction evaluation fails due to invalid parameters,
 * unparseable critical comparison inputs, or rule evaluation errors.
 */
public class ContradictionException extends RuntimeException {

    private final String caseId;

    public ContradictionException(String message) {
        super(message);
        this.caseId = null;
    }

    public ContradictionException(String message, Throwable cause) {
        super(message, cause);
        this.caseId = null;
    }

    public ContradictionException(String caseId, String message) {
        super("Case [" + caseId + "]: " + message);
        this.caseId = caseId;
    }

    public ContradictionException(String caseId, String message, Throwable cause) {
        super("Case [" + caseId + "]: " + message, cause);
        this.caseId = caseId;
    }

    public String getCaseId() {
        return caseId;
    }
}
