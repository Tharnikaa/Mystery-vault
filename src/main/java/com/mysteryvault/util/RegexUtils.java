package com.mysteryvault.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular expression utility and pattern collection for parsing investigative text.
 * Provides precompiled patterns, contextual snippet extraction, and safe string handling.
 */
public final class RegexUtils {

    // Reference ID Pattern: matches identifiers like S02, S-02, E01, W03, C101, CASE-101, EVT-001
    public static final Pattern REFERENCE_ID_PATTERN = Pattern.compile(
            "\\b(?:(?:suspect|witness|evidence|case|officer|person|victim|subject|item)\\s+)?([A-Z]{1,4}-?[0-9]{2,5})\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Standard Alpha-Numeric ID token pattern
    public static final Pattern STANDALONE_ID_PATTERN = Pattern.compile(
            "\\b[A-Z]{1,4}-?[0-9]{2,5}\\b"
    );

    // 12-hour Time: e.g. 9:30 PM, 9:30pm, 09:30 AM, 9 PM, 9:30:15 PM
    public static final Pattern TIME_12H_PATTERN = Pattern.compile(
            "\\b((?:0?[1-9]|1[0-2])(?::([0-5]\\d))?(?::([0-5]\\d))?)\\s*([APap][Mm])\\b"
    );

    // 24-hour Time: e.g. 21:30, 09:30, 14:00, 23:59:59
    public static final Pattern TIME_24H_PATTERN = Pattern.compile(
            "\\b([01]?\\d|2[0-3]):([0-5]\\d)(?::([0-5]\\d))?\\b(?!\\s*[APap][Mm])"
    );

    // Date Pattern 1: Day Month Year (e.g. 12 October 2026, 12th Oct 2026, 3 May 2025)
    public static final Pattern DATE_DAY_MONTH_YEAR_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+([A-Za-z]+)\\s+(\\d{4})\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Date Pattern 2: Month Day Year (e.g. October 12, 2026, Oct 12 2026)
    public static final Pattern DATE_MONTH_DAY_YEAR_PATTERN = Pattern.compile(
            "\\b([A-Za-z]+)\\s+(\\d{1,2})(?:st|nd|rd|th)?,?\\s+(\\d{4})\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Date Pattern 3: ISO format (e.g. 2026-10-12 or 2026/10/12)
    public static final Pattern DATE_ISO_PATTERN = Pattern.compile(
            "\\b(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})\\b"
    );

    // Date Pattern 4: Numeric slash/dash (e.g. 12/10/2026 or 12-10-2026)
    public static final Pattern DATE_NUMERIC_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})[-/](\\d{1,2})[-/](\\d{4})\\b"
    );

    // Location Phrase Pattern: preposition followed by optional article/possessive and location name
    public static final Pattern LOCATION_PREPOSITION_PATTERN = Pattern.compile(
            "\\b(?:at|near|in|inside|outside|by|around|from|to)\\s+(?:the\\s+|my\\s+|our\\s+|a\\s+|an\\s+)?([A-Za-z0-9][A-Za-z0-9\\s'-]{1,35}?)(?=[,\\.]|\\s+(?:at|on|near|with|by|around|and|during|when|before|after|which|who|where)\\b|$)",
            Pattern.CASE_INSENSITIVE
    );

    private RegexUtils() {
        // Prevent instantiation of utility class
    }

    /**
     * Extracts a context window surrounding a match in the source text.
     *
     * @param text           full source text
     * @param matchStart     start index of the match
     * @param matchEnd       end index of the match
     * @param paddingChars   number of context characters before and after
     * @return contextual snippet, prefixed/suffixed with "..." if truncated
     */
    public static String extractContextSnippet(String text, int matchStart, int matchEnd, int paddingChars) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        int clampedStart = Math.max(0, matchStart - paddingChars);
        int clampedEnd = Math.min(text.length(), matchEnd + paddingChars);

        String prefix = (clampedStart > 0) ? "..." : "";
        String suffix = (clampedEnd < text.length()) ? "..." : "";

        return prefix + text.substring(clampedStart, clampedEnd).trim() + suffix;
    }

    /**
     * Cleans extraneous whitespace and punctuation from extracted token values.
     */
    public static String sanitizeToken(String rawToken) {
        if (rawToken == null) {
            return "";
        }
        return rawToken.replaceAll("^[\\s,\\.;:\"'()]+|[\\s,\\.;:\"'()]+$", "").trim();
    }
}
