package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.AnalysisViewPort;
import com.mysteryvault.ui.swing.viewmodel.AnalysisViewModel;

public class MockAnalysisViewPort implements AnalysisViewPort {

    @Override
    public AnalysisViewModel getAnalysisResults(String caseId) {
        return new AnalysisViewModel(
                100, // Timeline Complete
                100, // Evidence Complete
                100, // Statement Complete
                90,  // Connection 90%
                8,   // Connections Found
                3,   // Contradictions
                5,   // Important Evidence
                2    // Suspicious Characters
        );
    }
}
