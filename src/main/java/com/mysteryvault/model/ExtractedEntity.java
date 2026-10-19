package com.mysteryvault.model;

import java.util.Objects;

/**
 * Represents a single entity extracted via regular expressions from statement text.
 *
 * @param <T> the parsed Java type (e.g. LocalDate, LocalTime, String)
 */
public class ExtractedEntity<T> {

    private final String rawSnippet;
    private final String matchedText;
    private final int startIndex;
    private final int endIndex;
    private final T parsedValue;

    public ExtractedEntity(String rawSnippet, String matchedText, int startIndex, int endIndex, T parsedValue) {
        this.rawSnippet = rawSnippet == null ? "" : rawSnippet;
        this.matchedText = matchedText == null ? "" : matchedText;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.parsedValue = parsedValue;
    }

    public String getRawSnippet() {
        return rawSnippet;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public T getParsedValue() {
        return parsedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtractedEntity<?> that)) return false;
        return startIndex == that.startIndex &&
               endIndex == that.endIndex &&
               Objects.equals(matchedText, that.matchedText) &&
               Objects.equals(parsedValue, that.parsedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchedText, startIndex, endIndex, parsedValue);
    }

    @Override
    public String toString() {
        return "ExtractedEntity{" +
                "matchedText='" + matchedText + '\'' +
                ", parsedValue=" + parsedValue +
                ", snippet='" + rawSnippet + '\'' +
                ", span=[" + startIndex + ", " + endIndex + "]" +
                '}';
    }
}
