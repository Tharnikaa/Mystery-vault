package com.mysteryvault.analyzer;

import java.util.*;

/**
 * Task that inspects witness and suspect statements.
 * Owned by Member 3 (Module 8: Concurrent Analysis).
 */
public class StatementAnalyzerTask implements Analyzer {

    private final List<String> rawStatements;

    public StatementAnalyzerTask(List<String> rawStatements) {
        this.rawStatements = rawStatements != null ? List.copyOf(rawStatements) : Collections.emptyList();
    }

    @Override
    public String getName() {
        return "Statement Sentiment & Semantic Analyzer";
    }

    @Override
    public AnalysisResult analyze(AnalysisContext context) throws Exception {
        long start = System.currentTimeMillis();
        List<String> findings = new ArrayList<>();
        Map<String, Object> metrics = new HashMap<>();

        int flaggedDeceptions = 0;
        int timeReferences = 0;

        for (int i = 0; i < rawStatements.size(); i++) {
            String stmt = rawStatements.get(i);
            String lower = stmt.toLowerCase();

            // Simple heuristic keywords
            boolean suspicious = lower.contains("never") || lower.contains("swear") ||
                                 lower.contains("honest") || lower.contains("can't remember") ||
                                 lower.contains("wasn't there");
            boolean mentionsTime = stmt.matches(".*\\b([0-1]?[0-9]|2[0-3]):[0-5][0-9]\\b.*") ||
                                  lower.contains("pm") || lower.contains("am") || lower.contains("o'clock");

            if (mentionsTime) {
                timeReferences++;
            }
            if (suspicious) {
                flaggedDeceptions++;
                findings.add(String.format("Statement #%d flagged for evasive language: \"%s\"", i + 1, stmt));
            } else {
                findings.add(String.format("Statement #%d verified: \"%s\"", i + 1, stmt));
            }
        }

        metrics.put("totalStatements", rawStatements.size());
        metrics.put("flaggedDeceptions", flaggedDeceptions);
        metrics.put("timeReferences", timeReferences);

        long duration = System.currentTimeMillis() - start;
        String summary = String.format("Processed %d statements. Flagged %d potential evasive markers, detected %d temporal markers.",
                rawStatements.size(), flaggedDeceptions, timeReferences);

        return AnalysisResult.success(getName(), summary, findings, metrics, duration);
    }
}
