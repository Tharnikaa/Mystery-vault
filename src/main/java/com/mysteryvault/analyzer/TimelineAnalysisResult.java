package com.mysteryvault.analyzer;

import com.mysteryvault.model.TimelineEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Structured outcome produced by {@link TimelineAnalyzerTask}.
 * Contains chronologically sorted events, detected suspicious gaps, overlaps,
 * and actor location collisions.
 */
public class TimelineAnalysisResult {

    private final String caseId;
    private final List<TimelineEvent> sortedEvents;
    private final List<TimelineGap> significantGaps;
    private final List<TimelineOverlap> eventOverlaps;
    private final List<ActorLocationCollision> actorCollisions;
    private final LocalDateTime earliestTimestamp;
    private final LocalDateTime latestTimestamp;
    private final Duration totalSpan;
    private final Set<String> actors;
    private final Set<String> locations;

    public TimelineAnalysisResult(String caseId,
                                  List<TimelineEvent> sortedEvents,
                                  List<TimelineGap> significantGaps,
                                  List<TimelineOverlap> eventOverlaps,
                                  List<ActorLocationCollision> actorCollisions,
                                  LocalDateTime earliestTimestamp,
                                  LocalDateTime latestTimestamp,
                                  Duration totalSpan,
                                  Set<String> actors,
                                  Set<String> locations) {
        this.caseId = caseId;
        this.sortedEvents = Collections.unmodifiableList(sortedEvents);
        this.significantGaps = Collections.unmodifiableList(significantGaps);
        this.eventOverlaps = Collections.unmodifiableList(eventOverlaps);
        this.actorCollisions = Collections.unmodifiableList(actorCollisions);
        this.earliestTimestamp = earliestTimestamp;
        this.latestTimestamp = latestTimestamp;
        this.totalSpan = totalSpan;
        this.actors = Collections.unmodifiableSet(actors);
        this.locations = Collections.unmodifiableSet(locations);
    }

    public String getCaseId() {
        return caseId;
    }

    public List<TimelineEvent> getSortedEvents() {
        return sortedEvents;
    }

    public List<TimelineGap> getSignificantGaps() {
        return significantGaps;
    }

    public List<TimelineOverlap> getEventOverlaps() {
        return eventOverlaps;
    }

    public List<ActorLocationCollision> getActorCollisions() {
        return actorCollisions;
    }

    public LocalDateTime getEarliestTimestamp() {
        return earliestTimestamp;
    }

    public LocalDateTime getLatestTimestamp() {
        return latestTimestamp;
    }

    public Duration getTotalSpan() {
        return totalSpan;
    }

    public Set<String> getActors() {
        return actors;
    }

    public Set<String> getLocations() {
        return locations;
    }

    public int getEventCount() {
        return sortedEvents.size();
    }

    /**
     * Represents a suspicious or significant time gap between two consecutive events.
     */
    public static class TimelineGap {
        private final TimelineEvent precedingEvent;
        private final TimelineEvent followingEvent;
        private final Duration gapDuration;

        public TimelineGap(TimelineEvent precedingEvent, TimelineEvent followingEvent, Duration gapDuration) {
            this.precedingEvent = precedingEvent;
            this.followingEvent = followingEvent;
            this.gapDuration = gapDuration;
        }

        public TimelineEvent getPrecedingEvent() {
            return precedingEvent;
        }

        public TimelineEvent getFollowingEvent() {
            return followingEvent;
        }

        public Duration getGapDuration() {
            return gapDuration;
        }

        @Override
        public String toString() {
            return "TimelineGap{" +
                    "from=" + precedingEvent.getId() + " (" + precedingEvent.getEndTimestamp() + ")" +
                    ", to=" + followingEvent.getId() + " (" + followingEvent.getTimestamp() + ")" +
                    ", duration=" + gapDuration +
                    '}';
        }
    }

    /**
     * Represents two events whose time intervals overlap.
     */
    public static class TimelineOverlap {
        private final TimelineEvent event1;
        private final TimelineEvent event2;

        public TimelineOverlap(TimelineEvent event1, TimelineEvent event2) {
            this.event1 = event1;
            this.event2 = event2;
        }

        public TimelineEvent getEvent1() {
            return event1;
        }

        public TimelineEvent getEvent2() {
            return event2;
        }

        @Override
        public String toString() {
            return "TimelineOverlap{" +
                    "event1=" + event1.getId() +
                    ", event2=" + event2.getId() +
                    '}';
        }
    }

    /**
     * Represents an impossible situation where the same actor is reported in two different locations
     * during overlapping time windows.
     */
    public static class ActorLocationCollision {
        private final String actor;
        private final TimelineEvent event1;
        private final TimelineEvent event2;
        private final String explanation;

        public ActorLocationCollision(String actor, TimelineEvent event1, TimelineEvent event2, String explanation) {
            this.actor = actor;
            this.event1 = event1;
            this.event2 = event2;
            this.explanation = explanation;
        }

        public String getActor() {
            return actor;
        }

        public TimelineEvent getEvent1() {
            return event1;
        }

        public TimelineEvent getEvent2() {
            return event2;
        }

        public String getExplanation() {
            return explanation;
        }

        @Override
        public String toString() {
            return "ActorLocationCollision{" +
                    "actor='" + actor + '\'' +
                    ", event1=" + event1.getId() + " (" + event1.getLocation() + ")" +
                    ", event2=" + event2.getId() + " (" + event2.getLocation() + ")" +
                    ", explanation='" + explanation + '\'' +
                    '}';
        }
    }
}
