package com.mysteryvault.service;

import com.mysteryvault.enums.Severity;
import com.mysteryvault.exception.ContradictionException;
import com.mysteryvault.model.Contradiction;
import com.mysteryvault.model.ExtractedStatementData;
import com.mysteryvault.model.Statement;
import com.mysteryvault.model.TimelineEvent;
import com.mysteryvault.util.DateTimeUtils;
import com.mysteryvault.util.RegexUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service that detects factual contradictions and alibi discrepancies
 * between witness/suspect statements and objective timeline events.
 * <p>
 * Key Design Principles:
 * <ul>
 *   <li><b>Rule-based &amp; Deterministic:</b> Evaluates explicit temporal and locational collisions.</li>
 *   <li><b>False-Positive Avoidance:</b> Missing dates, times, or locations do NOT trigger contradictions.
 *       Different locations are not contradictory unless their times establish physical incompatibility.</li>
 *   <li><b>Safety:</b> Never modifies case state or marks an investigation as solved.</li>
 * </ul>
 * </p>
 */
public class ContradictionDetectorService {

    public static final Duration DEFAULT_MIN_TRAVEL_BUFFER = Duration.ofMinutes(10);

    private final StatementRegexExtractor regexExtractor;
    private final Duration minimumTravelBuffer;

    public ContradictionDetectorService() {
        this(new StatementRegexExtractor(), DEFAULT_MIN_TRAVEL_BUFFER);
    }

    public ContradictionDetectorService(StatementRegexExtractor regexExtractor, Duration minimumTravelBuffer) {
        this.regexExtractor = (regexExtractor != null) ? regexExtractor : new StatementRegexExtractor();
        this.minimumTravelBuffer = (minimumTravelBuffer != null) ? minimumTravelBuffer : DEFAULT_MIN_TRAVEL_BUFFER;
    }

    /**
     * Detects contradictions among a collection of statements and verified timeline events.
     *
     * @param caseId         Identifier of the case being inspected
     * @param statements     List of statements to analyze (nullable)
     * @param timelineEvents List of timeline facts to cross-reference (nullable)
     * @return Sorted, deterministic list of discovered {@link Contradiction}s
     * @throws ContradictionException if caseId is null or blank
     */
    public List<Contradiction> detectContradictions(String caseId,
                                                   List<Statement> statements,
                                                   List<TimelineEvent> timelineEvents) {
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new ContradictionException("Case ID must not be null or blank when analyzing contradictions");
        }

        List<Statement> safeStatements = (statements == null) ? Collections.emptyList() : statements;
        List<TimelineEvent> safeTimeline = (timelineEvents == null) ? Collections.emptyList() : timelineEvents;

        List<Contradiction> contradictions = new ArrayList<>();
        int counter = 1;

        // Pre-parse statements into structured entities
        List<ExtractedStatementData> parsedStatements = new ArrayList<>();
        for (Statement s : safeStatements) {
            if (s != null) {
                parsedStatements.add(regexExtractor.extract(s));
            }
        }

        // 1. Compare Statements vs Objective Timeline Events (Alibi refutation)
        counter = checkStatementsAgainstTimeline(caseId, safeStatements, parsedStatements, safeTimeline, contradictions, counter);

        // 2. Compare Statements vs Statements (Conflicting witness/suspect claims)
        counter = checkStatementsAgainstStatements(caseId, safeStatements, parsedStatements, contradictions, counter);

        // 3. Compare Timeline Events vs Timeline Events (Physical collisions & impossible travel)
        checkTimelineAgainstTimeline(caseId, safeTimeline, contradictions, counter);

