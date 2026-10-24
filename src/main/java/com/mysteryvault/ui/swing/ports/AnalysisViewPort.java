package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.AnalysisViewModel;

/**
 * Presentation port for investigation analysis progress and contradiction metrics.
 * Future integration: Member 3 (ConcurrentAnalysisManager / Module 8) & Member 2 (Contradiction / Module 7).
 */
public interface AnalysisViewPort {
    AnalysisViewModel getAnalysisResults(String caseId);
}
