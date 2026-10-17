package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.CaseCardViewModel;
import java.util.List;

/**
 * Presentation port for Case Selection and active case details.
 * Future integration: Member 1 (CaseService / Module 1).
 */
public interface CaseViewPort {
    List<CaseCardViewModel> getAvailableCases();
    CaseCardViewModel getActiveCase();
    void selectCase(String caseId);
}