        // Deterministic sort: higher severity first, then by ID
        Collections.sort(contradictions);
        return Collections.unmodifiableList(contradictions);
    }

    /**
     * Cross-references suspect/witness claims in statements against verified timeline events.
     */
    private int checkStatementsAgainstTimeline(String caseId,
                                               List<Statement> statements,
                                               List<ExtractedStatementData> parsed,
                                               List<TimelineEvent> timelineEvents,
                                               List<Contradiction> contradictions,
                                               int counter) {
        for (int i = 0; i < statements.size(); i++) {
            Statement stmt = statements.get(i);
            ExtractedStatementData data = parsed.get(i);

            Optional<LocalDateTime> stmtTimeOpt = data.getCombinedDateTime();
            Optional<String> stmtLocOpt = data.getPrimaryLocation();
            String stmtActor = resolveActor(stmt, data);

            // Avoid false positives: both time and location must be clearly established in statement
            if (stmtTimeOpt.isEmpty() || stmtLocOpt.isEmpty() || stmtActor.isEmpty()) {
                continue;
            }

            LocalDateTime stmtTime = stmtTimeOpt.get();
            String stmtLoc = stmtLocOpt.get();

            for (TimelineEvent event : timelineEvents) {
                if (event == null || event.getActor().isEmpty() || event.getLocation().isEmpty()) {
                    continue;
                }

                if (event.getActor().equalsIgnoreCase(stmtActor)) {
                    // Check if statement time falls within the timeline event window
                    boolean timeConflict = DateTimeUtils.isWithinRange(stmtTime, event.getTimestamp(), event.getEndTimestamp());

                    if (timeConflict) {
                        // Check if locations are incompatible
                        if (!isSameLocation(stmtLoc, event.getLocation())) {
                            String reason = String.format(
                                    "Alibi conflict for '%s': Statement [%s] claims presence at '%s' around %s, " +
                                    "but verified timeline fact [%s] (%s) places them at '%s' between %s and %s.",
                                    stmtActor, stmt.getId(), stmtLoc, stmtTime,
                                    event.getId(), event.getSource(), event.getLocation(),
                                    event.getTimestamp(), event.getEndTimestamp());

                            contradictions.add(new Contradiction(
                                    formatId(counter++),
                                    caseId,
                                    List.of(stmt.getId(), event.getId()),
                                    reason,
                                    Severity.CRITICAL,
                                    "ALIBI_REFUTATION",
                                    stmtTime,
                                    stmtActor
                            ));
                        }
                    }
                }
            }
        }
        return counter;
    }

    /**
     * Compares pairs of statements to detect conflicting testimonies regarding whereabouts.
     */
    private int checkStatementsAgainstStatements(String caseId,
                                                 List<Statement> statements,
                                                 List<ExtractedStatementData> parsed,
                                                 List<Contradiction> contradictions,
                                                 int counter) {
        for (int i = 0; i < statements.size(); i++) {
            for (int j = i + 1; j < statements.size(); j++) {
                Statement s1 = statements.get(i);
                Statement s2 = statements.get(j);
                ExtractedStatementData d1 = parsed.get(i);
                ExtractedStatementData d2 = parsed.get(j);

                Optional<LocalDateTime> t1Opt = d1.getCombinedDateTime();
                Optional<LocalDateTime> t2Opt = d2.getCombinedDateTime();
                Optional<String> loc1Opt = d1.getPrimaryLocation();
                Optional<String> loc2Opt = d2.getPrimaryLocation();

                String actor1 = resolveActor(s1, d1);
                String actor2 = resolveActor(s2, d2);

                // Avoid false positives: require known actor, time, and location
                if (t1Opt.isEmpty() || t2Opt.isEmpty() || loc1Opt.isEmpty() || loc2Opt.isEmpty()) {
                    continue;
                }
                if (actor1.isEmpty() || !actor1.equalsIgnoreCase(actor2)) {
                    continue;
                }

                LocalDateTime t1 = t1Opt.get();
                LocalDateTime t2 = t2Opt.get();
                String loc1 = loc1Opt.get();
                String loc2 = loc2Opt.get();

                // If claimed times are identical or within 15 minutes but locations differ
                Duration diff = DateTimeUtils.calculateGap(t1, t2);
                if (diff.toMinutes() <= 15 && !isSameLocation(loc1, loc2)) {
                    String reason = String.format(
                            "Conflicting statement claims for '%s': Statement [%s] places them at '%s' at %s, " +
                            "while Statement [%s] places them at '%s' at %s.",
                            actor1, s1.getId(), loc1, t1, s2.getId(), loc2, t2);

                    contradictions.add(new Contradiction(
                            formatId(counter++),
                            caseId,
                            List.of(s1.getId(), s2.getId()),
                            reason,
                            Severity.HIGH,
                            "STATEMENT_WHEREABOUTS_COLLISION",
                            t1,
                            actor1
                    ));
                }
            }
        }
        return counter;
    }

    /**
     * Cross-references timeline events against other timeline events for direct overlap
     * or impossible travel transit times.
     */
    private int checkTimelineAgainstTimeline(String caseId,
                                             List<TimelineEvent> timelineEvents,
                                             List<Contradiction> contradictions,
                                             int counter) {
        List<TimelineEvent> sorted = DateTimeUtils.sortChronologically(timelineEvents);

        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                TimelineEvent e1 = sorted.get(i);
                TimelineEvent e2 = sorted.get(j);

                if (e1.getActor().isEmpty() || !e1.getActor().equalsIgnoreCase(e2.getActor())) {
                    continue;
                }
                if (e1.getLocation().isEmpty() || isSameLocation(e1.getLocation(), e2.getLocation())) {
                    continue;
                }

                // 1. Simultaneous Overlap at different locations
                if (DateTimeUtils.doEventsOverlap(e1, e2)) {
                    String reason = String.format(
                            "Simultaneous presence collision for '%s': Event [%s] records presence at '%s' " +
                            "while Event [%s] records presence at '%s' during overlapping intervals.",
                            e1.getActor(), e1.getId(), e1.getLocation(), e2.getId(), e2.getLocation());

                    contradictions.add(new Contradiction(
                            formatId(counter++),
                            caseId,
                            List.of(e1.getId(), e2.getId()),
                            reason,
                            Severity.HIGH,
                            "TIMELINE_LOCATION_COLLISION",
                            e1.getTimestamp(),
                            e1.getActor()
                    ));
                }
                // 2. Impossible Travel Buffer
                else {
                    Duration gap = DateTimeUtils.calculateEventGap(e1, e2);
                    if (gap.compareTo(minimumTravelBuffer) < 0) {
                        String reason = String.format(
                                "Physically improbable transit for '%s': Ended at '%s' at %s (Event %s) " +
                                "and appeared at '%s' at %s (Event %s) within %d minutes (minimum realistic buffer is %d min).",
                                e1.getActor(), e1.getLocation(), e1.getEndTimestamp(), e1.getId(),
                                e2.getLocation(), e2.getTimestamp(), e2.getId(),
                                gap.toMinutes(), minimumTravelBuffer.toMinutes());

                        contradictions.add(new Contradiction(
                                formatId(counter++),
                                caseId,
                                List.of(e1.getId(), e2.getId()),
                                reason,
                                Severity.MEDIUM,
                                "IMPOSSIBLE_TRAVEL_TRANSIT",
                                e2.getTimestamp(),
                                e1.getActor()
                        ));
                    }
                }
            }
        }
        return counter;
    }

    private String resolveActor(Statement stmt, ExtractedStatementData data) {
        if (data.hasReferenceIds()) {
            return data.getPrimaryReferenceId().get();
        }
        if (stmt.getSpeaker() != null && !stmt.getSpeaker().isEmpty()) {
            java.util.regex.Matcher m = RegexUtils.STANDALONE_ID_PATTERN.matcher(stmt.getSpeaker());
            if (m.find()) {
                return m.group().toUpperCase(java.util.Locale.ENGLISH);
            }
            return stmt.getSpeaker().trim();
        }
        return "";
    }

    private boolean isSameLocation(String loc1, String loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.trim().equalsIgnoreCase(loc2.trim());
    }

    private String formatId(int index) {
        return String.format("CTRD-%03d", index);
    }
}
