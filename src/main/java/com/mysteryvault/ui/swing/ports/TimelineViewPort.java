package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.TimelineEventViewModel;
import java.util.List;

/**
 * Presentation port for chronological events and timeline conflict detection.
 * Future integration: Member 2 (TimelineEvent / Module 5 & Contradictions / Module 7).
 */
public interface TimelineViewPort {
    List<TimelineEventViewModel> getTimelineForCase(String caseId);
    void addEvent(String caseId, String time, String description);
}
