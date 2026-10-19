package com.mysteryvault.ui.swing.ports;

import com.mysteryvault.ui.swing.viewmodel.SuspectViewModel;
import java.util.List;

/**
 * Presentation port for Suspect and Person profiles.
 * Future integration: Member 1 (Person/Suspect / Module 2).
 */
public interface SuspectViewPort {
    List<SuspectViewModel> getSuspectsForCase(String caseId);
    SuspectViewModel getSuspectById(String suspectId);
}
