package com.mysteryvault;

import com.mysteryvault.analyzer.AnalysisContext;
import com.mysteryvault.analyzer.AnalysisResult;
import com.mysteryvault.analyzer.TimelineAnalysisResult;
import com.mysteryvault.analyzer.TimelineAnalyzerTask;
import com.mysteryvault.model.Contradiction;
import com.mysteryvault.model.ExtractedStatementData;
import com.mysteryvault.model.Statement;
import com.mysteryvault.model.TimelineEvent;
import com.mysteryvault.service.ContradictionDetectorService;
import com.mysteryvault.service.StatementRegexExtractor;
import com.mysteryvault.util.DateTimeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for Member 2 analysis deliverables (Modules 5, 6, 7).
 */
public class Member2AnalysisTest {

    @Test
    @DisplayName("TimelineAnalyzerTask sorts events chronologically and detects gaps and overlaps")
    void testTimelineAnalyzerTask() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 15, 20, 0);

        TimelineEvent e1 = new TimelineEvent("E1", "C-1", base.withMinute(0), "Event 1", "LOG", "Alice", "Room A");
        TimelineEvent e2 = new TimelineEvent("E2", "C-1", base.withMinute(30), "Event 2", "LOG", "Bob", "Room B");
        TimelineEvent e3 = new TimelineEvent("E3", "C-1", base.plusHours(3), "Event 3", "LOG", "Alice", "Room C");

        // Pass out of order
        TimelineAnalyzerTask task = new TimelineAnalyzerTask("C-1", Arrays.asList(e3, e1, e2), Duration.ofHours(1));
        TimelineAnalysisResult result = task.execute();

        assertEquals(3, result.getSortedEvents().size());
        assertEquals("E1", result.getSortedEvents().get(0).getId());
        assertEquals("E2", result.getSortedEvents().get(1).getId());
        assertEquals("E3", result.getSortedEvents().get(2).getId());

        // Gap between E2 (20:30) and E3 (23:00) is 2.5 hours, which exceeds 1 hour threshold
        assertFalse(result.getSignificantGaps().isEmpty());
    }

    @Test
    @DisplayName("TimelineAnalyzerTask implements Analyzer contract for concurrent orchestration")
    void testTimelineAnalyzerTaskAsAnalyzer() throws Exception {
        LocalDateTime base = LocalDateTime.of(2026, 10, 15, 20, 0);
        TimelineEvent e1 = new TimelineEvent("E1", "C-1", base.withMinute(0), "Event 1", "LOG", "Alice", "Room A");

        TimelineAnalyzerTask task = new TimelineAnalyzerTask("C-1", List.of(e1));
        assertEquals("Timeline Chronological Analyzer", task.getName());

        AnalysisContext ctx = new AnalysisContext("C-1", "Case Title", List.of(), List.of(), List.of(), Map.of());
        AnalysisResult res = task.analyze(ctx);

        assertNotNull(res);
        assertEquals(AnalysisResult.Status.SUCCESS, res.getStatus());
        assertTrue(res.getSummary().contains("Analyzed 1 timeline events"));
    }

    @Test
    @DisplayName("StatementRegexExtractor parses time, date, and locations accurately")
    void testStatementRegexExtractor() {
        StatementRegexExtractor extractor = new StatementRegexExtractor();
        Statement stmt = new Statement(
                "ST-1", "C-103", "Witness",
                "I met suspect S02 near the cellar pantry at 08:45 PM on 15 October 2026.",
                LocalDateTime.now(), "INTERVIEW"
        );

        ExtractedStatementData data = extractor.extract(stmt);

        assertNotNull(data);
        assertFalse(data.getTimes().isEmpty());
        assertTrue(data.getTimes().stream().anyMatch(t -> t.getRawSnippet().contains("8:45") || t.getParsedValue().getHour() == 20));
        assertFalse(data.getDates().isEmpty());
    }

    @Test
    @DisplayName("ContradictionDetectorService flags statement conflicts against timeline events")
    void testContradictionDetector() {
        ContradictionDetectorService detector = new ContradictionDetectorService();
        LocalDateTime base = LocalDateTime.of(2026, 10, 12, 0, 0);

        Statement stmt1 = new Statement(
                "STMT-201", "C-1", "Witness W01",
                "I met suspect S02 near the laboratory at 9:30 PM on 12 October 2026.",
                base.withHour(23).withMinute(0), "WITNESS_INTERVIEW"
        );

        Statement stmt2 = new Statement(
                "STMT-202", "C-1", "Suspect S02",
                "I was never at the laboratory. I was resting in my hotel room at 9:30 PM on 12 October 2026.",
                base.plusDays(1).withHour(9).withMinute(0), "POLICE_INTERROGATION"
        );

        TimelineEvent cctvEvent = new TimelineEvent(
                "EVT-102", "C-1",
                base.withHour(21).withMinute(30),
                "CCTV camera 4 logged suspect S02 entering laboratory",
                "CCTV_LAB_CAM4", "S02", "laboratory"
        );

        List<Contradiction> contradictions = detector.detectContradictions("C-1", List.of(stmt1, stmt2), List.of(cctvEvent));

        assertNotNull(contradictions);
        assertFalse(contradictions.isEmpty());
        assertEquals("S02", contradictions.get(0).getActor());
    }
}
