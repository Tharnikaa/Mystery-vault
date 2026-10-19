package com.mysteryvault.analyzer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable result produced by an Analyzer task.
 * Owned by Member 3 (Module 8: Concurrent Analysis).
 */
public class AnalysisResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        SUCCESS,
        WARNING,
        FAILED
    }

    private final String analyzerName;
    private final Status status;
    private final String summary;
    private final List<String> findings;
    private final Map<String, Object> metrics;
    private final LocalDateTime completedAt;
    private final long executionDurationMs;

    public AnalysisResult(String analyzerName,
                          Status status,
                          String summary,
                          List<String> findings,
                          Map<String, Object> metrics,
                          long executionDurationMs) {
        this.analyzerName = Objects.requireNonNull(analyzerName, "analyzerName cannot be null");
        this.status = status != null ? status : Status.SUCCESS;
        this.summary = summary != null ? summary : "";
        this.findings = findings != null ? List.copyOf(findings) : Collections.emptyList();
        this.metrics = metrics != null ? Map.copyOf(metrics) : Collections.emptyMap();
        this.completedAt = LocalDateTime.now();
        this.executionDurationMs = executionDurationMs;
    }

    public static AnalysisResult success(String analyzerName, String summary, List<String> findings, Map<String, Object> metrics, long durationMs) {
        return new AnalysisResult(analyzerName, Status.SUCCESS, summary, findings, metrics, durationMs);
    }

    public static AnalysisResult failure(String analyzerName, String errorMessage, long durationMs) {
        return new AnalysisResult(analyzerName, Status.FAILED, errorMessage, List.of("ERROR: " + errorMessage), Collections.emptyMap(), durationMs);
    }

    public String getAnalyzerName() {
        return analyzerName;
    }

    public Status getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getFindings() {
        return findings;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "analyzer='" + analyzerName + '\'' +
                ", status=" + status +
                ", duration=" + executionDurationMs + "ms" +
                ", findings=" + findings.size() +
                '}';
    }
}
