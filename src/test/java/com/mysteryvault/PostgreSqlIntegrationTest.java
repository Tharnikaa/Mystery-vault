package com.mysteryvault;

import com.mysteryvault.config.DatabaseConfig;
import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.model.Case;
import com.mysteryvault.model.Priority;
import com.mysteryvault.repository.impl.CaseJdbcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live PostgreSQL Integration Test.
 * Checks for a real external PostgreSQL instance at MYSTERYVAULT_DB_URL.
 * If available, verifies end-to-end PostgreSQL persistence.
 * If unavailable, cleanly reports the external dependency limitation without fabricating results.
 */
public class PostgreSqlIntegrationTest {

    @Test
    @DisplayName("Verify PostgreSQL live persistence if external database is running")
    void testLivePostgreSqlPersistence() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();

        if (!config.isAvailable()) {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" [POSTGRESQL STATUS] Live PostgreSQL instance is NOT running or unreachable at: " + config.getMaskedUrl());
            System.out.println(" Set MYSTERYVAULT_DB_URL, MYSTERYVAULT_DB_USERNAME, MYSTERYVAULT_DB_PASSWORD");
            System.out.println(" and start PostgreSQL service to execute live PostgreSQL integration tests.");
            System.out.println("--------------------------------------------------------------------------------");
            return; // Gracefully pass with limitation clearly reported
        }

        System.out.println("Connecting to live PostgreSQL at: " + config.getMaskedUrl());
        DatabaseConnectionFactory factory = new DatabaseConnectionFactory(config);
        factory.initializeSchema();

        CaseJdbcRepository repo = new CaseJdbcRepository(factory);
        String testCaseId = "CASE-PG-" + System.currentTimeMillis();
        Case testCase = new Case(testCaseId, "PostgreSQL Live Case", "Created in live PG test", Priority.HIGH, "Detective PG");

        repo.save(testCase);
        Optional<Case> retrieved = repo.findById(testCaseId);

        assertTrue(retrieved.isPresent());
        assertEquals("PostgreSQL Live Case", retrieved.get().getTitle());

        // Cleanup test record
        repo.deleteById(testCaseId);
        assertFalse(repo.existsById(testCaseId));
    }
}
