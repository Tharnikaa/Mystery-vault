package com.mysteryvault;

import com.mysteryvault.enums.CaseStatus;
import com.mysteryvault.exception.CaseNotFoundException;
import com.mysteryvault.exception.CaseValidationException;
import com.mysteryvault.exception.InvalidCaseTransitionException;
import com.mysteryvault.model.Case;
import com.mysteryvault.model.Priority;
import com.mysteryvault.service.CaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CaseServiceTest {

    private CaseService caseService;

    @BeforeEach
    void setUp() {
        caseService = new CaseService();
    }

    @Test
    @DisplayName("Create and retrieve case successfully")
    void testCreateAndFindCase() {
        Case c = new Case("C-01", "The Red Room", "Homicide mystery", Priority.HIGH, "Inspector Ross");
        Case created = caseService.createCase(c);

        assertNotNull(created);
        assertEquals("C-01", created.getId());
        assertEquals("The Red Room", created.getTitle());
        assertEquals(CaseStatus.OPEN, created.getStatus());

        Optional<Case> found = caseService.findById("C-01");
        assertTrue(found.isPresent());
        assertEquals("The Red Room", found.get().getTitle());
    }

    @Test
    @DisplayName("Creating case with duplicate ID throws CaseValidationException")
    void testDuplicateCaseIdThrows() {
        Case c1 = new Case("C-DUP", "First Case", "Desc", Priority.LOW, "Lead");
        caseService.createCase(c1);

        Case c2 = new Case("C-DUP", "Second Case", "Desc", Priority.LOW, "Lead");
        assertThrows(CaseValidationException.class, () -> caseService.createCase(c2));
    }

    @Test
    @DisplayName("Creating case with blank title throws CaseValidationException")
    void testBlankTitleThrows() {
        Case badCase = new Case("C-BAD", "   ", "Desc", Priority.LOW, "Lead");
        assertThrows(CaseValidationException.class, () -> caseService.createCase(badCase));
    }

    @Test
    @DisplayName("Status transitions follow state machine and throw on illegal transitions")
    void testStatusTransitions() {
        Case c = new Case("C-TRANS", "Transition Test", "Desc", Priority.MEDIUM, "Lead");
        caseService.createCase(c);

        // Legal: OPEN -> ACTIVE
        Case active = caseService.transitionCaseStatus("C-TRANS", CaseStatus.ACTIVE);
        assertEquals(CaseStatus.ACTIVE, active.getStatus());

        // Illegal: ACTIVE -> OPEN
        assertThrows(InvalidCaseTransitionException.class, () ->
                caseService.transitionCaseStatus("C-TRANS", CaseStatus.OPEN));

        // Legal: ACTIVE -> COLD
        Case cold = caseService.transitionCaseStatus("C-TRANS", CaseStatus.COLD);
        assertEquals(CaseStatus.COLD, cold.getStatus());

        // Legal: COLD -> ACTIVE
        Case reopened = caseService.transitionCaseStatus("C-TRANS", CaseStatus.ACTIVE);
        assertEquals(CaseStatus.ACTIVE, reopened.getStatus());

        // Legal: ACTIVE -> SOLVED
        Case solved = caseService.transitionCaseStatus("C-TRANS", CaseStatus.SOLVED);
        assertEquals(CaseStatus.SOLVED, solved.getStatus());

        // Legal: SOLVED -> ARCHIVED
        Case archived = caseService.transitionCaseStatus("C-TRANS", CaseStatus.ARCHIVED);
        assertEquals(CaseStatus.ARCHIVED, archived.getStatus());

        // Illegal: ARCHIVED -> any
        assertThrows(InvalidCaseTransitionException.class, () ->
                caseService.transitionCaseStatus("C-TRANS", CaseStatus.ACTIVE));
    }

    @Test
    @DisplayName("Adding suspect, evidence, and clues by ID")
    void testAddRelations() {
        Case c = new Case("C-REL", "Relational Test", "Desc", Priority.LOW, "Lead");
        caseService.createCase(c);

        caseService.addSuspectToCase("C-REL", "SUS-1");
        caseService.addEvidenceToCase("C-REL", "EVD-1");
        caseService.addClueToCase("C-REL", "CLU-1");

        Case updated = caseService.getCaseOrThrow("C-REL");
        assertTrue(updated.getSuspectIds().contains("SUS-1"));
        assertTrue(updated.getEvidenceIds().contains("EVD-1"));
        assertTrue(updated.getClueIds().contains("CLU-1"));
    }

    @Test
    @DisplayName("Non-existent case throws CaseNotFoundException")
    void testCaseNotFound() {
        assertThrows(CaseNotFoundException.class, () -> caseService.getCaseOrThrow("NON-EXISTENT"));
    }

    @Test
    @DisplayName("Load sample Case #103")
    void testLoadSampleCase103() {
        Case sample = caseService.loadSampleCase103();
        assertEquals("CASE-103", sample.getId());
        assertEquals("The Poisoned Drink", sample.getTitle());
        assertEquals(3, sample.getSuspectIds().size());
        assertEquals(3, sample.getEvidenceIds().size());
        assertEquals(2, sample.getClueIds().size());
    }
}
