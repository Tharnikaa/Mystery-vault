package com.mysteryvault.service;

import com.mysteryvault.model.ExtractedEntity;
import com.mysteryvault.model.ExtractedStatementData;
import com.mysteryvault.model.Statement;
import com.mysteryvault.util.RegexUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that parses unstructured statement text into structured data using regular expressions.
 * Extracts:
 * <ul>
 *   <li>Dates (e.g. "12 October 2026", "October 12, 2026", "2026-10-12", "12/10/2026")</li>
 *   <li>Times (e.g. "9:30 PM", "09:30 AM", "21:30", "14:00")</li>
 *   <li>Reference IDs (e.g. "S02", "suspect S02", "E01", "W03", "CASE-101")</li>
 *   <li>Locations (e.g. "near the laboratory", "at Office 101", "in Room 302")</li>
 * </ul>
 * Gracefully handles missing, malformed, or multiple entities without throwing exceptions.
 */
public class StatementRegexExtractor {

    private static final int CONTEXT_PADDING = 25;

    /**
     * Extracts structured investigative entities from a {@link Statement}.
     *
     * @param statement the statement to extract from
     * @return structured {@link ExtractedStatementData}
     */
    public ExtractedStatementData extract(Statement statement) {
        if (statement == null) {
            return new ExtractedStatementData("", "", "", Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
        return extract(statement.getId(), statement.getCaseId(), statement.getRawText());
    }

    /**
     * Extracts structured entities directly from raw text with mock/unassigned IDs.
     *
     * @param rawText raw input text
     * @return structured {@link ExtractedStatementData}
     */
    public ExtractedStatementData extract(String rawText) {
        return extract("ANONYMOUS", "CASE-UNKNOWN", rawText);
    }

    /**
     * Core extraction implementation.
     */
    public ExtractedStatementData extract(String statementId, String caseId, String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new ExtractedStatementData(statementId, caseId, "", Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<ExtractedEntity<LocalDate>> dates = extractDates(rawText);
        List<ExtractedEntity<LocalTime>> times = extractTimes(rawText);
        List<ExtractedEntity<String>> referenceIds = extractReferenceIds(rawText);
        List<ExtractedEntity<String>> locations = extractLocations(rawText);

        return new ExtractedStatementData(statementId, caseId, rawText, dates, times, referenceIds, locations);
    }

    // ==========================================
    // DATE EXTRACTION
    // ==========================================

    public List<ExtractedEntity<LocalDate>> extractDates(String text) {
        List<ExtractedEntity<LocalDate>> results = new ArrayList<>();
        Set<String> matchedSpans = new LinkedHashSet<>();

        // 1. Day Month Year (e.g. 12 October 2026, 12th Oct 2026)
        Matcher m1 = RegexUtils.DATE_DAY_MONTH_YEAR_PATTERN.matcher(text);
        while (m1.find()) {
            String spanKey = m1.start() + ":" + m1.end();
            if (matchedSpans.add(spanKey)) {
                String dayStr = m1.group(1);
                String monthStr = m1.group(2);
                String yearStr = m1.group(3);
                LocalDate parsed = parseDayMonthYear(dayStr, monthStr, yearStr);
                String snippet = RegexUtils.extractContextSnippet(text, m1.start(), m1.end(), CONTEXT_PADDING);
                results.add(new ExtractedEntity<>(snippet, m1.group(), m1.start(), m1.end(), parsed));
            }
        }

        // 2. Month Day Year (e.g. October 12, 2026)
        Matcher m2 = RegexUtils.DATE_MONTH_DAY_YEAR_PATTERN.matcher(text);
        while (m2.find()) {
            String spanKey = m2.start() + ":" + m2.end();
            if (matchedSpans.add(spanKey)) {
                String monthStr = m2.group(1);
                String dayStr = m2.group(2);
                String yearStr = m2.group(3);
                LocalDate parsed = parseDayMonthYear(dayStr, monthStr, yearStr);
                String snippet = RegexUtils.extractContextSnippet(text, m2.start(), m2.end(), CONTEXT_PADDING);
                results.add(new ExtractedEntity<>(snippet, m2.group(), m2.start(), m2.end(), parsed));
            }
        }

        // 3. ISO Date (e.g. 2026-10-12)
        Matcher m3 = RegexUtils.DATE_ISO_PATTERN.matcher(text);
        while (m3.find()) {
            String spanKey = m3.start() + ":" + m3.end();
            if (matchedSpans.add(spanKey)) {
                LocalDate parsed = null;
                try {
                    int year = Integer.parseInt(m3.group(1));
                    int month = Integer.parseInt(m3.group(2));
                    int day = Integer.parseInt(m3.group(3));
                    parsed = LocalDate.of(year, month, day);
                } catch (Exception ignored) {}
                String snippet = RegexUtils.extractContextSnippet(text, m3.start(), m3.end(), CONTEXT_PADDING);
                results.add(new ExtractedEntity<>(snippet, m3.group(), m3.start(), m3.end(), parsed));
            }
        }

        // 4. Numeric Slash/Dash Date (e.g. 12/10/2026)
        Matcher m4 = RegexUtils.DATE_NUMERIC_PATTERN.matcher(text);
        while (m4.find()) {
            String spanKey = m4.start() + ":" + m4.end();
            if (matchedSpans.add(spanKey)) {
                LocalDate parsed = null;
                try {
                    int day = Integer.parseInt(m4.group(1));
                    int month = Integer.parseInt(m4.group(2));
                    int year = Integer.parseInt(m4.group(3));
                    parsed = LocalDate.of(year, month, day);
                } catch (Exception ignored) {}
                String snippet = RegexUtils.extractContextSnippet(text, m4.start(), m4.end(), CONTEXT_PADDING);
                results.add(new ExtractedEntity<>(snippet, m4.group(), m4.start(), m4.end(), parsed));
            }
        }

        return results;
    }

    private LocalDate parseDayMonthYear(String dayStr, String monthStr, String yearStr) {
        try {
            int day = Integer.parseInt(dayStr);
            int year = Integer.parseInt(yearStr);
            Month month = resolveMonth(monthStr);
            if (month != null) {
                return LocalDate.of(year, month, day);
            }
        } catch (DateTimeException | NumberFormatException ignored) {
            // Graceful fallback: return null parsed value without crashing
        }
        return null;
    }

    private Month resolveMonth(String monthStr) {
        if (monthStr == null) return null;
        String clean = monthStr.trim().toUpperCase(Locale.ENGLISH);
        for (Month m : Month.values()) {
            if (m.name().equalsIgnoreCase(clean) ||
                m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).equalsIgnoreCase(clean)) {
                return m;
            }
        }
        return null;
    }

    // ==========================================
    // TIME EXTRACTION
    // ==========================================

    public List<ExtractedEntity<LocalTime>> extractTimes(String text) {
        List<ExtractedEntity<LocalTime>> results = new ArrayList<>();
        Set<String> matchedSpans = new LinkedHashSet<>();

        // 1. 12-hour AM/PM (e.g. 9:30 PM, 9 PM, 09:30am)
        Matcher m1 = RegexUtils.TIME_12H_PATTERN.matcher(text);
        while (m1.find()) {
            String spanKey = m1.start() + ":" + m1.end();
            if (matchedSpans.add(spanKey)) {
                String timePart = m1.group(1);
                String ampm = m1.group(4);
                LocalTime parsed = parse12HourTime(timePart, ampm);
                String snippet = RegexUtils.extractContextSnippet(text, m1.start(), m1.end(), CONTEXT_PADDING);
                results.add(new ExtractedEntity<>(snippet, m1.group(), m1.start(), m1.end(), parsed));
            }
        }

        // 2. 24-hour military (e.g. 21:30, 09:30, 14:00)
        Matcher m2 = RegexUtils.TIME_24H_PATTERN.matcher(text);
        while (m2.find()) {
            String spanKey = m2.start() + ":" + m2.end();
            if (matchedSpans.add(spanKey)) {
                LocalTime parsed = null;
                try {
                    int hour = Integer.parseInt(m2.group(1));
                    int minute = Integer.parseInt(m2.group(2));
                    int second = (m2.group(3) != null) ? Integer.parseInt(m2.group(3)) : 0;
                    parsed = LocalTime.of(hour, minute, second);
                } catch (Exception ignored) {}
                String snippet = RegexUtils.extractContextSnippet(text, m2.start(), m2.end(), CONTEXT_PADDING);
                results.add(new ExtractedEntity<>(snippet, m2.group(), m2.start(), m2.end(), parsed));
            }
        }

        return results;
    }

    private LocalTime parse12HourTime(String timePart, String ampm) {
        try {
            String[] parts = timePart.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
            int second = (parts.length > 2) ? Integer.parseInt(parts[2]) : 0;

            boolean isPm = ampm.equalsIgnoreCase("pm");
            if (isPm && hour < 12) {
                hour += 12;
            } else if (!isPm && hour == 12) {
                hour = 0;
            }
            return LocalTime.of(hour, minute, second);
        } catch (Exception ignored) {
            return null;
        }
    }

    // ==========================================
    // REFERENCE ID EXTRACTION
    // ==========================================

    public List<ExtractedEntity<String>> extractReferenceIds(String text) {
        List<ExtractedEntity<String>> results = new ArrayList<>();
        Set<String> matchedSpans = new LinkedHashSet<>();

        Matcher m = RegexUtils.REFERENCE_ID_PATTERN.matcher(text);
        while (m.find()) {
            String spanKey = m.start() + ":" + m.end();
            if (matchedSpans.add(spanKey)) {
                // Group 1 is the clean identifier (e.g. "S02")
                String cleanId = m.group(1).toUpperCase(Locale.ENGLISH);
                String snippet = RegexUtils.extractContextSnippet(text, m.start(), m.end(), CONTEXT_PADDING);
                results.add(new ExtractedEntity<>(snippet, m.group(), m.start(), m.end(), cleanId));
            }
        }
        return results;
    }

    // ==========================================
    // LOCATION EXTRACTION
    // ==========================================

    public List<ExtractedEntity<String>> extractLocations(String text) {
        List<ExtractedEntity<String>> results = new ArrayList<>();
        Set<String> matchedSpans = new LinkedHashSet<>();

        Matcher m = RegexUtils.LOCATION_PREPOSITION_PATTERN.matcher(text);
        while (m.find()) {
            String spanKey = m.start() + ":" + m.end();
            if (matchedSpans.add(spanKey)) {
                String rawLoc = m.group(1);
                String cleanLoc = RegexUtils.sanitizeToken(rawLoc);

                // Filter out accidental time words or very short non-location matches or negated locations
                if (isValidLocationName(cleanLoc) && !isNegatedLocation(text, m.start())) {
                    String snippet = RegexUtils.extractContextSnippet(text, m.start(), m.end(), CONTEXT_PADDING);
                    results.add(new ExtractedEntity<>(snippet, m.group(), m.start(), m.end(), cleanLoc));
                }
            }
        }
        return results;
    }

    private boolean isNegatedLocation(String text, int matchStart) {
        int lookbackStart = Math.max(0, matchStart - 20);
        String prefix = text.substring(lookbackStart, matchStart).toLowerCase(Locale.ENGLISH);
        return prefix.matches(".*\\b(never|not|wasn't|was\\s+not|was\\s+never|didn't)\\s*$");
    }

    private boolean isValidLocationName(String name) {
        if (name == null || name.length() < 2) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ENGLISH);
        // Exclude common temporal tokens or pure pronouns that follow prepositions
        if (lower.matches("^(yesterday|today|tomorrow|noon|midnight|night|morning|afternoon|once|again|him|her|them|me|us|it)$")) {
            return false;
        }
        return true;
    }
}
