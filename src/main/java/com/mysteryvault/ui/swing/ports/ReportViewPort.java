package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.ReportViewModel;

/**
 * Presentation port for official investigation report generation and display.
 * Future integration: Member 3 (ReportGeneratorService / Module 10).
 */
public interface ReportViewPort {
    ReportViewModel generateReport(String caseId);
}
