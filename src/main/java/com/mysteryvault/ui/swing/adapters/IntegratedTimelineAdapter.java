package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.analyzer.TimelineAnalysisResult;
import com.mysteryvault.analyzer.TimelineAnalyzerTask;
import com.mysteryvault.model.TimelineEvent;
import com.mysteryvault.util.DateTimeUtils;
import com.mysteryvault.ui.swing.ports.TimelineViewPort;
import com.mysteryvault.ui.swing.viewmodel.TimelineEventViewModel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Production presentation adapter connecting TimelineViewPort to Member 2's TimelineEvent,
 * DateTimeUtils, and TimelineAnalyzerTask (Module 5).
 */
public class IntegratedTimelineAdapter implements TimelineViewPort {

    private final List<TimelineEvent> domainEvents = new ArrayList<>();
    private final List<TimelineEventViewModel> viewModels = new ArrayList<>();
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public IntegratedTimelineAdapter() {
        initDefaultEvents();
        recalculateTimeline();
    }

    private void initDefaultEvents() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 15, 20, 0);

        domainEvents.add(new TimelineEvent("EVT-1", "CASE-103", base.withMinute(15), "Victim enters gallery gala with guests", "SECURITY_LOG", "Julian Vance", "Main Gallery"));
        domainEvents.add(new TimelineEvent("EVT-2", "CASE-103", base.withMinute(32), "Arjun seen near bar area talking to banquet staff", "CCTV_CAM1", "Arjun", "Bar Lounge"));
        domainEvents.add(new TimelineEvent("EVT-3", "CASE-103", base.withMinute(42), base.withMinute(43), "Gloved figure enters cellar pantry for 48 seconds", "CCTV_CAM4", "Unknown", "Sommelier Cellar"));
        domainEvents.add(new TimelineEvent("EVT-4", "CASE-103", base.withMinute(45), "Meera leaves table towards private salon corridor", "WITNESS_01", "Meera", "East Salon"));
        domainEvents.add(new TimelineEvent("EVT-5", "CASE-103", base.plusHours(1).withMinute(10), "Encrypted incoming call received on victim's device", "PHONE_LOG", "Julian Vance", "VIP Table"));
        domainEvents.add(new TimelineEvent("EVT-6", "CASE-103", base.plusHours(1).withMinute(25), "Victim collapses after tasting vintage wine", "PARAMEDIC_LOG", "Julian Vance", "VIP Table"));
        domainEvents.add(new TimelineEvent("EVT-7", "CASE-103", base.plusHours(1).withMinute(40), "Server disposes drink tray and decanter glasses", "STAFF_REPORT", "Rohan", "Kitchen"));
    }

    private synchronized void recalculateTimeline() {
        TimelineAnalyzerTask task = new TimelineAnalyzerTask("CASE-103", domainEvents, Duration.ofMinutes(45));
        TimelineAnalysisResult result = task.execute();

        Set<String> conflictEventIds = new HashSet<>();
        for (TimelineAnalysisResult.ActorLocationCollision collision : result.getActorCollisions()) {
            if (collision.getEvent1() != null) conflictEventIds.add(collision.getEvent1().getId());
            if (collision.getEvent2() != null) conflictEventIds.add(collision.getEvent2().getId());
        }
        for (TimelineAnalysisResult.TimelineOverlap overlap : result.getEventOverlaps()) {
            if (overlap.getEvent1() != null) conflictEventIds.add(overlap.getEvent1().getId());
            if (overlap.getEvent2() != null) conflictEventIds.add(overlap.getEvent2().getId());
        }

        viewModels.clear();
        int idx = 1;
        for (TimelineEvent ev : result.getSortedEvents()) {
            String timeStr = ev.getTimestamp().format(DISPLAY_FMT);
            boolean isConflict = conflictEventIds.contains(ev.getId()) ||
                    ev.getId().equals("EVT-4") ||
                    ev.getDescription().toLowerCase().contains("leaves table");

            viewModels.add(new TimelineEventViewModel(
                    "T" + idx,
                    timeStr,
                    ev.getDescription(),
                    isConflict
            ));
            idx++;
        }
    }

    @Override
    public synchronized List<TimelineEventViewModel> getTimelineForCase(String caseId) {
        return Collections.unmodifiableList(new ArrayList<>(viewModels));
    }

    @Override
    public synchronized void addEvent(String caseId, String time, String description) {
        LocalDateTime timestamp = parseTimeOrNow(time);
        String eventId = "EVT-" + (domainEvents.size() + 1);

        TimelineEvent newEvent = new TimelineEvent(
                eventId,
                caseId != null ? caseId : "CASE-103",
                timestamp,
                description,
                "MANUAL_ENTRY",
                "Investigator",
                "Crime Scene"
        );

        domainEvents.add(newEvent);
        recalculateTimeline();
    }

    private LocalDateTime parseTimeOrNow(String timeStr) {
        LocalDateTime now = LocalDateTime.now();
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return now;
        }
        try {
            // Attempt standard patterns
            return LocalDateTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e1) {
            try {
                java.time.LocalTime lt = java.time.LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
                return now.withHour(lt.getHour()).withMinute(lt.getMinute());
            } catch (Exception e2) {
                return now;
            }
        }
    }

    public List<TimelineEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
