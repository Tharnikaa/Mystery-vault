package com.mysteryvault;

import com.mysteryvault.config.DatabaseConfig;
import com.mysteryvault.config.DatabaseConnectionFactory;
import com.mysteryvault.exception.ClueLockedException;
import com.mysteryvault.model.*;
import com.mysteryvault.repository.impl.CaseJdbcRepository;
import com.mysteryvault.repository.impl.ClueJdbcRepository;
import com.mysteryvault.repository.impl.EvidenceArchiveDao;
import com.mysteryvault.repository.impl.EvidenceJdbcRepository;
import com.mysteryvault.service.ClueUnlockEngine;
import com.mysteryvault.service.EvidenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration and service tests for Evidence & Clue Management (Module 3).
 * Verifies polymorphic evidence persistence, clue unlocking, and separation
 * of canonical case catalog from active session discovery.
 */
public class EvidenceJdbcRepositoryTest {

    private DatabaseConnectionFactory connectionFactory;
    private CaseJdbcRepository caseRepository;
    private EvidenceJdbcRepository evidenceRepository;
    private ClueJdbcRepository clueRepository;
    private EvidenceService evidenceService;

    @BeforeEach
    void setUp() {
        DatabaseConfig config = DatabaseConfig.forH2InMemory("evidence_test_" + System.nanoTime());
        connectionFactory = new DatabaseConnectionFactory(config);
        connectionFactory.initializeSchema();

        caseRepository = new CaseJdbcRepository(connectionFactory);
        evidenceRepository = new EvidenceJdbcRepository(connectionFactory);
        clueRepository = new ClueJdbcRepository(connectionFactory);
        evidenceService = new EvidenceService(evidenceRepository, clueRepository, new ClueUnlockEngine());

        // Setup base case
        caseRepository.save(new Case("CASE-EV-01", "Evidence Case", "Desc", Priority.HIGH, "Detective Holmes"));
    }

    @Test
    @DisplayName("Persist and retrieve polymorphic DocumentEvidence")
    void testDocumentEvidencePersistence() {
        DocumentEvidence doc = new DocumentEvidence(
                "EVD-DOC-1",
                "Autopsy Report",
                "CASE-EV-01",
                "Fatal wound analysis",
                LocalDateTime.of(2026, 10, 18, 9, 0),
                "County Morgue",
                "Dr. Chen",
                true,
                "Forensic Pathology Dept",
                8,
                true,
                "Medical Autopsy"
        );

        evidenceService.createEvidence(doc);

        Optional<Evidence> retrieved = evidenceService.findEvidenceById("EVD-DOC-1");
        assertTrue(retrieved.isPresent());
        assertInstanceOf(DocumentEvidence.class, retrieved.get());

        DocumentEvidence reloadedDoc = (DocumentEvidence) retrieved.get();
        assertEquals("Autopsy Report", reloadedDoc.getName());
        assertEquals("Forensic Pathology Dept", reloadedDoc.getAuthor());
        assertEquals(8, reloadedDoc.getPageCount());
        assertTrue(reloadedDoc.isClassified());
        assertEquals("Medical Autopsy", reloadedDoc.getDocumentType());
        assertTrue(reloadedDoc.getSummary().contains("[DOCUMENT EVIDENCE]"));
    }

    @Test
    @DisplayName("Persist and retrieve polymorphic DigitalEvidence")
    void testDigitalEvidencePersistence() {
        DigitalEvidence dig = new DigitalEvidence(
                "EVD-DIG-1",
                "CCTV Surveillance",
                "CASE-EV-01",
                "Hallway motion footage",
                LocalDateTime.of(2026, 10, 18, 2, 15),
                "Server Room",
                "Officer Tanaka",
                true,
                "Synology NVR",
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                "192.168.1.100",
                52428800L
        );

        evidenceService.createEvidence(dig);

        Optional<Evidence> retrieved = evidenceService.findEvidenceById("EVD-DIG-1");
        assertTrue(retrieved.isPresent());
        assertInstanceOf(DigitalEvidence.class, retrieved.get());

        DigitalEvidence reloadedDig = (DigitalEvidence) retrieved.get();
        assertEquals("CCTV Surveillance", reloadedDig.getName());
        assertEquals("Synology NVR", reloadedDig.getDeviceType());
        assertEquals("192.168.1.100", reloadedDig.getIpAddress());
        assertEquals(52428800L, reloadedDig.getFileSizeBytes());
        assertTrue(reloadedDig.getSummary().contains("[DIGITAL EVIDENCE]"));
    }

