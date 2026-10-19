package com.mysteryvault;

import com.mysteryvault.enums.CaseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CaseStatusTest {

    @Test
    @DisplayName("OPEN can only transition to ACTIVE")
    void testOpenTransitions() {
        assertTrue(CaseStatus.OPEN.canTransitionTo(CaseStatus.ACTIVE));
        assertFalse(CaseStatus.OPEN.canTransitionTo(CaseStatus.COLD));
        assertFalse(CaseStatus.OPEN.canTransitionTo(CaseStatus.SOLVED));
        assertFalse(CaseStatus.OPEN.canTransitionTo(CaseStatus.ARCHIVED));
        assertFalse(CaseStatus.OPEN.canTransitionTo(CaseStatus.OPEN));
        assertFalse(CaseStatus.OPEN.canTransitionTo(null));

        assertEquals(Set.of(CaseStatus.ACTIVE), CaseStatus.OPEN.getAllowedNextStatuses());
    }

    @Test
    @DisplayName("ACTIVE can transition to COLD, SOLVED, or ARCHIVED")
    void testActiveTransitions() {
        assertTrue(CaseStatus.ACTIVE.canTransitionTo(CaseStatus.COLD));
        assertTrue(CaseStatus.ACTIVE.canTransitionTo(CaseStatus.SOLVED));
        assertTrue(CaseStatus.ACTIVE.canTransitionTo(CaseStatus.ARCHIVED));
        assertFalse(CaseStatus.ACTIVE.canTransitionTo(CaseStatus.OPEN));
        assertFalse(CaseStatus.ACTIVE.canTransitionTo(CaseStatus.ACTIVE));

        assertEquals(Set.of(CaseStatus.COLD, CaseStatus.SOLVED, CaseStatus.ARCHIVED),
                CaseStatus.ACTIVE.getAllowedNextStatuses());
    }

    @Test
    @DisplayName("COLD can transition to ACTIVE or ARCHIVED")
    void testColdTransitions() {
        assertTrue(CaseStatus.COLD.canTransitionTo(CaseStatus.ACTIVE));
        assertTrue(CaseStatus.COLD.canTransitionTo(CaseStatus.ARCHIVED));
        assertFalse(CaseStatus.COLD.canTransitionTo(CaseStatus.OPEN));
        assertFalse(CaseStatus.COLD.canTransitionTo(CaseStatus.SOLVED));

        assertEquals(Set.of(CaseStatus.ACTIVE, CaseStatus.ARCHIVED),
                CaseStatus.COLD.getAllowedNextStatuses());
    }

    @Test
    @DisplayName("SOLVED can transition to ARCHIVED only")
    void testSolvedTransitions() {
        assertTrue(CaseStatus.SOLVED.canTransitionTo(CaseStatus.ARCHIVED));
        assertFalse(CaseStatus.SOLVED.canTransitionTo(CaseStatus.OPEN));
        assertFalse(CaseStatus.SOLVED.canTransitionTo(CaseStatus.ACTIVE));
        assertFalse(CaseStatus.SOLVED.canTransitionTo(CaseStatus.COLD));

        assertEquals(Set.of(CaseStatus.ARCHIVED), CaseStatus.SOLVED.getAllowedNextStatuses());
    }

    @Test
    @DisplayName("ARCHIVED is terminal and cannot transition to any state")
    void testArchivedTransitions() {
        assertTrue(CaseStatus.ARCHIVED.isTerminal());
        assertFalse(CaseStatus.ARCHIVED.canTransitionTo(CaseStatus.OPEN));
        assertFalse(CaseStatus.ARCHIVED.canTransitionTo(CaseStatus.ACTIVE));
        assertFalse(CaseStatus.ARCHIVED.canTransitionTo(CaseStatus.COLD));
        assertFalse(CaseStatus.ARCHIVED.canTransitionTo(CaseStatus.SOLVED));
        assertFalse(CaseStatus.ARCHIVED.canTransitionTo(CaseStatus.ARCHIVED));

        assertTrue(CaseStatus.ARCHIVED.getAllowedNextStatuses().isEmpty());
    }
}
