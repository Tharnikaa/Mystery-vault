package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.analyzer.ConcurrentAnalysisManager;
import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.ui.swing.ports.AnalysisViewPort;
import com.mysteryvault.ui.swing.viewmodel.AnalysisViewModel;

/**
 * Production implementation of AnalysisViewPort backed by ConcurrentAnalysisManager.
 */
public class IntegratedAnalysisAdapter implements AnalysisViewPort {

    private final CaseDatabaseManager dbManager;
    private final ConcurrentAnalysisManager analysisManager;

    public IntegratedAnalysisAdapter(CaseDatabaseManager dbManager, ConcurrentAnalysisManager analysisManager) {
        this.dbManager = dbManager;
        this.analysisManager = analysisManager != null ? analysisManager : new ConcurrentAnalysisManager(dbManager);
    }

    @Override
    public AnalysisViewModel getAnalysisResults(String caseId) {
        String effectiveCaseId = resolveCaseId(caseId);
        return analysisManager.runAnalysis(effectiveCaseId);
    }

    private String resolveCaseId(String caseId) {
        if (caseId != null && !caseId.trim().isEmpty()) {
            return caseId;
        }
        String active = dbManager.getActiveCaseId();
        return active != null ? active : "CASE_101";
    }
}
