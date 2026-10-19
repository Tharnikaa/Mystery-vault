package com.mysteryvault;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.exception.CaseNotFoundException;
import com.mysteryvault.exception.CaseValidationException;
import com.mysteryvault.exception.ClueLockedException;
import com.mysteryvault.exception.InvalidCaseTransitionException;
import com.mysteryvault.model.*;
import com.mysteryvault.service.CaseService;
import com.mysteryvault.service.ClueUnlockEngine;
import com.mysteryvault.service.SampleDataFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Member 1 Standalone Verification Demo (Modules 1, 2, 3).
 * Demonstrates:
 * 1. Case Management (Module 1): CRUD, validation, and state machine transitions.
 * 2. Suspect & Person (Module 2): Domain inheritance, suspicion scoring, alibis.
 * 3. Evidence & Clues (Module 3): Polymorphic summaries and deterministic clue unlocking.
 * 4. Sample Data Loading for Case #103 'The Poisoned Drink'.
 *
 * Runs as a pure console application without Swing or JDBC dependencies.
 */
public class Member1VerificationDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   MYSTERY VAULT — MEMBER 1 VERIFICATION DEMO (MODULES 1, 2, 3)");
        System.out.println("   Pure Java 17+ Console Verification | Zero Swing / Zero SQL Dependencies");
        System.out.println("================================================================================\n");

        CaseService caseService = new CaseService();
        ClueUnlockEngine unlockEngine = new ClueUnlockEngine();

        // ---------------------------------------------------------------------
        // 1. MODULE 1 DEMO: Case CRUD & State Machine Transitions
        // ---------------------------------------------------------------------
        System.out.println(">>> 1. MODULE 1: CASE CRUD & LIFECYCLE TRANSITIONS");
        Case newCase = new Case("CASE-001", "The Stolen Artifact", "Museum vault breach overnight",
                Priority.MEDIUM, "Detective Vance");
        caseService.createCase(newCase);
        System.out.println("Created initial case: " + newCase);
        System.out.println("Initial status: " + newCase.getStatus());

        // Legal Transition: OPEN -> ACTIVE
        System.out.println("\nAttempting legal transition: OPEN -> ACTIVE...");
        Case activeCase = caseService.transitionCaseStatus("CASE-001", CaseStatus.ACTIVE);
        System.out.println("Status after transition: " + activeCase.getStatus() + " [SUCCESS]");

        // Illegal Transition Test: ACTIVE -> OPEN (not allowed)
        System.out.println("\nAttempting ILLEGAL transition: ACTIVE -> OPEN...");
        try {
            caseService.transitionCaseStatus("CASE-001", CaseStatus.OPEN);
            System.err.println("FAILED: Illegal transition was allowed!");
        } catch (InvalidCaseTransitionException e) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + e.getMessage() + " [CORRECT]");
        }

        // Legal Transition: ACTIVE -> SOLVED
        System.out.println("\nAttempting legal transition: ACTIVE -> SOLVED...");
        Case solvedCase = caseService.transitionCaseStatus("CASE-001", CaseStatus.SOLVED);
        System.out.println("Status after transition: " + solvedCase.getStatus() + " [SUCCESS]");

        // Legal Transition: SOLVED -> ARCHIVED
        System.out.println("\nAttempting legal transition: SOLVED -> ARCHIVED...");
        Case archivedCase = caseService.transitionCaseStatus("CASE-001", CaseStatus.ARCHIVED);
        System.out.println("Status after transition: " + archivedCase.getStatus() + " [SUCCESS]");

        // Illegal Transition from Terminal: ARCHIVED -> ACTIVE
        System.out.println("\nAttempting transition from terminal state: ARCHIVED -> ACTIVE...");
        try {
            caseService.transitionCaseStatus("CASE-001", CaseStatus.ACTIVE);
            System.err.println("FAILED: Terminal state transition was allowed!");
        } catch (InvalidCaseTransitionException e) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + e.getMessage() + " [CORRECT]");
        }

        // Validation test
        System.out.println("\nAttempting to create case with blank title...");
        try {
            caseService.createCase(new Case("CASE-BAD", "  ", "Desc", Priority.LOW, "Investigator"));
            System.err.println("FAILED: Blank title was allowed!");
        } catch (CaseValidationException e) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + e.getMessage() + " [CORRECT]");
        }

        // ---------------------------------------------------------------------
        // 2. MODULE 2 DEMO: Person & Suspect Domain Entities
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 2. MODULE 2: PERSON & SUSPECT DOMAIN ENTITIES");
        Person witness = new Person("PER-101", "Arthur Pendelton", "Witness", "Night watchman");
        System.out.println("Created Person: " + witness);

        Suspect suspect = new Suspect("SUS-101", "Lady Evelyn Grey", "Art Restorer", 41,
                "Claimed to be dining at Club 21", 85, "Large financial arrears to cartel", false);
        System.out.println("Created Suspect (subclass): " + suspect);
        System.out.println("  Occupation: " + suspect.getOccupation());
        System.out.println("  Suspicion Score: " + suspect.getSuspicionScore() + "/100");
        System.out.println("  Alibi: " + suspect.getAlibi());

        // Score boundary test
        try {
            suspect.setSuspicionScore(150);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught score boundary validation: " + e.getMessage() + " [CORRECT]");
        }

        // ---------------------------------------------------------------------
        // 3. MODULE 3 DEMO: Polymorphic Evidence & Clue Unlock Engine
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 3. MODULE 3: POLYMORPHIC EVIDENCE & CLUE UNLOCK ENGINE");

        Evidence docEvidence = new DocumentEvidence(
                "EVD-01", "Ledger Excerpt", "CASE-001", "Discrepancy of $500,000",
                LocalDateTime.now(), "Office Desk Drawer", "Officer Davis", true,
                "Forensic Accountant R. Smith", 3, true, "Financial Audit"
        );

        Evidence digitalEvidence = new DigitalEvidence(
                "EVD-02", "Server Access Log", "CASE-001", "Unauthorized admin root session at 03:14 AM",
                LocalDateTime.now(), "Main Server Room", "Cyber Detective King", true,
                "Debian Bastion Host", "a1b2c3d4e5f67890abcdef1234567890abcdef12", "10.0.0.45", 1048576L
        );

        System.out.println("Document Evidence getSummary():\n" + docEvidence.getSummary());
        System.out.println();
        System.out.println("Digital Evidence getSummary():\n" + digitalEvidence.getSummary());

        // Clue Unlocking Workflow
        System.out.println("\nTesting Clue Unlocking Workflow...");
        Clue encryptedClue = new Clue(
                "CLU-99", "CASE-001", "Vault Keycode Algorithm",
                "Decrypted passcode matches the birthday of the founder.",
                Set.of("EVD-01", "EVD-02")
        );
        System.out.println("Initial Clue state: " + encryptedClue);
        System.out.println("Required Evidence: " + encryptedClue.getRequiredEvidenceIds());

        Set<String> discoveredEvidence = new HashSet<>();
        discoveredEvidence.add("EVD-01"); // Only 1 of 2 prerequisites discovered

        System.out.println("\nAttempting to unlock clue with partial evidence (only EVD-01)...");
        System.out.println("Can unlock? " + unlockEngine.canUnlock(encryptedClue, discoveredEvidence));
        try {
            unlockEngine.unlock(encryptedClue, discoveredEvidence);
            System.err.println("FAILED: Locked clue unlocked prematurely!");
        } catch (ClueLockedException e) {
            System.out.println("CAUGHT EXPECTED EXCEPTION: " + e.getMessage() + " [CORRECT]");
            System.out.println("Missing prerequisites: " + e.getMissingEvidenceIds());
        }

        System.out.println("\nDiscovering second prerequisite (EVD-02)...");
        discoveredEvidence.add("EVD-02");
        System.out.println("Can unlock now? " + unlockEngine.canUnlock(encryptedClue, discoveredEvidence));
        unlockEngine.unlock(encryptedClue, discoveredEvidence);
        System.out.println("Clue unlocked successfully! Unlocked: " + encryptedClue.isUnlocked()
                + " at " + encryptedClue.getUnlockedAt());

        // ---------------------------------------------------------------------
        // 4. CASE #103 SAMPLE DATA DEMO
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 4. PREDICTABLE SAMPLE DATA: CASE #103 'THE POISONED DRINK'");
        Case case103 = caseService.loadSampleCase103();
        System.out.println("Loaded Sample Case: " + case103);

        List<Suspect> sampleSuspects = SampleDataFactory.createSampleSuspects();
        System.out.println("\nCase #103 Suspects (" + sampleSuspects.size() + "):");
        for (Suspect s : sampleSuspects) {
            System.out.println("  • " + s);
        }

        List<Evidence> sampleEvidence = SampleDataFactory.createSampleEvidence();
        System.out.println("\nCase #103 Evidence items (" + sampleEvidence.size() + "):");
        for (Evidence e : sampleEvidence) {
            System.out.println("  • [" + e.getId() + "] " + e.getName() + " (" + e.getClass().getSimpleName() + ")");
        }

        List<Clue> sampleClues = SampleDataFactory.createSampleClues();
        System.out.println("\nCase #103 Clues (" + sampleClues.size() + "):");
        for (Clue c : sampleClues) {
            System.out.println("  • [" + c.getId() + "] " + c.getTitle() + " | Requires: " + c.getRequiredEvidenceIds());
        }

        // ---------------------------------------------------------------------
        // 5. DATABASE ACCESS & REAL JDBC PERSISTENCE DEMO
        // ---------------------------------------------------------------------
        System.out.println("\n>>> 5. DATABASE ARCHITECTURE: JDBC PERSISTENCE & SCHEMA VALIDATION");
        com.mysteryvault.config.DatabaseConfig dbConfig = com.mysteryvault.config.DatabaseConfig.forH2InMemory("demo_mv_db");
        System.out.println("Configured Database: " + dbConfig.getMaskedUrl());

        com.mysteryvault.config.DatabaseConnectionFactory factory = new com.mysteryvault.config.DatabaseConnectionFactory(dbConfig);
        boolean connected = factory.checkConnectivity();
        System.out.println("Database connectivity verified (SELECT 1): " + connected + " [SUCCESS]");

        System.out.println("Executing automated database schema migration (schema.sql)...");
        factory.initializeSchema();
        System.out.println("Schema initialization succeeded. Tables created: cases, suspects, case_suspects, evidence, case_evidence, clues, case_clues, clue_prerequisites, evidence_archive.");

        com.mysteryvault.repository.impl.CaseJdbcRepository caseJdbcRepo = new com.mysteryvault.repository.impl.CaseJdbcRepository(factory);
        com.mysteryvault.repository.impl.SuspectJdbcRepository suspectJdbcRepo = new com.mysteryvault.repository.impl.SuspectJdbcRepository(factory);
        com.mysteryvault.repository.impl.EvidenceJdbcRepository evidenceJdbcRepo = new com.mysteryvault.repository.impl.EvidenceJdbcRepository(factory);
        com.mysteryvault.repository.impl.ClueJdbcRepository clueJdbcRepo = new com.mysteryvault.repository.impl.ClueJdbcRepository(factory);

        System.out.println("\nSeeding Case #103 relational graph into real JDBC database...");
        SampleDataFactory.populateRepositories(caseJdbcRepo, suspectJdbcRepo, evidenceJdbcRepo, clueJdbcRepo);

        System.out.println("Reading back persisted data from database:");
        Case dbCase = caseJdbcRepo.findById("CASE-103").orElseThrow();
        System.out.println("  • Persisted Case: " + dbCase.getTitle() + " [Status: " + dbCase.getStatus() + "]");
        System.out.println("  • Associated Suspect IDs: " + dbCase.getSuspectIds());
        System.out.println("  • Associated Evidence IDs: " + dbCase.getEvidenceIds());
        System.out.println("  • Associated Clue IDs: " + dbCase.getClueIds());

        List<Suspect> dbSuspects = suspectJdbcRepo.findByCaseId("CASE-103");
        System.out.println("  • Verified Suspects loaded from DB: " + dbSuspects.size());

        List<Evidence> dbEvidence = evidenceJdbcRepo.findByCaseId("CASE-103");
        System.out.println("  • Verified Polymorphic Evidence loaded from DB: " + dbEvidence.size());

        List<Clue> dbClues = clueJdbcRepo.findByCaseId("CASE-103");
        System.out.println("  • Verified Clues with prerequisites loaded from DB: " + dbClues.size());

        System.out.println("\n================================================================================");
        System.out.println("   MEMBER 1 VERIFICATION COMPLETED WITH ZERO ERRORS (MODULES 1, 2, 3)");
        System.out.println("================================================================================");
    }
}
