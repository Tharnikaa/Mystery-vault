package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.EvidenceViewModel;
import java.util.List;

/**
 * Presentation port for Evidence and Clue management.
 */
public interface EvidenceViewPort {
    List<EvidenceViewModel> getEvidenceForCase(String caseId);
    EvidenceViewModel getEvidenceById(String evidenceId);
    String examineEvidence(String evidenceId);
    String sendToLab(String evidenceId);

    default List<EvidenceViewModel> getEvidenceForSuspect(String caseId, String suspectId) {
        return getEvidenceForCase(caseId).stream()
                .filter(e -> suspectId != null && suspectId.equalsIgnoreCase(e.getAssociatedPersonId()))
                .collect(java.util.stream.Collectors.toList());
    }

    default List<EvidenceViewModel> getEvidenceForSuspect(String suspectId) {
        return getEvidenceForSuspect(null, suspectId);
    }
}
