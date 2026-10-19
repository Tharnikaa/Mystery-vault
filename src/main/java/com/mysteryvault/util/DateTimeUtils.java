package com.mysteryvault.util;

import com.mysteryvault.model.TimelineEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Utility class providing deterministic date-time comparison, chronological sorting,
 * time-gap calculation, and interval overlap detection for investigative timelines.
 */
public final class DateTimeUtils {

    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DateTimeUtils() {
        // Prevent instantiation of utility class
    }

    /**
     * Compares two LocalDateTime instances deterministically.
     *
     * @param t1 first timestamp
     * @param t2 second timestamp
     * @return negative if t1 < t2, zero if equal, positive if t1 > t2
     * @throws IllegalArgumentException if either timestamp is null
     */
    public static int compare(LocalDateTime t1, LocalDateTime t2) {
        if (t1 == null || t2 == null) {
            throw new IllegalArgumentException("Timestamps for comparison must not be null");
        }
        return t1.compareTo(t2);
    }

    /**
     * Checks if t1 occurs strictly before t2.
     */
    public static boolean isBefore(LocalDateTime t1, LocalDateTime t2) {
        return compare(t1, t2) < 0;
    }

    /**
     * Checks if t1 occurs strictly after t2.
     */
    public static boolean isAfter(LocalDateTime t1, LocalDateTime t2) {
        return compare(t1, t2) > 0;
    }

    /**
     * Checks if two timestamps represent the exact same point in time.
     */
    public static boolean isSameTime(LocalDateTime t1, LocalDateTime t2) {
        return compare(t1, t2) == 0;
    }

    /**
     * Checks if a target timestamp falls within [start, end] inclusive.
     *
     * @param target the timestamp to check
     * @param start  the interval start
     * @param end    the interval end
     * @return true if target is within start and end inclusive
     * @throws IllegalArgumentException if any argument is null or start is after end
     */
    public static boolean isWithinRange(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        if (target == null || start == null || end == null) {
            throw new IllegalArgumentException("Range check arguments must not be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Interval start cannot be after end: " + start + " > " + end);
        }
        return !target.isBefore(start) && !target.isAfter(end);
    }

    /**
     * Returns a new unmodifiable list of timeline events sorted chronologically by start timestamp,
     * then end timestamp, then event ID.
     *
     * @param events input list of events
     * @return chronologically sorted list (new list)
     * @throws IllegalArgumentException if the list contains null elements
     */
    public static List<TimelineEvent> sortChronologically(List<TimelineEvent> events) {
        if (events == null) {
            return Collections.emptyList();
        }
        for (TimelineEvent event : events) {
            if (event == null) {
                throw new IllegalArgumentException("TimelineEvent list contains a null element");
            }
        }
        List<TimelineEvent> copy = new ArrayList<>(events);
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    /**
     * Returns a new unmodifiable list of timeline events sorted reverse-chronologically (newest first).
     *
     * @param events input list of events
     * @return reverse-chronologically sorted list
     * @throws IllegalArgumentException if the list contains null elements
     */
    public static List<TimelineEvent> sortReverseChronologically(List<TimelineEvent> events) {
        if (events == null) {
            return Collections.emptyList();
        }
        for (TimelineEvent event : events) {
            if (event == null) {
                throw new IllegalArgumentException("TimelineEvent list contains a null element");
            }
        }
        List<TimelineEvent> copy = new ArrayList<>(events);
        copy.sort(Collections.reverseOrder());
        return Collections.unmodifiableList(copy);
    }

    /**
     * Calculates the absolute time difference between two timestamps.
     *
     * @param t1 first timestamp
     * @param t2 second timestamp
     * @return absolute {@link Duration} between t1 and t2
     * @throws IllegalArgumentException if either timestamp is null
     */
    public static Duration calculateGap(LocalDateTime t1, LocalDateTime t2) {
        if (t1 == null || t2 == null) {
            throw new IllegalArgumentException("Timestamps for gap calculation must not be null");
        }
        return Duration.between(t1, t2).abs();
    }

    /**
     * Calculates the absolute time difference in whole minutes between two timestamps.
     */
    public static long calculateGapMinutes(LocalDateTime t1, LocalDateTime t2) {
        return calculateGap(t1, t2).toMinutes();
    }

    /**
     * Calculates the idle time gap between two timeline events.
     * If the events overlap or occur simultaneously, returns {@link Duration#ZERO}.
     *
     * @param e1 first event
     * @param e2 second event
     * @return gap duration (ZERO if overlapping)
     * @throws IllegalArgumentException if either event is null
     */
    public static Duration calculateEventGap(TimelineEvent e1, TimelineEvent e2) {
        if (e1 == null || e2 == null) {
            throw new IllegalArgumentException("TimelineEvents for gap calculation must not be null");
        }
        if (doEventsOverlap(e1, e2)) {
            return Duration.ZERO;
        }

        // Determine which event is earlier
        TimelineEvent earlier = e1.compareTo(e2) <= 0 ? e1 : e2;
        TimelineEvent later = earlier == e1 ? e2 : e1;

        if (earlier.getEndTimestamp().isBefore(later.getTimestamp())) {
            return Duration.between(earlier.getEndTimestamp(), later.getTimestamp());
        }
        return Duration.ZERO;
    }

    /**
     * Determines whether two closed intervals [start1, end1] and [start2, end2] overlap.
     * Two intervals overlap if they share at least one point in time (inclusive boundaries).
     *
     * @param start1 start of first interval
     * @param end1   end of first interval
     * @param start2 start of second interval
     * @param end2   end of second interval
     * @return true if the intervals overlap
     * @throws IllegalArgumentException if any parameter is null or start is after end
     */
    public static boolean doIntervalsOverlap(LocalDateTime start1,
                                            LocalDateTime end1,
                                            LocalDateTime start2,
                                            LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            throw new IllegalArgumentException("Interval boundaries must not be null");
        }
        if (start1.isAfter(end1)) {
            throw new IllegalArgumentException("Interval 1 start is after end: " + start1 + " > " + end1);
        }
        if (start2.isAfter(end2)) {
            throw new IllegalArgumentException("Interval 2 start is after end: " + start2 + " > " + end2);
        }
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }

    /**
     * Checks if two timeline events overlap in time.
     *
     * @param e1 first event
     * @param e2 second event
     * @return true if e1 and e2 have overlapping time spans
     * @throws IllegalArgumentException if either event is null
     */
    public static boolean doEventsOverlap(TimelineEvent e1, TimelineEvent e2) {
        if (e1 == null || e2 == null) {
            throw new IllegalArgumentException("Events to check overlap must not be null");
        }
        return doIntervalsOverlap(e1.getTimestamp(), e1.getEndTimestamp(),
                                 e2.getTimestamp(), e2.getEndTimestamp());
    }

    /**
     * Formats a {@link Duration} into a human-readable string such as "2h 15m", "45m", or "30s".
     *
     * @param duration Duration to format
     * @return readable string representation
     */
    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "0m";
        }
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m");
        } else {
            sb.append(seconds).append("s");
        }
        return sb.toString().trim();
    }
}
