package com.mysteryvault.ui.swing.mock;

import com.mysteryvault.ui.swing.ports.ReportViewPort;
import com.mysteryvault.ui.swing.viewmodel.ReportViewModel;

public class MockReportViewPort implements ReportViewPort {

    @Override
    public ReportViewModel generateReport(String caseId) {
        String summary = "OFFICIAL POLICE DOSSIER — CASE #103: THE POISONED DRINK\n" +
                "===========================================================\n" +
                "Primary Suspect: Meera (Corporate Accountant)\n" +
                "Motive: Concealment of significant financial embezzlement uncovered by victim\n" +
                "Decisive Evidence:\n" +
                " - E01: Crystal Wine Glass (synthetic digitalis traces match victim toxicology)\n" +
                " - E03: Security CCTV Footage (timestamps confirm access at 8:45 PM)\n" +
                " - E05: Central Forensic Lab Report (neurotoxin alkaloid profile confirmed)\n" +
                "\n" +
                "Status: Solved and submitted for prosecution.\n" +
                "Commendation: Master Detective Service Citation.";

        return new ReportViewModel(caseId, "THE POISONED DRINK", "Meera", "Financial dispute", "SOLVED", summary);
    }
}
