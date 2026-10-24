package com.mysteryvault.ui.swing.adapters;

import com.mysteryvault.database.CaseDatabaseManager;
import com.mysteryvault.service.ReportGeneratorService;
import com.mysteryvault.ui.swing.ports.ReportViewPort;
import com.mysteryvault.ui.swing.viewmodel.ReportViewModel;

/**
 * Production implementation of ReportViewPort backed by ReportGeneratorService.
 */
public class IntegratedReportAdapter implements ReportViewPort {

    private final ReportGeneratorService reportService;
    private final CaseDatabaseManager dbManager;

    public IntegratedReportAdapter(CaseDatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.reportService = new ReportGeneratorService(dbManager);
    }

    @Override
    public ReportViewModel generateReport(String caseId) {
        String effectiveCaseId = resolveCaseId(caseId);
        return reportService.generateReport(effectiveCaseId);
    }

    private String resolveCaseId(String caseId) {
        if (caseId != null && !caseId.trim().isEmpty()) {
            return caseId;
        }
        String active = dbManager.getActiveCaseId();
        return active != null ? active : "CASE_101";
    }
}
