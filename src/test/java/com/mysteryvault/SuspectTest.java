package com.mysteryvault;

import com.mysteryvault.model.Person;
import com.mysteryvault.model.Suspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SuspectTest {

    @Test
    @DisplayName("Suspect extends Person and preserves attributes")
    void testSuspectInheritance() {
        Suspect suspect = new Suspect("SUS-01", "Jonathan Black", "Antique Dealer", 45,
                "At home reading", 60, "Inherits antique collection", false);

        assertTrue(suspect instanceof Person);
        assertEquals("SUS-01", suspect.getId());
        assertEquals("Jonathan Black", suspect.getName());
        assertEquals("Suspect", suspect.getRole());
        assertEquals("Antique Dealer", suspect.getOccupation());
        assertEquals(45, suspect.getAge());
        assertEquals(60, suspect.getSuspicionScore());
        assertEquals("At home reading", suspect.getAlibi());
        assertEquals("Inherits antique collection", suspect.getMotiveDescription());
        assertFalse(suspect.isAlibiVerified());
    }

    @Test
    @DisplayName("Suspicion score boundary validation (0 to 100)")
    void testSuspicionScoreBoundaries() {
        Suspect s = new Suspect("SUS-02", "Jane Doe", "Chef", 30, "Kitchen", 0);
        assertEquals(0, s.getSuspicionScore());

        s.setSuspicionScore(100);
        assertEquals(100, s.getSuspicionScore());

        assertThrows(IllegalArgumentException.class, () -> s.setSuspicionScore(-1));
        assertThrows(IllegalArgumentException.class, () -> s.setSuspicionScore(101));
    }

    @Test
    @DisplayName("Age boundary validation (0 to 150)")
    void testAgeBoundaries() {
        Suspect s = new Suspect("SUS-03", "Old John", "Retired", 70, "Park", 10);
        assertThrows(IllegalArgumentException.class, () -> s.setAge(-5));
        assertThrows(IllegalArgumentException.class, () -> s.setAge(160));
    }
}
