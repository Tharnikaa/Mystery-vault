package com.mysteryvault.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Legal lifecycle states for a criminal case investigation (Module 1).
 * Defines deterministic state machine transitions:
 * - OPEN -> ACTIVE
 * - ACTIVE -> COLD, SOLVED, ARCHIVED
 * - COLD -> ACTIVE, ARCHIVED
 * - SOLVED -> ARCHIVED
 * - ARCHIVED -> Terminal state (no transitions allowed)
 */
public enum CaseStatus {
    OPEN,
    ACTIVE,
    COLD,
    SOLVED,
    ARCHIVED;

    /**
     * Determines whether transitioning from this status to target status is legally permitted.
     *
     * @param target Status to transition into
     * @return true if legal transition, false otherwise
     */
    public boolean canTransitionTo(CaseStatus target) {
        if (target == null || this == target) {
            return false;
        }

        switch (this) {
            case OPEN:
                return target == ACTIVE;
            case ACTIVE:
                return target == COLD || target == SOLVED || target == ARCHIVED;
            case COLD:
                return target == ACTIVE || target == ARCHIVED;
            case SOLVED:
                return target == ARCHIVED;
            case ARCHIVED:
            default:
                return false;
        }
    }

    /**
     * Returns an unmodifiable set of allowed next statuses from the current status.
     *
     * @return Unmodifiable set of legal next CaseStatus values
     */
    public Set<CaseStatus> getAllowedNextStatuses() {
        switch (this) {
            case OPEN:
                return Collections.unmodifiableSet(EnumSet.of(ACTIVE));
            case ACTIVE:
                return Collections.unmodifiableSet(EnumSet.of(COLD, SOLVED, ARCHIVED));
            case COLD:
                return Collections.unmodifiableSet(EnumSet.of(ACTIVE, ARCHIVED));
            case SOLVED:
                return Collections.unmodifiableSet(EnumSet.of(ARCHIVED));
            case ARCHIVED:
            default:
                return Collections.emptySet();
        }
    }

    /**
     * Checks if this status is terminal (cannot transition further).
     */
    public boolean isTerminal() {
        return this == ARCHIVED;
    }
}
