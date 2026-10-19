package com.mysteryvault.demo;

import com.mysteryvault.analyzer.TimelineAnalysisResult;
import com.mysteryvault.analyzer.TimelineAnalyzerTask;
import com.mysteryvault.model.Contradiction;
import com.mysteryvault.model.ExtractedStatementData;
import com.mysteryvault.model.Statement;
import com.mysteryvault.model.TimelineEvent;
import com.mysteryvault.service.ContradictionDetectorService;
import com.mysteryvault.service.StatementRegexExtractor;
import com.mysteryvault.util.DateTimeUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Isolated demonstration runner for Person 2's assigned modules:
 * <ul>
 *   <li>Module 5: Timeline Analysis &amp; DateTime Utilities</li>
 *   <li>Module 6: Statement Parsing &amp; Regex Extraction</li>
 *   <li>Module 7: Contradiction Detection Service</li>
 * </ul>
 * <p>
 * <b>NOTE:</b> This is an educational demonstration runner for verification and testing.
 * It is NOT the production application entry point.
 * </p>
 */
public final class Person2Demo {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("   MYSTERYVAULT - PERSON 2 MODULES DEMONSTRATION (Modules 5, 6, and 7)        ");
        System.out.println("===============================================================================");

        String caseId = "CASE-2026-CRYOCLOCK";
        System.out.println("\nInvestigation Case: " + caseId);
        System.out.println("Scenario: Incident at Horizon Biometrix Laboratory Vault\n");

        // =========================================================================
        // MODULE 5: TIMELINE & DATE-TIME UTILITIES
        // =========================================================================
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("1. MODULE 5 - TIMELINE & CHRONOLOGICAL ANALYSIS");
        System.out.println("-------------------------------------------------------------------------------");

        LocalDateTime baseDate = LocalDateTime.of(2026, 10, 12, 0, 0);

        TimelineEvent evt1 = new TimelineEvent(
                "EVT-101", caseId,
                baseDate.withHour(8).withMinute(15),
                "Staff member S01 checked in through security turnstile",
                "SECURITY_TURNSTILE_A", "S01", "Lobby"
        );

        TimelineEvent evt2 = new TimelineEvent(
                "EVT-102", caseId,
                baseDate.withHour(21).withMinute(30),
                "CCTV camera 4 logged suspect S02 entering laboratory",
                "CCTV_LAB_CAM4", "S02", "laboratory"
        );

        TimelineEvent evt3 = new TimelineEvent(
                "EVT-103", caseId,
                baseDate.withHour(22).withMinute(0),
                baseDate.withHour(22).withMinute(20),
                "Alarm sensor triggered in Vault Alpha",
                "VAULT_MOTION_SENSOR", "UNKNOWN", "Vault Alpha"
        );

        List<TimelineEvent> rawEvents = Arrays.asList(evt3, evt1, evt2); // intentionally out of order
        System.out.println("Initial Events Count: " + rawEvents.size() + " (provided out of order)");

        TimelineAnalyzerTask timelineTask = new TimelineAnalyzerTask(caseId, rawEvents, Duration.ofHours(3));
        TimelineAnalysisResult timelineResult = timelineTask.execute();

        System.out.println("\nChronologically Sorted Timeline:");
        for (TimelineEvent e : timelineResult.getSortedEvents()) {
            System.out.printf("  [%s] %s | Actor: %-5s | Location: %-12s | %s%n",
                    e.getId(),
                    e.getTimestamp().format(DateTimeUtils.DEFAULT_FORMATTER),
                    e.getActor().isEmpty() ? "N/A" : e.getActor(),
                    e.getLocation().isEmpty() ? "N/A" : e.getLocation(),
                    e.getDescription());
        }

        System.out.println("\nTimeline Metadata:");
        System.out.println("  Earliest Event: " + timelineResult.getEarliestTimestamp());
        System.out.println("  Latest Event:   " + timelineResult.getLatestTimestamp());
        System.out.println("  Total Span:     " + DateTimeUtils.formatDuration(timelineResult.getTotalSpan()));
        System.out.println("  Actors:         " + timelineResult.getActors());
        System.out.println("  Locations:      " + timelineResult.getLocations());

