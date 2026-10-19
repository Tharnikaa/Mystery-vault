package com.mysteryvault;

import com.mysteryvault.exception.ClueLockedException;
import com.mysteryvault.model.Clue;
import com.mysteryvault.service.ClueUnlockEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClueUnlockEngineTest {

    private ClueUnlockEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ClueUnlockEngine();
    }

    @Test
    @DisplayName("Clue without prerequisites unlocks freely")
    void testClueWithoutPrerequisites() {
        Clue clue = new Clue("CLU-01", "CASE-01", "Obvious Observation", "Footprints outside window");
        assertTrue(engine.canUnlock(clue, Collections.emptySet()));

        engine.unlock(clue, Collections.emptySet());
        assertTrue(clue.isUnlocked());
        assertNotNull(clue.getUnlockedAt());
    }

    @Test
    @DisplayName("Clue with missing prerequisites cannot unlock and throws ClueLockedException")
    void testClueMissingPrerequisitesThrows() {
        Clue clue = new Clue("CLU-02", "CASE-01", "Decrypted Diary", "Hidden safe combination",
                Set.of("EVD-01", "EVD-02"));

        assertFalse(engine.canUnlock(clue, Collections.emptySet()));
        assertFalse(engine.canUnlock(clue, Set.of("EVD-01"))); // partial only

        Set<String> missing = engine.getMissingPrerequisites(clue, Set.of("EVD-01"));
        assertEquals(Set.of("EVD-02"), missing);

        ClueLockedException ex = assertThrows(ClueLockedException.class, () ->
                engine.unlock(clue, Set.of("EVD-01")));

        assertEquals("CLU-02", ex.getClueId());
        assertEquals(Set.of("EVD-02"), ex.getMissingEvidenceIds());
        assertFalse(clue.isUnlocked());
    }

    @Test
    @DisplayName("Clue unlocks successfully when all prerequisites are fulfilled")
    void testClueUnlocksSuccessfully() {
        Clue clue = new Clue("CLU-03", "CASE-01", "Motive Solved", "Insurance payout discovery",
                Set.of("EVD-A", "EVD-B"));

        Set<String> discovered = new HashSet<>(Set.of("EVD-A", "EVD-B", "EVD-C")); // superset

        assertTrue(engine.canUnlock(clue, discovered));
        assertTrue(engine.getMissingPrerequisites(clue, discovered).isEmpty());

        engine.unlock(clue, discovered);
        assertTrue(clue.isUnlocked());
        assertNotNull(clue.getUnlockedAt());

        // Subsequent unlock call should be a safe no-op
        assertDoesNotThrow(() -> engine.unlock(clue, Collections.emptySet()));
    }
}
