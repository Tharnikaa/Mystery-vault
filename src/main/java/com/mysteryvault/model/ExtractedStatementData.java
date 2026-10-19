package com.mysteryvault.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Structured outcome resulting from parsing a {@link Statement} using regular expressions.
 * Holds all discovered dates, times, reference IDs, and locations along with their source snippets.
 */
public class ExtractedStatementData {

    private final String statementId;
    private final String caseId;
    private final String originalText;
    private final List<ExtractedEntity<LocalDate>> dates;
    private final List<ExtractedEntity<LocalTime>> times;
    private final List<ExtractedEntity<String>> referenceIds;
    private final List<ExtractedEntity<String>> locations;

    public ExtractedStatementData(String statementId,
                                  String caseId,
                                  String originalText,
                                  List<ExtractedEntity<LocalDate>> dates,
                                  List<ExtractedEntity<LocalTime>> times,
                                  List<ExtractedEntity<String>> referenceIds,
                                  List<ExtractedEntity<String>> locations) {
        this.statementId = statementId == null ? "" : statementId;
        this.caseId = caseId == null ? "" : caseId;
        this.originalText = originalText == null ? "" : originalText;
        this.dates = dates == null ? Collections.emptyList() : Collections.unmodifiableList(dates);
        this.times = times == null ? Collections.emptyList() : Collections.unmodifiableList(times);
        this.referenceIds = referenceIds == null ? Collections.emptyList() : Collections.unmodifiableList(referenceIds);
        this.locations = locations == null ? Collections.emptyList() : Collections.unmodifiableList(locations);
    }

    public String getStatementId() {
        return statementId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public List<ExtractedEntity<LocalDate>> getDates() {
        return dates;
    }

    public List<ExtractedEntity<LocalTime>> getTimes() {
        return times;
    }

    public List<ExtractedEntity<String>> getReferenceIds() {
        return referenceIds;
    }

    public List<ExtractedEntity<String>> getLocations() {
        return locations;
    }

    public boolean hasDates() {
        return !dates.isEmpty();
    }

    public boolean hasTimes() {
        return !times.isEmpty();
    }

    public boolean hasReferenceIds() {
        return !referenceIds.isEmpty();
    }

    public boolean hasLocations() {
        return !locations.isEmpty();
    }

    /**
     * Returns the primary (first) extracted date if present.
     */
    public Optional<LocalDate> getPrimaryDate() {
        return dates.stream()
                .map(ExtractedEntity::getParsedValue)
                .filter(java.util.Objects::nonNull)
                .findFirst();
    }

    /**
     * Returns the primary (first) extracted time if present.
     */
    public Optional<LocalTime> getPrimaryTime() {
        return times.stream()
                .map(ExtractedEntity::getParsedValue)
                .filter(java.util.Objects::nonNull)
                .findFirst();
    }

    /**
     * If both a date and a time were extracted, combines the primary date and primary time
     * into a {@link LocalDateTime}.
     */
    public Optional<LocalDateTime> getCombinedDateTime() {
        Optional<LocalDate> dateOpt = getPrimaryDate();
        Optional<LocalTime> timeOpt = getPrimaryTime();
        if (dateOpt.isPresent() && timeOpt.isPresent()) {
            return Optional.of(LocalDateTime.of(dateOpt.get(), timeOpt.get()));
        }
        return Optional.empty();
    }

    /**
     * Returns the primary extracted location if present.
     * If times are present, prioritizes the location physically closest to the primary time.
     */
    public Optional<String> getPrimaryLocation() {
        if (locations.isEmpty()) {
            return Optional.empty();
        }
        if (times.isEmpty()) {
            return locations.stream()
                    .map(ExtractedEntity::getParsedValue)
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .findFirst();
        }

        int timePos = times.get(0).getStartIndex();
        ExtractedEntity<String> closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (ExtractedEntity<String> loc : locations) {
            if (loc.getParsedValue() != null && !loc.getParsedValue().trim().isEmpty()) {
                int dist = Math.abs(loc.getStartIndex() - timePos);
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = loc;
                }
            }
        }
        return (closest != null) ? Optional.ofNullable(closest.getParsedValue()) : Optional.empty();
    }

    /**
     * Returns the primary (first) extracted reference ID if present.
     */
    public Optional<String> getPrimaryReferenceId() {
        return referenceIds.stream()
                .map(ExtractedEntity::getParsedValue)
                .filter(s -> s != null && !s.trim().isEmpty())
                .findFirst();
    }

    @Override
    public String toString() {
        return "ExtractedStatementData{" +
                "statementId='" + statementId + '\'' +
                ", dates=" + dates.size() +
                ", times=" + times.size() +
                ", referenceIds=" + referenceIds.size() +
                ", locations=" + locations.size() +
                '}';
    }
}
