package com.mysteryvault.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for StatementRegexExtractor (Module 6).
 * Validates regular expression parsing for times, dates, and locations.
 */
public class StatementRegexExtractorTest {

    private StatementRegexExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new StatementRegexExtractor();
    }

    @Test
    @DisplayName("Extract times from 12-hour and 24-hour statement formats")
    void testExtractTimes() {
        String statement = "I entered the study at 10:30 PM, then spoke with Arthur at 11:15 pm until 23:45.";
        List<String> times = extractor.extractTimes(statement);

        assertNotNull(times);
        assertTrue(times.contains("10:30 PM") || times.contains("10:30"));
        assertTrue(times.contains("11:15 pm") || times.contains("11:15"));
        assertTrue(times.contains("23:45"));
    }

    @Test
    @DisplayName("Extract dates from natural language and calendar formats")
    void testExtractDates() {
        String statement = "We met on Friday and then again yesterday, on 14th Oct, before the incident on 2026-10-22.";
        List<String> dates = extractor.extractDates(statement);

        assertNotNull(dates);
        assertTrue(dates.stream().anyMatch(d -> d.equalsIgnoreCase("Friday")));
        assertTrue(dates.stream().anyMatch(d -> d.equalsIgnoreCase("yesterday")));
        assertTrue(dates.stream().anyMatch(d -> d.contains("14th Oct") || d.contains("Oct")));
        assertTrue(dates.stream().anyMatch(d -> d.equals("2026-10-22")));
    }

    @Test
    @DisplayName("Extract locations with prepositions")
    void testExtractLocations() {
        String statement = "I was waiting at the wine cellar before walking to the library during the gala.";
        List<String> locations = extractor.extractLocations(statement);

        assertNotNull(locations);
        assertTrue(locations.stream().anyMatch(l -> l.equalsIgnoreCase("wine cellar")));
        assertTrue(locations.stream().anyMatch(l -> l.equalsIgnoreCase("library")));
    }

    @Test
    @DisplayName("Handles null and empty inputs safely")
    void testNullAndEmptyInputs() {
        assertTrue(extractor.extractTimes(null).isEmpty());
        assertTrue(extractor.extractTimes("").isEmpty());
        assertTrue(extractor.extractTimes("   ").isEmpty());

        assertTrue(extractor.extractDates(null).isEmpty());
        assertTrue(extractor.extractDates("").isEmpty());

        assertTrue(extractor.extractLocations(null).isEmpty());
        assertTrue(extractor.extractLocations("").isEmpty());
    }
}
