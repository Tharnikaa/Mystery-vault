package com.mysteryvault.enums;

/**
 * Represents the severity level of an investigative contradiction.
 */
public enum Severity {

    /**
     * Minor discrepancy or slight time drift without direct physical impossibility.
     */
    LOW(1, "Minor discrepancy or negligible time variance"),

    /**
     * Questionable divergence, such as improbable travel speed or unverified presence.
     */
    MEDIUM(2, "Plausibility issue or tight travel constraint"),

    /**
     * Direct factual collision, such as identical claimed times in mutually exclusive locations.
     */
    HIGH(3, "Direct conflicting claims for person whereabouts"),

    /**
     * Irrefutable physical impossibility or complete destruction of an alibi by objective facts.
     */
    CRITICAL(4, "Objective evidence conclusively refutes claimed alibi");

    private final int weight;
    private final String description;

    Severity(int weight, String description) {
        this.weight = weight;
        this.description = description;
    }

    public int getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this severity has a higher investigative impact than another.
     */
    public boolean isHigherThan(Severity other) {
        if (other == null) return true;
        return this.weight > other.weight;
    }
}
