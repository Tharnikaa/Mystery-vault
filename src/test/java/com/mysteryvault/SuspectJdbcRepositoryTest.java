package com.mysteryvault;

import com.mysteryvault.config.DatabaseConfig;
import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.model.Case;
import com.mysteryvault.model.Priority;
import com.mysteryvault.model.Suspect;
import com.mysteryvault.repository.impl.CaseJdbcRepository;
import com.mysteryvault.repository.impl.SuspectJdbcRepository;
import com.mysteryvault.service.SuspectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and service tests for Suspect & Person profiles (Module 2).
 * Verifies profile persistence, case isolation, cross-case boundary protection, and validations.
 */
public class SuspectJdbcRepositoryTest {

    private DatabaseConnectionFactory connectionFactory;
    private SuspectJdbcRepository suspectRepository;
    private CaseJdbcRepository caseRepository;
    private SuspectService suspectService;

    @BeforeEach
    void setUp() {
        DatabaseConfig config = DatabaseConfig.forH2InMemory("suspect_test_" + System.nanoTime());
        connectionFactory = new DatabaseConnectionFactory(config);
        connectionFactory.initializeSchema();
        suspectRepository = new SuspectJdbcRepository(connectionFactory);
        caseRepository = new CaseJdbcRepository(connectionFactory);
        suspectService = new SuspectService(suspectRepository);

        // Seed 2 distinct cases for isolation tests
        caseRepository.save(new Case("CASE-ALPHA", "Alpha Investigation", "Desc A", Priority.HIGH, "Lead A"));
        caseRepository.save(new Case("CASE-BETA", "Beta Investigation", "Desc B", Priority.MEDIUM, "Lead B"));
    }

    @Test
    @DisplayName("Create and retrieve suspect profile from database")
    void testCreateAndRetrieveSuspect() {
        Suspect s = new Suspect("SUS-01", "Vincent Moreau", "Antiques Dealer", 45,
                "Dining at Le Petit Bistro", 72, "Needs money for overseas debts", false);
        s.setRole("Prime Suspect");
        s.setNotes("Possesses master key");

        Suspect created = suspectService.createSuspect(s);
        assertNotNull(created);
        assertEquals("SUS-01", created.getId());

        Optional<Suspect> found = suspectService.findById("SUS-01");
        assertTrue(found.isPresent());
        assertEquals("Vincent Moreau", found.get().getName());
        assertEquals("Antiques Dealer", found.get().getOccupation());
        assertEquals(45, found.get().getAge());
        assertEquals("Dining at Le Petit Bistro", found.get().getAlibi());
        assertEquals(72, found.get().getSuspicionScore());
        assertEquals("Needs money for overseas debts", found.get().getMotiveDescription());
        assertFalse(found.get().isAlibiVerified());
    }

    @Test
    @DisplayName("Update suspect profile persists correctly")
    void testUpdateSuspect() {
        Suspect s = new Suspect("SUS-02", "Clara Oswald", "Teacher", 28, "At school", 10);
        suspectService.createSuspect(s);

        s.setAlibi("Updated: At the library with witnesses");
        s.setAlibiVerified(true);
        s.setSuspicionScore(5);
        suspectService.updateSuspect(s);

        Optional<Suspect> updated = suspectService.findById("SUS-02");
        assertTrue(updated.isPresent());
        assertEquals("Updated: At the library with witnesses", updated.get().getAlibi());
        assertTrue(updated.get().isAlibiVerified());
        assertEquals(5, updated.get().getSuspicionScore());
    }

    @Test
    @DisplayName("Case isolation: suspects linked to Case Alpha do NOT leak into Case Beta")
    void testCaseIsolation() {
        Suspect s1 = new Suspect("SUS-A1", "Suspect Alpha 1", "Guard", 30, "Gate", 60);
        Suspect s2 = new Suspect("SUS-A2", "Suspect Alpha 2", "Clerk", 40, "Desk", 40);
        Suspect s3 = new Suspect("SUS-B1", "Suspect Beta 1", "Driver", 35, "Car", 80);

        suspectService.createSuspect(s1);
        suspectService.createSuspect(s2);
        suspectService.createSuspect(s3);

        suspectService.linkSuspectToCase("CASE-ALPHA", "SUS-A1");
        suspectService.linkSuspectToCase("CASE-ALPHA", "SUS-A2");
        suspectService.linkSuspectToCase("CASE-BETA", "SUS-B1");

        List<Suspect> alphaSuspects = suspectService.listSuspectsByCase("CASE-ALPHA");
        List<Suspect> betaSuspects = suspectService.listSuspectsByCase("CASE-BETA");

        assertEquals(2, alphaSuspects.size());
        assertTrue(alphaSuspects.stream().anyMatch(s -> s.getId().equals("SUS-A1")));
        assertTrue(alphaSuspects.stream().anyMatch(s -> s.getId().equals("SUS-A2")));
        assertFalse(alphaSuspects.stream().anyMatch(s -> s.getId().equals("SUS-B1")),
                "Suspect from Case Beta must not leak into Case Alpha!");

        assertEquals(1, betaSuspects.size());
        assertTrue(betaSuspects.stream().anyMatch(s -> s.getId().equals("SUS-B1")));
        assertFalse(betaSuspects.stream().anyMatch(s -> s.getId().equals("SUS-A1")),
                "Suspect from Case Alpha must not leak into Case Beta!");
    }

    @Test
    @DisplayName("Search suspects by name substring")
    void testSearchByName() {
        suspectService.createSuspect(new Suspect("S-10", "Professor Plum", "Academic", 55, "Library", 30));
        suspectService.createSuspect(new Suspect("S-11", "Colonel Mustard", "Military", 62, "Billiard Room", 70));

        List<Suspect> foundPlum = suspectService.searchByName("plum");
        assertEquals(1, foundPlum.size());
        assertEquals("Professor Plum", foundPlum.get(0).getName());

        List<Suspect> foundAll = suspectService.searchByName("e");
        assertEquals(2, foundAll.size());
    }
}
