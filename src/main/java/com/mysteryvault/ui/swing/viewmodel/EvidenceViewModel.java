package com.mysteryvault.ui.swing.viewmodel;

public class EvidenceViewModel {
    private final String id;
    private final String title;
    private final String type;
    private final String location;
    private final String timeCollected;
    private final String description;
    private final String imageKey;
    private final String associatedPersonId;
    private final String significance;
    private final boolean discovered;

    public EvidenceViewModel(String id, String title, String type, String location, String timeCollected, String description, String imageKey) {
        this(id, title, type, location, timeCollected, description, imageKey, null, "MEDIUM", true);
    }

    public EvidenceViewModel(String id, String title, String type, String location, String timeCollected, String description, String imageKey,
                             String associatedPersonId, String significance, boolean discovered) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.location = location;
        this.timeCollected = timeCollected;
        this.description = description;
        this.imageKey = imageKey;
        this.associatedPersonId = associatedPersonId;
        this.significance = significance != null ? significance : "MEDIUM";
        this.discovered = discovered;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getLocation() { return location; }
    public String getTimeCollected() { return timeCollected; }
    public String getDescription() { return description; }
    public String getImageKey() { return imageKey; }
    public String getAssociatedPersonId() { return associatedPersonId; }
    public String getSignificance() { return significance; }
    public boolean isDiscovered() { return discovered; }
}
