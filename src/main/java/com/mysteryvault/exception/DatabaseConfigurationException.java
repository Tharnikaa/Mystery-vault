package com.mysteryvault.exception;

/**
 * Thrown when database configuration is missing, malformed, or connectivity verification fails.
 */
public class DatabaseConfigurationException extends RuntimeException {

    private final String connectionUrl;

    public DatabaseConfigurationException(String message) {
        super(message);
        this.connectionUrl = null;
    }

    public DatabaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.connectionUrl = null;
    }

    public DatabaseConfigurationException(String message, String connectionUrl, Throwable cause) {
        super(String.format("%s [URL: %s]", message, maskUrl(connectionUrl)), cause);
        this.connectionUrl = connectionUrl;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    private static String maskUrl(String url) {
        if (url == null) return "null";
        // Prevent password leakage in connection URLs
        return url.replaceAll("(?i)password=[^&;]*", "password=*****");
    }
}
