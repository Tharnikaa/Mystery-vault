package com.mysteryvault.exception;

/**
 * Unchecked exception thrown when a database or data access operation fails.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
