package com.mysteryvault.analyzer;

import java.sql.Connection;

/**
 * Shared strategy interface for modular case analyzers.
 */
public interface Analyzer {
    AnalysisResult analyze(String caseId, Connection conn) throws Exception;
}
