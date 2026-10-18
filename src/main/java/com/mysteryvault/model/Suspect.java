package com.mysteryvault.model;

import java.util.Objects;

/**
 * Suspect domain model extending Person.
 * Encapsulates suspect-specific attributes including occupation, age, alibi,
 * motive, and suspicion score (0-100).
 */
public class Suspect extends Person {
    private static final long serialVersionUID = 1L;

    private String occupation;
    private int age;
    private String alibi;
    private int suspicionScore; // 0 to 100
    private String motiveDescription;
    private boolean alibiVerified;

    public Suspect() {
        super();
        this.occupation = "";
        this.alibi = "";
        this.motiveDescription = "";
        this.suspicionScore = 0;
    }

    public Suspect(String id, String name, String occupation, int age, String alibi, int suspicionScore) {
        this(id, name, occupation, age, alibi, suspicionScore, "", false);
    }

    public Suspect(String id, String name, String occupation, int age, String alibi,
                   int suspicionScore, String motiveDescription, boolean alibiVerified) {
        super(id, name, "Suspect");
        this.occupation = occupation != null ? occupation.trim() : "";
        setAge(age);
        this.alibi = alibi != null ? alibi.trim() : "";
        setSuspicionScore(suspicionScore);
        this.motiveDescription = motiveDescription != null ? motiveDescription.trim() : "";
        this.alibiVerified = alibiVerified;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation != null ? occupation.trim() : "";
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150. Given: " + age);
        }
        this.age = age;
    }

    public String getAlibi() {
        return alibi;
    }

    public void setAlibi(String alibi) {
        this.alibi = alibi != null ? alibi.trim() : "";
    }

    public int getSuspicionScore() {
        return suspicionScore;
    }

    public void setSuspicionScore(int suspicionScore) {
        if (suspicionScore < 0 || suspicionScore > 100) {
            throw new IllegalArgumentException("Suspicion score must be between 0 and 100. Given: " + suspicionScore);
        }
        this.suspicionScore = suspicionScore;
    }

    public String getMotiveDescription() {
        return motiveDescription;
    }

    public void setMotiveDescription(String motiveDescription) {
        this.motiveDescription = motiveDescription != null ? motiveDescription.trim() : "";
    }

    public boolean isAlibiVerified() {
        return alibiVerified;
    }

    public void setAlibiVerified(boolean alibiVerified) {
        this.alibiVerified = alibiVerified;
    }

    @Override
    public String toString() {
        return String.format("Suspect{id='%s', name='%s', occupation='%s', age=%d, suspicion=%d%%, alibiVerified=%s}",
                getId(), getName(), occupation, age, suspicionScore, alibiVerified);
    }
}
