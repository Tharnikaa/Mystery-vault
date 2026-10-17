package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;
import java.util.List;

/**
 * Presentation port for Evidence and Clue management.
 * Future integration: Member 1 (Evidence/Clue / Module 3).
 */
public interface EvidenceViewPort {
    List<EvidenceViewModel> getEvidenceForCase(String caseId);
    EvidenceViewModel getEvidenceById(String evidenceId);
    String examineEvidence(String evidenceId);
    String sendToLab(String evidenceId);
}
