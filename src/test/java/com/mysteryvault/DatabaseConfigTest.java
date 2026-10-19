package com.mysteryvault;

import com.mysteryvault.config.DatabaseConfig;
import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.exception.DatabaseConfigurationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DatabaseConfig and DatabaseConnectionFactory.
 * Validates configuration parsing, secret masking, and connectivity error handling.
 */
public class DatabaseConfigTest {

    @Test
    @DisplayName("DatabaseConfig parses defaults and environment settings")
    void testConfigDefaults() {
        DatabaseConfig config = new DatabaseConfig("jdbc:postgresql://localhost:5432/testdb", "user1", "pass1");
        assertEquals("jdbc:postgresql://localhost:5432/testdb", config.getUrl());
        assertEquals("user1", config.getUsername());
        assertEquals("*****", config.getMaskedPassword());
    }

    @Test
    @DisplayName("Password is masked in URLs and logs to protect credentials")
    void testMaskUrlCredentials() {
        String sensitiveUrl = "jdbc:postgresql://myhost:5432/db?password=secretPassword123&user=admin";
        String masked = DatabaseConfig.maskUrl(sensitiveUrl);
        assertFalse(masked.contains("secretPassword123"));
        assertTrue(masked.contains("password=*****"));
    }

    @Test
    @DisplayName("Null or empty URL throws DatabaseConfigurationException")
    void testNullUrlThrows() {
        assertThrows(DatabaseConfigurationException.class, () -> new DatabaseConfig("", "u", "p"));
        assertThrows(DatabaseConfigurationException.class, () -> new DatabaseConfig(null, "u", "p"));
    }

    @Test
    @DisplayName("Connectivity verification via SELECT 1 succeeds on active database")
    void testSelect1Connectivity() {
        DatabaseConfig config = DatabaseConfig.forH2InMemory("connectivity_test");
        DatabaseConnectionFactory factory = new DatabaseConnectionFactory(config);
        assertTrue(factory.checkConnectivity());
    }

    @Test
    @DisplayName("Invalid connection fails fast with descriptive DatabaseConfigurationException")
    void testInvalidConnectionThrows() {
        // Unreachable port
        DatabaseConfig badConfig = new DatabaseConfig("jdbc:postgresql://localhost:65530/nonexistent", "user", "pass");
        DatabaseConnectionFactory badFactory = new DatabaseConnectionFactory(badConfig);

        assertThrows(DatabaseConfigurationException.class, badFactory::checkConnectivity);
    }
}