    @Test
    @DisplayName("Canonical evidence is distinct from discovered session progress")
    void testCanonicalVsDiscoveredSeparation() {
        DocumentEvidence e1 = new DocumentEvidence("E-1", "Doc 1", "CASE-EV-01", "D", LocalDateTime.now(), "L", "C", false, "A", 1, false, "T");
        DocumentEvidence e2 = new DocumentEvidence("E-2", "Doc 2", "CASE-EV-01", "D", LocalDateTime.now(), "L", "C", false, "A", 1, false, "T");

        evidenceService.createEvidence(e1);
        evidenceService.createEvidence(e2);

        // Initially, 2 canonical items exist, 0 are marked discovered in this active session
        List<Evidence> canonical = evidenceService.listCanonicalEvidenceByCase("CASE-EV-01");
        List<Evidence> discovered = evidenceService.listDiscoveredEvidenceByCase("CASE-EV-01");

        assertEquals(2, canonical.size());
        assertEquals(0, discovered.size(), "Canonical clues/evidence must not be automatically treated as discovered!");

        // Player discovers E-1 during gameplay
        evidenceService.markEvidenceDiscovered("E-1", true);

        List<Evidence> discoveredNow = evidenceService.listDiscoveredEvidenceByCase("CASE-EV-01");
        assertEquals(1, discoveredNow.size());
        assertEquals("E-1", discoveredNow.get(0).getId());
    }

    @Test
    @DisplayName("Clue persistence with prerequisites and unlocking workflow")
    void testCluePersistenceAndUnlockWorkflow() {
        // Prerequisites
        evidenceService.createEvidence(new DocumentEvidence("E-PREREQ-1", "Key Letter", "CASE-EV-01", "D", LocalDateTime.now(), "L", "C", false, "A", 1, false, "T"));
        evidenceService.createEvidence(new DocumentEvidence("E-PREREQ-2", "Bank Statement", "CASE-EV-01", "D", LocalDateTime.now(), "L", "C", false, "A", 1, false, "T"));

        Clue clue = new Clue("CLU-TEST", "CASE-EV-01", "Blackmail Plot",
                "Letters reveal blackmail scheme", Set.of("E-PREREQ-1", "E-PREREQ-2"));

        evidenceService.createClue(clue);

        Optional<Clue> reloaded = evidenceService.findClueById("CLU-TEST");
        assertTrue(reloaded.isPresent());
        assertFalse(reloaded.get().isUnlocked());
        assertEquals(2, reloaded.get().getRequiredEvidenceIds().size());

        // Attempting to unlock with partial evidence fails
        assertFalse(evidenceService.canUnlockClue("CLU-TEST", Set.of("E-PREREQ-1")));
        assertThrows(ClueLockedException.class, () ->
                evidenceService.unlockClue("CLU-TEST", Set.of("E-PREREQ-1")));

        // Unlocking with all prerequisites succeeds and updates DB
        assertTrue(evidenceService.canUnlockClue("CLU-TEST", Set.of("E-PREREQ-1", "E-PREREQ-2")));
        Clue unlocked = evidenceService.unlockClue("CLU-TEST", Set.of("E-PREREQ-1", "E-PREREQ-2"));

        assertTrue(unlocked.isUnlocked());
        assertNotNull(unlocked.getUnlockedAt());

        // Verify state is persisted in new query
        Clue dbClue = evidenceService.findClueById("CLU-TEST").orElseThrow();
        assertTrue(dbClue.isUnlocked());
    }

    @Test
    @DisplayName("EvidenceArchiveDao persists to database table evidence_archive")
    void testEvidenceArchiveDatabasePersistence() {
        EvidenceArchiveDao archiveDao = new EvidenceArchiveDao(connectionFactory);
        DocumentEvidence doc = new DocumentEvidence("E-ARCH-1", "Archived Will", "CASE-EV-01", "Legal Will",
                LocalDateTime.now(), "Vault", "Detective", true, "Law Firm", 10, false, "Will");

        archiveDao.archive(doc);

        assertEquals(1, archiveDao.count());
        Optional<Evidence> retrieved = archiveDao.retrieve("E-ARCH-1");
        assertTrue(retrieved.isPresent());
        assertEquals("Archived Will", retrieved.get().getName());
    }
}
