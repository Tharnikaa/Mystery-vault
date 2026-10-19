package com.mysteryvault.ui.swing.viewmodel;

public class TimelineEventViewModel {
    private final String id;
    private final String time;
    private final String description;
    private final boolean hasConflict;

    public TimelineEventViewModel(String id, String time, String description, boolean hasConflict) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.hasConflict = hasConflict;
    }

    public String getId() { return id; }
    public String getTime() { return time; }
    public String getDescription() { return description; }
    public boolean isHasConflict() { return hasConflict; }
}
