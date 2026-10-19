package com.mysteryvault.analyzer;

/**
 * Shared strategy interface for all analysis modules.
 * Owned by Member 3 (Module 8: Concurrent Analysis).
 * Coordinated with Member 2 (TimelineAnalyzerTask) and Member 4 (AnalysisViewPort).
 */
@FunctionalInterface
public interface Analyzer {

    /**
     * Executes the analysis task on the given context.
     * Must be safe to execute in background threads.
     *
     * @param context the immutable analysis context
     * @return structured, immutable analysis result
     * @throws Exception if unexpected runtime error occurs during analysis
     */
    AnalysisResult analyze(AnalysisContext context) throws Exception;

    /**
     * Returns the human-readable identifier of this analyzer.
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
