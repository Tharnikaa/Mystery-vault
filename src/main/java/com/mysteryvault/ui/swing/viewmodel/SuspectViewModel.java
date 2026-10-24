package com.mysteryvault.ui.swing.viewmodel;

public class SuspectViewModel {
    private final String id;
    private final String name;
    private final String role;
    private final String occupation;
    private final int age;
    private final String status;
    private final String alibi;
    private final String lastLocation;
    private final String knownConnection;
    private final int suspicion;
    private final int trust;
    private final String imageKey;
    private final boolean defaultExpanded;

    public SuspectViewModel(String id, String name, String role, int age, String status,
                            String lastLocation, String knownConnection, int suspicion, int trust,
                            String imageKey, boolean defaultExpanded) {
        this(id, name, role, role, age, status, lastLocation, lastLocation, knownConnection, suspicion, trust, imageKey, defaultExpanded);
    }

    public SuspectViewModel(String id, String name, String role, String occupation, int age, String status,
                            String alibi, String lastLocation, String knownConnection, int suspicion, int trust,
                            String imageKey, boolean defaultExpanded) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.occupation = occupation != null ? occupation : role;
        this.age = age;
        this.status = status;
        this.alibi = alibi != null ? alibi : "No statement provided.";
        this.lastLocation = lastLocation;
        this.knownConnection = knownConnection;
        this.suspicion = suspicion;
        this.trust = trust;
        this.imageKey = imageKey;
        this.defaultExpanded = defaultExpanded;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getOccupation() { return occupation; }
    public int getAge() { return age; }
    public String getStatus() { return status; }
    public String getAlibi() { return alibi; }
    public String getLastLocation() { return lastLocation; }
    public String getKnownConnection() { return knownConnection; }
    public int getSuspicion() { return suspicion; }
    public int getTrust() { return trust; }
    public String getImageKey() { return imageKey; }
    public boolean isDefaultExpanded() { return defaultExpanded; }
}
