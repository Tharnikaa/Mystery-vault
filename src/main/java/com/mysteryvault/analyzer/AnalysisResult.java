package com.mysteryvault.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result DTO returned by individual analyzer tasks.
 */
public class AnalysisResult {

    private final String taskName;
    private final int progressPercent;
    private final int metricCount;
    private final List<String> findings;

    public AnalysisResult(String taskName, int progressPercent, int metricCount, List<String> findings) {
        this.taskName = taskName;
        this.progressPercent = progressPercent;
        this.metricCount = metricCount;
        this.findings = findings != null ? new ArrayList<>(findings) : Collections.emptyList();
    }

    public String getTaskName() { return taskName; }
    public int getProgressPercent() { return progressPercent; }
    public int getMetricCount() { return metricCount; }
    public List<String> getFindings() { return Collections.unmodifiableList(findings); }
}
