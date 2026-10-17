package com.mysteryvault.ui.swing.viewmodel;

public class CaseCardViewModel {
    private final String id;
    private final String title;
    private final String duration;
    private final String statusBadge;
    private final String badgeType; // SOLVED, IN_PROGRESS, LOCKED
    private final String imageKey;
    private final boolean selectedDefault;

    public CaseCardViewModel(String id, String title, String duration, String statusBadge, String badgeType, String imageKey, boolean selectedDefault) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.statusBadge = statusBadge;
        this.badgeType = badgeType;
        this.imageKey = imageKey;
        this.selectedDefault = selectedDefault;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    public String getStatusBadge() { return statusBadge; }
    public String getBadgeType() { return badgeType; }
    public String getImageKey() { return imageKey; }
    public boolean isSelectedDefault() { return selectedDefault; }
}
