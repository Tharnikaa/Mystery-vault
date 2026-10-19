package com.mysteryvault;

import com.mysteryvault.config.DatabaseConfig;
import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.model.Case;
import com.mysteryvault.model.Priority;
import com.mysteryvault.repository.impl.CaseJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CaseJdbcRepository (Module 1).
 * Verifies real SQL persistence, updates, transactional relationships, and database reconnection.
 */
public class CaseJdbcRepositoryTest {

    private DatabaseConnectionFactory connectionFactory;
    private CaseJdbcRepository repository;

    @BeforeEach
    void setUp() {
        // Use an isolated database fixture
        DatabaseConfig config = DatabaseConfig.forH2InMemory("case_jdbc_test_" + System.nanoTime());
        connectionFactory = new DatabaseConnectionFactory(config);
        connectionFactory.initializeSchema();
        repository = new CaseJdbcRepository(connectionFactory);
    }

    @Test
    @DisplayName("Create case and retrieve from database")
    void testCreateAndFindCase() {
        Case c = new Case("CASE-101", "The Grand Casino Heist", "Vault drilled from sewer",
                Priority.HIGH, "Inspector Ross");
        c.addSuspectId("SUS-101");
        c.addEvidenceId("EVD-101");
        c.addClueId("CLU-101");

        Case saved = repository.save(c);
        assertNotNull(saved);
        assertEquals("CASE-101", saved.getId());

        Optional<Case> retrieved = repository.findById("CASE-101");
        assertTrue(retrieved.isPresent());
        assertEquals("The Grand Casino Heist", retrieved.get().getTitle());
        assertEquals(CaseStatus.OPEN, retrieved.get().getStatus());
        assertEquals(Priority.HIGH, retrieved.get().getPriority());
        assertEquals("Inspector Ross", retrieved.get().getLeadInvestigator());
        assertTrue(retrieved.get().getSuspectIds().contains("SUS-101"));
        assertTrue(retrieved.get().getEvidenceIds().contains("EVD-101"));
        assertTrue(retrieved.get().getClueIds().contains("CLU-101"));
    }

    @Test
    @DisplayName("Update existing case persists changes in database")
    void testUpdateCase() {
        Case c = new Case("CASE-UPDATE", "Initial Title", "Initial Desc", Priority.LOW, "Officer A");
        repository.save(c);

        c.setTitle("Updated Title");
        c.setStatus(CaseStatus.ACTIVE);
        c.setPriority(Priority.CRITICAL);
        c.addSuspectId("SUS-NEW");

        Case updated = repository.save(c);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(CaseStatus.ACTIVE, updated.getStatus());
        assertEquals(Priority.CRITICAL, updated.getPriority());

        Optional<Case> reloaded = repository.findById("CASE-UPDATE");
        assertTrue(reloaded.isPresent());
        assertEquals("Updated Title", reloaded.get().getTitle());
        assertEquals(CaseStatus.ACTIVE, reloaded.get().getStatus());
        assertTrue(reloaded.get().getSuspectIds().contains("SUS-NEW"));
    }

    @Test
    @DisplayName("Data remains persistent across new repository instance")
    void testPersistenceAcrossInstances() {
        Case c = new Case("CASE-PERSIST", "Cold Case 1999", "Unsolved", Priority.MEDIUM, "Detective Doe");
        repository.save(c);

        // Reconnect with a brand new repository instance pointing to the same connection factory
        CaseJdbcRepository secondRepoInstance = new CaseJdbcRepository(connectionFactory);
        Optional<Case> found = secondRepoInstance.findById("CASE-PERSIST");

        assertTrue(found.isPresent());
        assertEquals("Cold Case 1999", found.get().getTitle());
        assertTrue(secondRepoInstance.existsById("CASE-PERSIST"));
    }

    @Test
    @DisplayName("List all cases from database")
    void testListAllCases() {
        repository.save(new Case("CASE-A", "Case A", "Desc", Priority.LOW, "Lead"));
        repository.save(new Case("CASE-B", "Case B", "Desc", Priority.HIGH, "Lead"));

        List<Case> all = repository.findAll();
        assertEquals(2, all.size());
        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("Delete case removes case and associations")
    void testDeleteCase() {
        Case c = new Case("CASE-DEL", "Delete Case", "Desc", Priority.LOW, "Lead");
        c.addSuspectId("SUS-DEL");
        repository.save(c);

        assertTrue(repository.existsById("CASE-DEL"));
        boolean deleted = repository.deleteById("CASE-DEL");
        assertTrue(deleted);
        assertFalse(repository.existsById("CASE-DEL"));
        assertTrue(repository.findById("CASE-DEL").isEmpty());
    }

    @Test
    @DisplayName("Query non-existent case returns empty Optional")
    void testFindNonExistent() {
        Optional<Case> notFound = repository.findById("NON-EXISTENT");
        assertTrue(notFound.isEmpty());
        assertFalse(repository.existsById("NON-EXISTENT"));
    }
}
