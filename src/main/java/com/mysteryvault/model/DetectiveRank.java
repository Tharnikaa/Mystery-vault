package com.mysteryvault.model;

/**
 * Centralized, deterministic progression system for Detective ranks.
 * Encapsulates XP thresholds in one place to avoid magic numbers.
 */
public enum DetectiveRank {
    ROOKIE("Rookie Detective", 0),
    JUNIOR("Junior Detective", 100),
    DETECTIVE("Detective", 300),
    SENIOR("Senior Detective", 600),
    MASTER("Master Detective", 1000);

    private final String title;
    private final int requiredXp;

    DetectiveRank(String title, int requiredXp) {
        this.title = title;
        this.requiredXp = requiredXp;
    }

    public String getTitle() {
        return title;
    }

    public int getRequiredXp() {
        return requiredXp;
    }

    /**
     * Calculates the deterministic detective rank from accumulated XP.
     */
    public static DetectiveRank fromXp(int xp) {
        if (xp >= MASTER.requiredXp) return MASTER;
        if (xp >= SENIOR.requiredXp) return SENIOR;
        if (xp >= DETECTIVE.requiredXp) return DETECTIVE;
        if (xp >= JUNIOR.requiredXp) return JUNIOR;
        return ROOKIE;
    }

    public static String getRankTitle(int xp) {
        return fromXp(xp).getTitle();
    }
}
