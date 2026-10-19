package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.TimelineViewPort;
import com.mysteryvault.ui.swing.viewmodel.TimelineEventViewModel;

import java.util.ArrayList;
import java.util.List;

public class MockTimelineViewPort implements TimelineViewPort {

    private final List<TimelineEventViewModel> events = new ArrayList<>();

    public MockTimelineViewPort() {
        events.add(new TimelineEventViewModel("T1", "08:15 PM", "Victim enters restaurant with guests", false));
        events.add(new TimelineEventViewModel("T2", "08:32 PM", "Arjun seen near the bar talking to staff", false));
        events.add(new TimelineEventViewModel("T3", "08:45 PM", "Meera leaves the table towards the private salon", true));
        events.add(new TimelineEventViewModel("T4", "09:10 PM", "Unknown phone call received on victim's device", false));
        events.add(new TimelineEventViewModel("T5", "09:25 PM", "Victim collapses and becomes unconscious", false));
        events.add(new TimelineEventViewModel("T6", "09:40 PM", "Rohan enters kitchen and disposes drink tray", false));
    }

    @Override
    public List<TimelineEventViewModel> getTimelineForCase(String caseId) {
        return new ArrayList<>(events);
    }

    @Override
    public void addEvent(String caseId, String time, String description) {
        events.add(new TimelineEventViewModel("T" + (events.size() + 1), time, description, false));
    }
}
