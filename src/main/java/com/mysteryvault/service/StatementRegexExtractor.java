package com.mysteryvault.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module 6 — Statement Regex Extractor.
 * Extracts times, dates, locations, and key entities from suspect statements.
 */
public class StatementRegexExtractor {

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "\\b((?:0?[1-9]|1[0-2]):[0-5][0-9]\\s*(?:AM|PM|am|pm)?|(?:[01]?[0-9]|2[0-3]):[0-5][0-9])\\b"
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(?:(?:Mon|Tues|Wednes|Thurs|Fri|Satur|Sun)day|yesterday|today|last\\s+\\w+|\\d{1,2}(?:st|nd|rd|th)?\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*|\\d{4}-\\d{2}-\\d{2})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "\\b(?:in|at|near|inside|beside|to|from)\\s+the\\s+([A-Za-z0-9\\s]{3,25}?)(?=[.,;]|\\s+(?:at|during|when|and|with|until|before|after)|$)",
            Pattern.CASE_INSENSITIVE
    );

    public List<String> extractTimes(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>();
        Matcher m = TIME_PATTERN.matcher(text);
        while (m.find()) {
            String time = m.group().trim();
            if (!results.contains(time)) {
                results.add(time);
            }
        }
        return results;
    }

    public List<String> extractDates(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>();
        Matcher m = DATE_PATTERN.matcher(text);
        while (m.find()) {
            String date = m.group().trim();
            if (!results.contains(date)) {
                results.add(date);
            }
        }
        return results;
    }

    public List<String> extractLocations(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>();
        Matcher m = LOCATION_PATTERN.matcher(text);
        while (m.find()) {
            String loc = m.group(1).trim();
            if (!loc.isEmpty() && !results.contains(loc)) {
                results.add(loc);
            }
        }
        return results;
    }
}