        if (!timelineResult.getSignificantGaps().isEmpty()) {
            System.out.println("\nDetected Significant Gaps (> 3h threshold):");
            for (TimelineAnalysisResult.TimelineGap gap : timelineResult.getSignificantGaps()) {
                System.out.printf("  Gap between %s and %s: %s%n",
                        gap.getPrecedingEvent().getId(),
                        gap.getFollowingEvent().getId(),
                        DateTimeUtils.formatDuration(gap.getGapDuration()));
            }
        }

        // =========================================================================
        // MODULE 6: STATEMENT PARSING WITH REGEX
        // =========================================================================
        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.println("2. MODULE 6 - STATEMENT PARSING & REGEX EXTRACTION");
        System.out.println("-------------------------------------------------------------------------------");

        StatementRegexExtractor extractor = new StatementRegexExtractor();

        Statement stmt1 = new Statement(
                "STMT-201", caseId, "Witness W01",
                "I met suspect S02 near the laboratory at 9:30 PM on 12 October 2026.",
                baseDate.withHour(23).withMinute(0), "WITNESS_INTERVIEW"
        );

        Statement stmt2 = new Statement(
                "STMT-202", caseId, "Suspect S02",
                "I was never at the laboratory. I was resting in my hotel room at 9:30 PM on 12 October 2026.",
                baseDate.plusDays(1).withHour(9).withMinute(0), "POLICE_INTERROGATION"
        );

        Statement stmt3 = new Statement(
                "STMT-203", caseId, "Witness W03",
                "I cannot recall specific dates, but I remember seeing dark shadows around the complex.",
                baseDate.plusDays(1).withHour(10).withMinute(0), "INFORMAL_TIP"
        );

        List<Statement> statements = Arrays.asList(stmt1, stmt2, stmt3);

        for (Statement stmt : statements) {
            System.out.println("\nParsing Statement [" + stmt.getId() + "] (Speaker: " + stmt.getSpeaker() + "):");
            System.out.println("  Raw Text: \"" + stmt.getRawText() + "\"");

            ExtractedStatementData data = extractor.extract(stmt);
            System.out.println("  Extracted Entities:");
            System.out.println("    - Reference IDs: " + data.getReferenceIds().stream()
                    .map(e -> e.getParsedValue() + " (snippet: '" + e.getRawSnippet() + "')").toList());
            System.out.println("    - Locations:     " + data.getLocations().stream()
                    .map(e -> e.getParsedValue() + " (snippet: '" + e.getRawSnippet() + "')").toList());
            System.out.println("    - Dates:         " + data.getDates().stream()
                    .map(e -> e.getParsedValue() + " (snippet: '" + e.getRawSnippet() + "')").toList());
            System.out.println("    - Times:         " + data.getTimes().stream()
                    .map(e -> e.getParsedValue() + " (snippet: '" + e.getRawSnippet() + "')").toList());
            System.out.println("    - Combined DT:   " + data.getCombinedDateTime().orElse(null));
        }

        // =========================================================================
        // MODULE 7: CONTRADICTION DETECTION
        // =========================================================================
        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.println("3. MODULE 7 - CONTRADICTION DETECTION (RULE-BASED)");
        System.out.println("-------------------------------------------------------------------------------");

        ContradictionDetectorService contradictionDetector = new ContradictionDetectorService();
        List<Contradiction> contradictions = contradictionDetector.detectContradictions(
                caseId, statements, timelineResult.getSortedEvents()
        );

        System.out.println("Discovered Contradictions (" + contradictions.size() + " found):");
        for (Contradiction c : contradictions) {
            System.out.println("\n  [" + c.getId() + "] Severity: " + c.getSeverity() + " (" + c.getContradictionType() + ")");
            System.out.println("  Conflicting Sources: " + c.getSourceIds());
            System.out.println("  Actor Involved:      " + c.getActor());
            System.out.println("  Timestamp:           " + c.getConflictTimestamp());
            System.out.println("  Reason:              " + c.getReason());
        }

        System.out.println("\n===============================================================================");
        System.out.println("   DEMONSTRATION COMPLETED SUCCESSFULLY (All modules deterministic & tested)   ");
        System.out.println("===============================================================================");
    }
}
