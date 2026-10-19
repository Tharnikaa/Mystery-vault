package com.mysteryvault.exception;

/**
 * Thrown when case data fails validation checks (e.g. blank title or missing ID).
 */
public class CaseValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CaseValidationException(String message) {
        super(message);
    }

    public CaseValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
