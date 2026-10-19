package com.mysteryvault.analyzer;

import com.mysteryvault.model.TimelineEvent;
import com.mysteryvault.util.DateTimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Task that performs timeline analysis on a set of {@link TimelineEvent}s.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Sort events chronologically</li>
 *   <li>Detect significant unexplained time gaps between consecutive events</li>
 *   <li>Identify temporal overlaps and actor location collisions (impossible co-presence)</li>
 *   <li>Calculate high-level timeline metadata (earliest/latest timestamps, total span, participants)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Note on Team Architecture &amp; Integration:</b><br>
 * Person 3 owns {@code com.mysteryvault.analyzer.Analyzer}. To avoid inventing a competing
 * interface contract or blocking independent testing, this class provides direct, testable
 * execution methods ({@link #execute()} and {@link #analyze()}). Once Person 3's contract
 * is finalized, this class can seamlessly declare {@code implements Analyzer}.
 * </p>
 */
public class TimelineAnalyzerTask {

    public static final Duration DEFAULT_GAP_THRESHOLD = Duration.ofHours(1);

    private final String caseId;
    private final List<TimelineEvent> events;
    private final Duration gapThreshold;

    /**
     * Constructs a TimelineAnalyzerTask with default gap threshold (1 hour).
     *
     * @param caseId Identifier of the case
     * @param events List of events to analyze
     */
    public TimelineAnalyzerTask(String caseId, List<TimelineEvent> events) {
        this(caseId, events, DEFAULT_GAP_THRESHOLD);
    }

    /**
     * Constructs a TimelineAnalyzerTask with a custom time gap threshold.
     *
     * @param caseId       Identifier of the case
     * @param events       List of events to analyze
     * @param gapThreshold Minimum duration gap considered noteworthy
     */
    public TimelineAnalyzerTask(String caseId, List<TimelineEvent> events, Duration gapThreshold) {
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new IllegalArgumentException("caseId must not be null or blank");
        }
        this.caseId = caseId.trim();
        this.events = (events == null) ? Collections.emptyList() : new ArrayList<>(events);
        this.gapThreshold = (gapThreshold == null || gapThreshold.isNegative())
                ? DEFAULT_GAP_THRESHOLD
                : gapThreshold;
    }

    /**
     * Primary execution method that analyzes the timeline events.
     *
     * @return structured {@link TimelineAnalysisResult}
     */
    public TimelineAnalysisResult execute() {
        List<TimelineEvent> sorted = DateTimeUtils.sortChronologically(events);

        if (sorted.isEmpty()) {
            return new TimelineAnalysisResult(
                    caseId,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null,
                    null,
                    Duration.ZERO,
                    Collections.emptySet(),
                    Collections.emptySet()
            );
        }

        List<TimelineAnalysisResult.TimelineGap> gaps = detectGaps(sorted);
        List<TimelineAnalysisResult.TimelineOverlap> overlaps = new ArrayList<>();
        List<TimelineAnalysisResult.ActorLocationCollision> collisions = new ArrayList<>();
        detectOverlapsAndCollisions(sorted, overlaps, collisions);

        LocalDateTime earliest = sorted.get(0).getTimestamp();
        LocalDateTime latest = sorted.get(0).getEndTimestamp();
        Set<String> actors = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> locations = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (TimelineEvent event : sorted) {
            if (event.getEndTimestamp().isAfter(latest)) {
                latest = event.getEndTimestamp();
            }
            if (!event.getActor().isEmpty()) {
                actors.add(event.getActor());
            }
            if (!event.getLocation().isEmpty()) {
                locations.add(event.getLocation());
            }
        }

        Duration totalSpan = Duration.between(earliest, latest);

        return new TimelineAnalysisResult(
                caseId,
                sorted,
                gaps,
                overlaps,
                collisions,
                earliest,
                latest,
                totalSpan,
                actors,
                locations
        );
    }

    /**
     * Alias for {@link #execute()} for convenient calling semantics.
     */
    public TimelineAnalysisResult analyze() {
        return execute();
    }

    /**
     * Detects chronological gaps between consecutive events that exceed the threshold.
     */
    private List<TimelineAnalysisResult.TimelineGap> detectGaps(List<TimelineEvent> sorted) {
        List<TimelineAnalysisResult.TimelineGap> gaps = new ArrayList<>();
        for (int i = 0; i < sorted.size() - 1; i++) {
            TimelineEvent current = sorted.get(i);
            TimelineEvent next = sorted.get(i + 1);

            // A gap occurs if the next event starts strictly after current event ends
            if (next.getTimestamp().isAfter(current.getEndTimestamp())) {
                Duration gap = Duration.between(current.getEndTimestamp(), next.getTimestamp());
                if (gap.compareTo(gapThreshold) >= 0) {
                    gaps.add(new TimelineAnalysisResult.TimelineGap(current, next, gap));
                }
            }
        }
        return gaps;
    }

    /**
     * Detects interval overlaps and simultaneous location collisions for the same actor.
     */
    private void detectOverlapsAndCollisions(List<TimelineEvent> sorted,
                                            List<TimelineAnalysisResult.TimelineOverlap> overlaps,
                                            List<TimelineAnalysisResult.ActorLocationCollision> collisions) {
        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                TimelineEvent e1 = sorted.get(i);
                TimelineEvent e2 = sorted.get(j);

                if (DateTimeUtils.doEventsOverlap(e1, e2)) {
                    overlaps.add(new TimelineAnalysisResult.TimelineOverlap(e1, e2));

                    // Check for actor location collision: same person at two different locations simultaneously
                    if (!e1.getActor().isEmpty() && e1.getActor().equalsIgnoreCase(e2.getActor())) {
                        if (!e1.getLocation().isEmpty() && !e2.getLocation().isEmpty()
                                && !e1.getLocation().equalsIgnoreCase(e2.getLocation())) {
                            String explanation = String.format(
                                    "Actor '%s' is reported simultaneously at '%s' (Event %s) and '%s' (Event %s).",
                                    e1.getActor(), e1.getLocation(), e1.getId(), e2.getLocation(), e2.getId());
                            collisions.add(new TimelineAnalysisResult.ActorLocationCollision(
                                    e1.getActor(), e1, e2, explanation));
                        }
                    }
                }
            }
        }
    }

    public String getCaseId() {
        return caseId;
    }

    public Duration getGapThreshold() {
        return gapThreshold;
    }
}
